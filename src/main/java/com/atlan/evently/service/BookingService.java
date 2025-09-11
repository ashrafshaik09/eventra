package com.atlan.evently.service;

import com.atlan.evently.dto.BookingRequest;
import com.atlan.evently.dto.BookingResponse;
import com.atlan.evently.exception.EventException;
import com.atlan.evently.mapper.BookingMapper;
import com.atlan.evently.model.Booking;
import com.atlan.evently.model.Event;
import com.atlan.evently.model.User;
import com.atlan.evently.repository.BookingRepository;
import com.atlan.evently.repository.EventRepository;
import com.atlan.evently.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final BookingMapper bookingMapper;

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

    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
        // Validate request
        validateBookingRequest(request);
        
        // Parse UUIDs
        UUID userUuid = parseUUID(request.getUserId(), "User ID");
        UUID eventUuid = parseUUID(request.getEventId(), "Event ID");
        
        // Get entities
        User user = userRepository.findById(userUuid)
                .orElseThrow(() -> new EventException("User not found", 
                        "USER_NOT_FOUND", 
                        "User with ID " + request.getUserId() + " does not exist"));
        
        Event event = eventRepository.findById(eventUuid)
                .orElseThrow(() -> new EventException("Event not found", 
                        "EVENT_NOT_FOUND", 
                        "Event with ID " + request.getEventId() + " does not exist"));

        // Check availability (atomic operation will be implemented in Phase D)
        if (event.getAvailableSeats() < request.getQuantity()) {
            throw new EventException("Insufficient seats available", 
                    "EVENT_SOLD_OUT", 
                    "Only " + event.getAvailableSeats() + " seats available, requested " + request.getQuantity());
        }

        // Create booking entity
        Booking booking = bookingMapper.toEntity(request);
        booking.setUser(user);
        booking.setEvent(event);
        
        // Save booking and update event capacity (will be made atomic in Phase D)
        event.setAvailableSeats(event.getAvailableSeats() - request.getQuantity());
        eventRepository.save(event);
        
        Booking savedBooking = bookingRepository.save(booking);
        return bookingMapper.toResponse(savedBooking);
    }

    @Transactional
    public void cancelBooking(String bookingId) {
        UUID bookingUuid = parseUUID(bookingId, "Booking ID");
        Booking booking = bookingRepository.findById(bookingUuid)
                .orElseThrow(() -> new EventException("Booking not found", 
                        "BOOKING_NOT_FOUND", 
                        "Booking with ID " + bookingId + " does not exist"));
        
        if ("CANCELLED".equals(booking.getStatus())) {
            throw new EventException("Booking already cancelled", 
                    "BOOKING_ALREADY_CANCELLED", 
                    "Booking " + bookingId + " is already cancelled");
        }

        // Update booking status and restore seats
        booking.setStatus("CANCELLED");
        Event event = booking.getEvent();
        event.setAvailableSeats(event.getAvailableSeats() + booking.getQuantity());
        
        bookingRepository.save(booking);
        eventRepository.save(event);
    }

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