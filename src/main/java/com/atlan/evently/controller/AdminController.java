package com.atlan.evently.controller;

import com.atlan.evently.dto.AnalyticsResponse;
import com.atlan.evently.dto.BookingResponse;
import com.atlan.evently.dto.EventRequest;
import com.atlan.evently.dto.EventResponse;
import com.atlan.evently.dto.UserRegistrationRequest;
import com.atlan.evently.dto.UserResponse;
import com.atlan.evently.dto.UserUpdateRequest;
import com.atlan.evently.service.AdminService;
import com.atlan.evently.service.BookingService;
import com.atlan.evently.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AdminService adminService;
    private final BookingService bookingService;
    private final UserService userService; // Add UserService for admin user operations

    // ============= EVENT MANAGEMENT =============
    @PostMapping("/events")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventResponse> createEvent(@Valid @RequestBody EventRequest request) {
        EventResponse response = adminService.createEvent(request);
        return ResponseEntity.status(201).body(response);
    }

    @PutMapping("/events/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventResponse> updateEvent(@PathVariable String id,
                                                    @Valid @RequestBody EventRequest request) {
        EventResponse response = adminService.updateEvent(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/events/analytics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AnalyticsResponse> getAnalytics() {
        AnalyticsResponse response = adminService.getAnalytics();
        return ResponseEntity.ok(response);
    }

    // ============= BOOKING MANAGEMENT =============
    @GetMapping("/bookings")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BookingResponse>> getAllBookings(@RequestParam(required = false) String status,
                                                               @RequestParam(required = false) String eventId) {
        List<BookingResponse> bookings = adminService.getAllBookings(status, eventId);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/bookings/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BookingResponse>> getUserBookings(@PathVariable String userId,
                                                               @RequestParam(required = false) String status) {
        List<BookingResponse> bookings = bookingService.getUserBookingsAsDto(userId, status);
        return ResponseEntity.ok(bookings);
    }

    @DeleteMapping("/bookings/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> cancelBookingAdmin(@PathVariable String id) {
        bookingService.cancelBooking(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/bookings/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BookingResponse> getBookingDetails(@PathVariable String id) {
        BookingResponse booking = adminService.getBookingById(id);
        return ResponseEntity.ok(booking);
    }

    // ============= USER MANAGEMENT (NEW) =============
    
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserResponse>> getAllUsers(Pageable pageable,
                                                         @RequestParam(required = false) String role,
                                                         @RequestParam(required = false) Boolean isActive) {
        Page<UserResponse> users = adminService.getAllUsers(pageable, role, isActive);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable String id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRegistrationRequest request) {
        UserResponse response = adminService.createUser(request);
        return ResponseEntity.status(201).body(response);
    }

    @PutMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateUser(@PathVariable String id,
                                                  @Valid @RequestBody UserUpdateRequest request) {
        UserResponse response = adminService.updateUser(id, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/users/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivateUser(@PathVariable String id) {
        adminService.deactivateUser(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/users/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> activateUser(@PathVariable String id) {
        adminService.activateUser(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/users/{id}/promote")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> promoteToAdmin(@PathVariable String id) {
        adminService.promoteUserToAdmin(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/users/{id}/demote")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> demoteToUser(@PathVariable String id) {
        adminService.demoteUserToRegular(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        adminService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/users/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserResponse>> searchUsers(@RequestParam String query,
                                                         Pageable pageable) {
        Page<UserResponse> users = adminService.searchUsers(query, pageable);
        return ResponseEntity.ok(users);
    }
}