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
        List<BookingResponse> bookings = bookingService.getUserBookingsAsDto(userId, status);
        return ResponseEntity.ok(bookings);
    }

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody BookingRequest request) {
        BookingResponse response = bookingService.createBooking(request);
        return ResponseEntity.status(201).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelBooking(@PathVariable String id) {
        bookingService.cancelBooking(id);
        return ResponseEntity.noContent().build();
    }
}