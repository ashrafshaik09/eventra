package com.atlan.evently.controller;

import com.atlan.evently.dto.BookingRequest;
import com.atlan.evently.dto.BookingResponse;
import com.atlan.evently.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/bookings")
public class BookingController {

    private final BookingService bookingService;

    @GetMapping("/users/{userId}")
    public ResponseEntity<List<BookingResponse>> getUserBookings(@PathVariable String userId,
                                                                @RequestParam(required = false) String status) {
        List<BookingResponse> bookings = bookingService.getUserBookings(userId, status)
                .stream()
                .map(booking -> {
                    BookingResponse response = new BookingResponse();
                    response.setBookingId(booking.getId());
                    response.setUserId(booking.getUser().getId());
                    response.setEventId(booking.getEvent().getId());
                    response.setQuantity(booking.getQuantity());
                    response.setBookingStatus(booking.getStatus());
                    return response;
                })
                .toList();
        return ResponseEntity.ok(bookings);
    }

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody BookingRequest request) {
        // Placeholder: Concurrency handling to be implemented in BookingService (Phase D)
        BookingResponse response = new BookingResponse();
        response.setBookingId("temp-id"); // Replace with actual ID from service
        response.setUserId(request.getUserId());
        response.setEventId(request.getEventId());
        response.setQuantity(request.getQuantity());
        response.setBookingStatus("CONFIRMED");
        return ResponseEntity.status(201).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelBooking(@PathVariable String id) {
        // Placeholder: Cancellation logic to be implemented in BookingService (Phase D)
        return ResponseEntity.noContent().build();
    }
}