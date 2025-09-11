package com.atlan.evently.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.ZonedDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class BookingTest {

    private Booking booking;
    private User user;
    private Event event;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(UUID.randomUUID())
                .email("user@example.com")
                .name("John Doe")
                .passwordHash("hashedPassword123")
                .role(User.UserRole.USER)
                .isActive(true)
                .createdAt(ZonedDateTime.now())
                .build();
        
        event = Event.builder()
                .id(UUID.randomUUID())
                .name("Concert 2025")
                .venue("City Hall")
                .startsAt(ZonedDateTime.now().plusDays(1))
                .capacity(100)
                .availableSeats(100)
                .createdAt(ZonedDateTime.now())
                .version(1)
                .build();
        
        booking = Booking.builder()
                .id(UUID.randomUUID())
                .user(user)
                .event(event)
                .quantity(2)
                .status("CONFIRMED")
                .idempotencyKey("test-idempotency-key")
                .createdAt(ZonedDateTime.now())
                .build();
    }

    @Test
    void testValidBookingCreation() {
        assertNotNull(booking);
        assertEquals(2, booking.getQuantity());
        assertEquals("CONFIRMED", booking.getStatus());
        assertEquals("test-idempotency-key", booking.getIdempotencyKey());
    }

    @Test
    void testBookingStatusHelperMethods() {
        // Test confirmed booking
        assertTrue(booking.isConfirmed());
        assertFalse(booking.isCancelled());
        
        // Test cancelled booking
        booking.cancel();
        assertFalse(booking.isConfirmed());
        assertTrue(booking.isCancelled());
        assertEquals("CANCELLED", booking.getStatus());
        
        // Test confirm method
        booking.confirm();
        assertTrue(booking.isConfirmed());
        assertFalse(booking.isCancelled());
        assertEquals("CONFIRMED", booking.getStatus());
    }

    @Test
    void testInvalidStatusThrowsException() {
        booking.setStatus("PENDING");
        assertThrows(IllegalStateException.class, booking::validateStatus);
    }

    @Test
    void testInvalidQuantityThrowsException() {
        booking.setQuantity(0);
        assertThrows(IllegalStateException.class, booking::validateStatus);
        
        booking.setQuantity(-1);
        assertThrows(IllegalStateException.class, booking::validateStatus);
    }

    @Test
    void testIdempotencyKeyHandling() {
        String newKey = "new-idempotency-key";
        booking.setIdempotencyKey(newKey);
        assertEquals(newKey, booking.getIdempotencyKey());
        
        // Test null idempotency key (should be allowed)
        booking.setIdempotencyKey(null);
        assertNull(booking.getIdempotencyKey());
    }

    @Test
    void testSettersUpdateFields() {
        User newUser = User.builder()
                .id(UUID.randomUUID())
                .email("newuser@example.com")
                .name("Jane Doe")
                .passwordHash("newHashedPassword123")
                .role(User.UserRole.USER)
                .isActive(true)
                .createdAt(ZonedDateTime.now())
                .build();
        
        booking.setUser(newUser);
        booking.setQuantity(3);
        booking.setStatus("CANCELLED");
        
        assertEquals(newUser, booking.getUser());
        assertEquals(3, booking.getQuantity());
        assertEquals("CANCELLED", booking.getStatus());
    }
}