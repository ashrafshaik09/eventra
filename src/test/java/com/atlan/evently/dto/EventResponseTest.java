package com.atlan.evently.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EventResponseTest {

    private EventResponse eventResponse;

    @BeforeEach
    void setUp() {
        eventResponse = new EventResponse();
    }

    @Test
    void testValidEventResponse() {
        ZonedDateTime now = ZonedDateTime.now();
        eventResponse.setEventId("1");
        eventResponse.setName("Concert 2025");
        eventResponse.setVenue("City Hall");
        eventResponse.setStartTime(now.plusDays(1));
        eventResponse.setCapacity(100);
        eventResponse.setAvailableSeats(90);

        assertEquals("1", eventResponse.getEventId());
        assertEquals("Concert 2025", eventResponse.getName());
        assertEquals("City Hall", eventResponse.getVenue());
        assertEquals(now.plusDays(1), eventResponse.getStartTime());
        assertEquals(100, eventResponse.getCapacity());
        assertEquals(90, eventResponse.getAvailableSeats());
    }

    @Test
    void testSettersAndGetters() {
        eventResponse.setEventId("2");
        eventResponse.setName("Workshop 2025");
        eventResponse.setVenue("Community Center");
        eventResponse.setStartTime(ZonedDateTime.now());
        eventResponse.setCapacity(50);
        eventResponse.setAvailableSeats(40);

        assertEquals("2", eventResponse.getEventId());
        assertEquals("Workshop 2025", eventResponse.getName());
        assertEquals("Community Center", eventResponse.getVenue());
        assertNotNull(eventResponse.getStartTime());
        assertEquals(50, eventResponse.getCapacity());
        assertEquals(40, eventResponse.getAvailableSeats());
    }
}