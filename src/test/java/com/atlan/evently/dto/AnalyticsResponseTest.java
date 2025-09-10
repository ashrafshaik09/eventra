package com.atlan.evently.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AnalyticsResponseTest {

    private AnalyticsResponse analyticsResponse;

    @BeforeEach
    void setUp() {
        analyticsResponse = new AnalyticsResponse();
    }

    @Test
    void testValidAnalyticsResponse() {
        analyticsResponse.setTotalBookings(50L);
        analyticsResponse.setTotalCapacity(100L);
        analyticsResponse.setUtilizationPercentage("50.00");

        assertEquals(50L, analyticsResponse.getTotalBookings());
        assertEquals(100L, analyticsResponse.getTotalCapacity());
        assertEquals("50.00", analyticsResponse.getUtilizationPercentage());
    }

    @Test
    void testSettersAndGetters() {
        analyticsResponse.setTotalBookings(75L);
        analyticsResponse.setTotalCapacity(150L);
        analyticsResponse.setUtilizationPercentage("50.00");

        assertEquals(75L, analyticsResponse.getTotalBookings());
        assertEquals(150L, analyticsResponse.getTotalCapacity());
        assertEquals("50.00", analyticsResponse.getUtilizationPercentage());
    }
}