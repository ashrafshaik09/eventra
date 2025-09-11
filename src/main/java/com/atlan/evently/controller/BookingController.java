package com.atlan.evently.controller;

import com.atlan.evently.dto.BookingRequest;
import com.atlan.evently.dto.BookingResponse;
import com.atlan.evently.service.BookingService;
import com.atlan.evently.service.WaitlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/bookings")
@Tag(name = "Bookings", description = "Booking and waitlist management endpoints")
public class BookingController {

    private final BookingService bookingService;
    private final WaitlistService waitlistService;

    // ========== EXISTING BOOKING ENDPOINTS ==========

    @GetMapping("/users/{userId}")
    @Operation(
        summary = "Get user booking history",
        description = "Retrieve all bookings for a specific user with optional status filtering"
    )
    @ApiResponse(responseCode = "200", description = "Successfully retrieved user bookings")
    @ApiResponse(responseCode = "400", description = "Invalid user ID format")
    public ResponseEntity<List<BookingResponse>> getUserBookings(
            @Parameter(description = "User ID") @PathVariable String userId,
            @Parameter(description = "Filter by booking status (CONFIRMED/CANCELLED)") 
            @RequestParam(required = false) String status) {
        List<BookingResponse> bookings = bookingService.getUserBookingsAsDto(userId, status);
        return ResponseEntity.ok(bookings);
    }

    @PostMapping
    @Operation(
        summary = "Create booking",
        description = "Book tickets for an event with concurrency protection and idempotency support"
    )
    @ApiResponse(responseCode = "201", description = "Booking created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid booking request")
    @ApiResponse(responseCode = "409", description = "Event sold out or booking conflict")
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody BookingRequest request) {
        BookingResponse response = bookingService.createBooking(request);
        return ResponseEntity.status(201).body(response);
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Cancel booking",
        description = "Cancel an existing booking and restore seats to the event"
    )
    @ApiResponse(responseCode = "204", description = "Booking cancelled successfully")
    @ApiResponse(responseCode = "404", description = "Booking not found")
    @ApiResponse(responseCode = "409", description = "Cannot cancel booking (event started or already cancelled)")
    public ResponseEntity<Void> cancelBooking(
            @Parameter(description = "Booking ID") @PathVariable String id) {
        bookingService.cancelBooking(id);
        return ResponseEntity.noContent().build();
    }

    // ========== NEW WAITLIST ENDPOINTS ==========

    @PostMapping("/events/{eventId}/waitlist")
    @Operation(
        summary = "Join event waitlist",
        description = "Add user to FIFO waitlist when event is sold out. Returns current position in queue."
    )
    @ApiResponse(responseCode = "201", description = "Successfully joined waitlist")
    @ApiResponse(responseCode = "400", description = "Invalid request or user/event ID")
    @ApiResponse(responseCode = "409", description = "Event has available seats (book directly) or user already on waitlist")
    public ResponseEntity<WaitlistService.WaitlistResponse> joinWaitlist(
            @Parameter(description = "Event ID") @PathVariable String eventId,
            @Parameter(description = "User ID in request body") @RequestBody WaitlistJoinRequest request) {
        
        WaitlistService.WaitlistResponse response = waitlistService.joinWaitlist(request.getUserId(), eventId);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/users/{userId}/waitlist")
    @Operation(
        summary = "Get user waitlist entries",
        description = "Retrieve all waitlist entries for a user across all events"
    )
    @ApiResponse(responseCode = "200", description = "Successfully retrieved waitlist entries")
    @ApiResponse(responseCode = "400", description = "Invalid user ID format")
    public ResponseEntity<List<WaitlistService.WaitlistResponse>> getUserWaitlistEntries(
            @Parameter(description = "User ID") @PathVariable String userId) {
        
        List<WaitlistService.WaitlistResponse> waitlistEntries = waitlistService.getUserWaitlistEntries(userId);
        return ResponseEntity.ok(waitlistEntries);
    }

    @GetMapping("/events/{eventId}/waitlist/position")
    @Operation(
        summary = "Get waitlist position",
        description = "Get user's current position in waitlist for a specific event"
    )
    @ApiResponse(responseCode = "200", description = "Successfully retrieved waitlist position")
    @ApiResponse(responseCode = "404", description = "User not on waitlist for this event")
    public ResponseEntity<WaitlistService.WaitlistResponse> getWaitlistPosition(
            @Parameter(description = "Event ID") @PathVariable String eventId,
            @Parameter(description = "User ID") @RequestParam String userId) {
        
        WaitlistService.WaitlistResponse position = waitlistService.getWaitlistPosition(userId, eventId);
        return ResponseEntity.ok(position);
    }

    @DeleteMapping("/waitlist/{waitlistId}")
    @Operation(
        summary = "Leave waitlist",
        description = "Remove user from event waitlist and adjust positions for remaining users"
    )
    @ApiResponse(responseCode = "204", description = "Successfully left waitlist")
    @ApiResponse(responseCode = "404", description = "Waitlist entry not found")
    @ApiResponse(responseCode = "409", description = "Cannot leave waitlist (user already notified)")
    public ResponseEntity<Void> leaveWaitlist(
            @Parameter(description = "Waitlist ID") @PathVariable String waitlistId) {
        
        waitlistService.leaveWaitlist(waitlistId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/waitlist/{waitlistId}/convert")
    @Operation(
        summary = "Convert waitlist to booking",
        description = "Convert waitlist entry to actual booking (called when user books after notification)"
    )
    @ApiResponse(responseCode = "201", description = "Waitlist converted to booking successfully")
    @ApiResponse(responseCode = "400", description = "Invalid conversion request")
    @ApiResponse(responseCode = "409", description = "Waitlist entry expired or seats no longer available")
    public ResponseEntity<BookingResponse> convertWaitlistToBooking(
            @Parameter(description = "Waitlist ID") @PathVariable String waitlistId,
            @Valid @RequestBody BookingRequest bookingRequest) {
        
        // Create the actual booking
        BookingResponse booking = bookingService.createBooking(bookingRequest);
        
        // Mark waitlist entry as converted
        waitlistService.markAsConverted(waitlistId);
        
        return ResponseEntity.status(201).body(booking);
    }

    // ========== INNER DTO CLASS ==========

    public static class WaitlistJoinRequest {
        private String userId;

        public WaitlistJoinRequest() {}

        public WaitlistJoinRequest(String userId) {
            this.userId = userId;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }
    }
}