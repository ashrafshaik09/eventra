package com.atlan.evently.service;

import com.atlan.evently.dto.BookingRequest;
import com.atlan.evently.dto.BookingResponse;
import com.atlan.evently.dto.events.BookingCancelledEvent;
import com.atlan.evently.exception.BookingConflictException;
import com.atlan.evently.exception.DuplicateBookingException;
import com.atlan.evently.exception.EventException;
import com.atlan.evently.mapper.BookingMapper;
import com.atlan.evently.model.Booking;
import com.atlan.evently.model.Event;
import com.atlan.evently.model.User;
import com.atlan.evently.repository.BookingRepository;
import com.atlan.evently.repository.EventRepository;
import com.atlan.evently.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final BookingMapper bookingMapper;
    private final EventPublisher eventPublisher; // NEW: Add event publisher

    // ========== READ OPERATIONS (unchanged) ==========
    
    @Transactional(readOnly = true)
    public List<Booking> getUserBookings(UUID userId, String status) {
        validateStatus(status);
        if (status != null) {
            return bookingRepository.findByUserIdAndStatus(userId, status);
        }
        return bookingRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getUserBookingsAsDto(String userId, String status) {
        UUID userUuid = parseUUID(userId, "User ID");
        return getUserBookings(userUuid, status).stream()
                .map(bookingMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ========== ENHANCED BOOKING CREATION WITH CONCURRENCY PROTECTION (unchanged) ==========

    /**
     * Create booking with multi-layered concurrency protection:
     * 1. Idempotency key check (prevent duplicates on retries)
     * 2. Atomic seat reservation (prevent overselling) 
     * 3. Optimistic locking fallback (handle race conditions)
     * 4. Retry mechanism for transient conflicts
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    @Retryable(value = {OptimisticLockingFailureException.class, BookingConflictException.class}, 
               maxAttempts = 3, backoff = @Backoff(delay = 100, multiplier = 2))
    public BookingResponse createBooking(BookingRequest request) {
        log.info("Creating booking for user {} event {} quantity {} idempotencyKey {}", 
                request.getUserId(), request.getEventId(), request.getQuantity(), request.getIdempotencyKey());

        // Step 1: Validation
        validateBookingRequest(request);
        
        // Step 2: Parse and validate entities
        UUID userUuid = parseUUID(request.getUserId(), "User ID");
        UUID eventUuid = parseUUID(request.getEventId(), "Event ID");
        
        // Step 3: Idempotency check - return existing booking if found
        if (request.getIdempotencyKey() != null) {
            Optional<Booking> existingBooking = bookingRepository.findByIdempotencyKey(request.getIdempotencyKey());
            if (existingBooking.isPresent()) {
                log.info("Duplicate booking request detected for idempotencyKey: {}", request.getIdempotencyKey());
                if (existingBooking.get().isConfirmed()) {
                    return bookingMapper.toResponse(existingBooking.get());
                } else {
                    throw new DuplicateBookingException(
                        "Booking with this idempotency key exists but is cancelled",
                        "IdempotencyKey: " + request.getIdempotencyKey()
                    );
                }
            }
        }
        
        // Step 4: Get entities with proper error handling
        User user = userRepository.findById(userUuid)
                .orElseThrow(() -> new EventException("User not found", 
                        "USER_NOT_FOUND", 
                        "User with ID " + request.getUserId() + " does not exist"));
        
        Event event = eventRepository.findById(eventUuid)
                .orElseThrow(() -> new EventException("Event not found", 
                        "EVENT_NOT_FOUND", 
                        "Event with ID " + request.getEventId() + " does not exist"));

        // Step 5: Business validation
        if (event.getStartsAt().isBefore(ZonedDateTime.now())) {
            throw new EventException("Event has already started", 
                    "EVENT_STARTED", 
                    "Cannot book tickets for past events");
        }

        // Step 6: Check for existing booking (prevent user from double-booking same event)
        Optional<Booking> existingUserBooking = bookingRepository.findExistingBooking(userUuid, eventUuid);
        if (existingUserBooking.isPresent()) {
            throw new BookingConflictException(
                "User already has a booking for this event",
                "Existing booking ID: " + existingUserBooking.get().getId()
            );
        }

        // Step 7: ATOMIC SEAT RESERVATION (Primary concurrency protection)
        int rowsUpdated = bookingRepository.reserveSeats(eventUuid, request.getQuantity());
        if (rowsUpdated == 0) {
            // Refresh event to get current state
            Event refreshedEvent = eventRepository.findById(eventUuid).orElseThrow();
            throw new BookingConflictException(
                "Insufficient seats available", 
                String.format("Requested: %d, Available: %d", request.getQuantity(), refreshedEvent.getAvailableSeats())
            );
        }

        log.info("Successfully reserved {} seats for event {}", request.getQuantity(), eventUuid);

        // Step 8: Create booking record
        try {
            Booking booking = bookingMapper.toEntity(request);
            booking.setUser(user);
            booking.setEvent(event);
            booking.setIdempotencyKey(request.getIdempotencyKey());
            booking.setCreatedAt(ZonedDateTime.now());
            
            Booking savedBooking = bookingRepository.save(booking);
            
            log.info("Booking created successfully: {}", savedBooking.getId());
            return bookingMapper.toResponse(savedBooking);
            
        } catch (Exception e) {
            // Rollback: restore seats if booking creation fails
            log.error("Booking creation failed, restoring seats", e);
            bookingRepository.restoreSeats(eventUuid, request.getQuantity());
            throw new EventException("Booking creation failed", 
                    "BOOKING_CREATION_ERROR", 
                    "Please try again. Seats have been restored.");
        }
    }

    // ========== ENHANCED BOOKING CANCELLATION WITH KAFKA EVENT PUBLISHING ==========

    @Transactional(isolation = Isolation.READ_COMMITTED)
    @Retryable(value = {OptimisticLockingFailureException.class}, 
               maxAttempts = 3, backoff = @Backoff(delay = 50))
    public void cancelBooking(String bookingId) {
        log.info("Cancelling booking: {}", bookingId);
        
        UUID bookingUuid = parseUUID(bookingId, "Booking ID");
        Booking booking = bookingRepository.findById(bookingUuid)
                .orElseThrow(() -> new EventException("Booking not found", 
                        "BOOKING_NOT_FOUND", 
                        "Booking with ID " + bookingId + " does not exist"));
        
        if (booking.isCancelled()) {
            throw new EventException("Booking already cancelled", 
                    "BOOKING_ALREADY_CANCELLED", 
                    "Booking " + bookingId + " is already cancelled");
        }

        // Check if event has started (business rule: no cancellation after event start)
        if (booking.getEvent().getStartsAt().isBefore(ZonedDateTime.now())) {
            throw new EventException("Cannot cancel booking for started event", 
                    "EVENT_ALREADY_STARTED", 
                    "Event started at " + booking.getEvent().getStartsAt());
        }

        // Store event details before cancellation for Kafka event
        String userId = booking.getUser().getId().toString();
        String eventId = booking.getEvent().getId().toString();
        Integer quantity = booking.getQuantity();

        // Update booking status
        booking.cancel();
        
        // ATOMIC SEAT RESTORATION
        int rowsUpdated = bookingRepository.restoreSeats(booking.getEvent().getId(), booking.getQuantity());
        if (rowsUpdated == 0) {
            log.warn("Failed to restore seats for event {} - event may have been deleted", booking.getEvent().getId());
        }
        
        bookingRepository.save(booking);
        
        log.info("Booking {} cancelled successfully, {} seats restored", bookingId, booking.getQuantity());

        // NEW: Publish booking cancelled event to Kafka for waitlist processing
        try {
            BookingCancelledEvent cancelledEvent = new BookingCancelledEvent(
                bookingId,
                userId,
                eventId,
                quantity,
                ZonedDateTime.now(),
                "USER_CANCELLED" // Could be "ADMIN_CANCELLED", "EXPIRED", etc.
            );

            eventPublisher.publishBookingCancelled(cancelledEvent);
            log.info("Published booking cancelled event for booking {} to trigger waitlist processing", bookingId);
            
        } catch (Exception e) {
            // Don't fail the cancellation if Kafka publishing fails
            log.error("Failed to publish booking cancelled event for booking {}: {}", bookingId, e.getMessage(), e);
        }
    }

    // ========== VALIDATION METHODS (enhanced) ==========
    
    private void validateBookingRequest(BookingRequest request) {
        if (request.getUserId() == null || request.getUserId().trim().isEmpty()) {
            throw new IllegalArgumentException("User ID is required");
        }
        if (request.getEventId() == null || request.getEventId().trim().isEmpty()) {
            throw new IllegalArgumentException("Event ID is required");
        }
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (request.getQuantity() > 10) { // Business rule: max 10 tickets per booking
            throw new IllegalArgumentException("Maximum 10 tickets per booking allowed");
        }
    }

    private void validateStatus(String status) {
        if (status != null && !"CONFIRMED".equals(status) && !"CANCELLED".equals(status)) {
            throw new IllegalArgumentException("Status must be CONFIRMED or CANCELLED");
        }
    }

    private UUID parseUUID(String id, String fieldName) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid " + fieldName + " format: " + id);
        }
    }
}