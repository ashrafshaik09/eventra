package com.atlan.evently.service;

import com.atlan.evently.dto.BookingRequest;
import com.atlan.evently.dto.BookingResponse;
import com.atlan.evently.dto.EventResponse;
import com.atlan.evently.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
public class AsyncBookingService {

    private final BookingService bookingService;
    private final EventService eventService; 
    private final UserService userService;

    /**
     * Parallel validation of booking request components
     * Validates user, event, and availability simultaneously
     */
    @Async("bookingExecutor")
    public CompletableFuture<UserResponse> validateUserAsync(String userId) {
        log.debug("Async validation of user: {}", userId);
        try {
            UserResponse user = userService.getUserById(userId);
            return CompletableFuture.completedFuture(user);
        } catch (Exception e) {
            log.error("User validation failed for {}: {}", userId, e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }

    @Async("bookingExecutor")
    public CompletableFuture<EventResponse> validateEventAsync(String eventId) {
        log.debug("Async validation of event: {}", eventId);
        try {
            EventResponse event = eventService.getEventByIdAsDto(eventId);
            return CompletableFuture.completedFuture(event);
        } catch (Exception e) {
            log.error("Event validation failed for {}: {}", eventId, e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }

    @Async("bookingExecutor")
    public CompletableFuture<List<BookingResponse>> checkExistingBookingsAsync(String userId) {
        log.debug("Async check of existing bookings for user: {}", userId);
        try {
            List<BookingResponse> bookings = bookingService.getUserBookingsAsDto(userId, "CONFIRMED");
            return CompletableFuture.completedFuture(bookings);
        } catch (Exception e) {
            log.error("Existing bookings check failed for {}: {}", userId, e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Enhanced booking creation with parallel validation
     * Follows the Medium article pattern but maintains atomic booking creation
     */
    public BookingResponse createBookingWithParallelValidation(BookingRequest request) {
        log.info("Creating booking with parallel validation for user {} event {}", 
                request.getUserId(), request.getEventId());

        try {
            // Step 1: Start parallel validation (similar to Medium article approach)
            CompletableFuture<UserResponse> userValidation = validateUserAsync(request.getUserId());
            CompletableFuture<EventResponse> eventValidation = validateEventAsync(request.getEventId());
            CompletableFuture<List<BookingResponse>> existingBookingsCheck = checkExistingBookingsAsync(request.getUserId());

            // Step 2: Wait for all validations to complete (parallel execution)
            CompletableFuture<Void> allValidations = CompletableFuture.allOf(
                userValidation, eventValidation, existingBookingsCheck
            );

            // Wait for completion with timeout
            allValidations.get(); // This blocks until all async operations complete

            // Step 3: Get results from parallel operations
            UserResponse user = userValidation.get();
            EventResponse event = eventValidation.get();
            List<BookingResponse> existingBookings = existingBookingsCheck.get();

            log.info("Parallel validation completed - User: {}, Event: {}, Existing bookings: {}", 
                    user.getName(), event.getName(), existingBookings.size());

            // Step 4: Perform atomic booking creation (your existing concurrency-safe logic)
            return bookingService.createBooking(request);

        } catch (Exception e) {
            log.error("Parallel booking validation failed", e);
            throw new RuntimeException("Booking validation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Bulk booking processing with parallel execution
     * Process multiple booking requests simultaneously
     */
    @Async("bookingExecutor")
    public CompletableFuture<BookingResponse> createBookingAsync(BookingRequest request) {
        try {
            BookingResponse response = bookingService.createBooking(request);
            return CompletableFuture.completedFuture(response);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    public List<BookingResponse> processBulkBookings(List<BookingRequest> requests) {
        log.info("Processing {} booking requests in parallel", requests.size());

        // Create parallel booking requests
        List<CompletableFuture<BookingResponse>> futures = requests.stream()
                .map(this::createBookingAsync)
                .toList();

        // Wait for all to complete and collect results
        return futures.stream()
                .map(CompletableFuture::join) // This will throw if any failed
                .toList();
    }
}
