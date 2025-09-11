package com.atlan.evently.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConstantsTest {

    @Test
    void testConstantsValues() {
        assertEquals("EVENT_001", Constants.ERROR_CODE_EVENT_SOLD_OUT);
        assertEquals("VALIDATION_001", Constants.ERROR_CODE_VALIDATION_FAILED);
        assertEquals("INTERNAL_001", Constants.ERROR_CODE_INTERNAL_ERROR);
        assertEquals("CONFIRMED", Constants.STATUS_CONFIRMED);
        assertEquals("CANCELLED", Constants.STATUS_CANCELLED);
        assertEquals("/api/v1", Constants.API_BASE_PATH);
        assertEquals("/api/v1/events", Constants.EVENTS_PATH);
        assertEquals("/api/v1/bookings", Constants.BOOKINGS_PATH);
        assertEquals("/api/v1/admin/events", Constants.ADMIN_EVENTS_PATH);
        assertEquals("Event name is required", Constants.MSG_EVENT_NAME_REQUIRED);
        assertEquals("Capacity must be positive", Constants.MSG_CAPACITY_POSITIVE);
    }

    @Test
    void testPrivateConstructor() {
        assertThrows(IllegalStateException.class, Constants::new);
    }
}