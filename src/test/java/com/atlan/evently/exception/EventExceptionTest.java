package com.atlan.evently.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EventExceptionTest {

    @Test
    void testEventExceptionCreation() {
        String message = "Event is sold out";
        String errorCode = "EVENT_001";
        String details = "No available seats";
        EventException exception = new EventException(message, errorCode, details);

        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertEquals(errorCode, exception.getErrorCode());
        assertEquals(details, exception.getDetails());
    }
}