package com.atlan.evently.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ErrorResponseTest {

    private ErrorResponse errorResponse;

    @BeforeEach
    void setUp() {
        errorResponse = new ErrorResponse(
                ZonedDateTime.now(),
                409,
                "Conflict",
                "Event sold out",
                "EVENT_001",
                "No seats available",
                "/api/v1/bookings"
        );
    }

    @Test
    void testGettersAndSetters() {
        assertNotNull(errorResponse.getTimestamp());
        assertEquals(409, errorResponse.getStatus());
        assertEquals("Conflict", errorResponse.getError());
        assertEquals("Event sold out", errorResponse.getMessage());
        assertEquals("EVENT_001", errorResponse.getErrorCode());
        assertEquals("No seats available", errorResponse.getDetails());
        assertEquals("/api/v1/bookings", errorResponse.getPath());

        ZonedDateTime newTime = ZonedDateTime.now().plusDays(1);
        errorResponse.setTimestamp(newTime);
        errorResponse.setStatus(400);
        errorResponse.setError("Bad Request");
        errorResponse.setMessage("Invalid input");
        errorResponse.setErrorCode("VALIDATION_001");
        errorResponse.setDetails("Field error");
        errorResponse.setPath("/api/v1/events");

        assertEquals(newTime, errorResponse.getTimestamp());
        assertEquals(400, errorResponse.getStatus());
        assertEquals("Bad Request", errorResponse.getError());
        assertEquals("Invalid input", errorResponse.getMessage());
        assertEquals("VALIDATION_001", errorResponse.getErrorCode());
        assertEquals("Field error", errorResponse.getDetails());
        assertEquals("/api/v1/events", errorResponse.getPath());
    }
}