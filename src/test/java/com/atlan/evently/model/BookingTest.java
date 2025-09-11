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
        user = User.builder().id(UUID.randomUUID()).email("user@example.com").name("John Doe").createdAt(ZonedDateTime.now()).build();
        event = Event.builder().id(UUID.randomUUID()).name("Concert 2025").venue("City Hall").startsAt(ZonedDateTime.now().plusDays(1))
                .capacity(100).availableSeats(100).createdAt(ZonedDateTime.now()).version(1).build();
        booking = Booking.builder()
                .id(UUID.randomUUID())
                .user(user)
                .event(event)
                .quantity(2)
                .status("CONFIRMED")
                .createdAt(ZonedDateTime.now())
                .build();
    }

    @Test
    void testValidBookingCreation() {
        assertNotNull(booking);
        assertEquals(2, booking.getQuantity());
        assertEquals("CONFIRMED", booking.getStatus());
    }

    @Test
    void testInvalidStatusThrowsException() {
        booking.setStatus("PENDING");
        assertThrows(IllegalStateException.class, booking::validateStatus);
    }

    @Test
    void testSettersUpdateFields() {
        User newUser = User.builder().id(UUID.randomUUID()).email("newuser@example.com").name("Jane Doe").createdAt(ZonedDateTime.now()).build();
        booking.setUser(newUser);
        booking.setQuantity(1);
        booking.setStatus("CANCELLED");
        assertEquals(newUser, booking.getUser());
        assertEquals(1, booking.getQuantity());
        assertEquals("CANCELLED", booking.getStatus());
    }
}