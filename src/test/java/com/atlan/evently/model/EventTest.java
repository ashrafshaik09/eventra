package com.atlan.evently.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EventTest {

    private Event event;

    @BeforeEach
    void setUp() {
        event = Event.builder()
                .id("1")
                .name("Concert 2025")
                .venue("City Hall")
                .startsAt(ZonedDateTime.now().plusDays(1))
                .capacity(100)
                .availableSeats(100)
                .createdAt(ZonedDateTime.now())
                .version(1)
                .build();
    }

    @Test
    void testValidEventCreation() {
        assertNotNull(event);
        assertEquals(100, event.getCapacity());
        assertEquals(100, event.getAvailableSeats());
    }

    @Test
    void testAvailableSeatsExceedsCapacity() {
        event.setAvailableSeats(101);
        assertThrows(IllegalStateException.class, event::validateAvailability);
    }

    @Test
    void testNegativeAvailableSeats() {
        event.setAvailableSeats(-1);
        assertThrows(IllegalStateException.class, event::validateAvailability);
    }
}