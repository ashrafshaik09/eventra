package com.atlan.evently.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BookingResponseTest {

    private BookingResponse bookingResponse;

    @BeforeEach
    void setUp() {
        bookingResponse = new BookingResponse();
    }

    @Test
    void testValidBookingResponse() {
        bookingResponse.setBookingId("1");
        bookingResponse.setUserId("1");
        bookingResponse.setEventId("1");
        bookingResponse.setQuantity(2);
        bookingResponse.setBookingStatus("CONFIRMED");

        assertEquals("1", bookingResponse.getBookingId());
        assertEquals("1", bookingResponse.getUserId());
        assertEquals("1", bookingResponse.getEventId());
        assertEquals(2, bookingResponse.getQuantity());
        assertEquals("CONFIRMED", bookingResponse.getBookingStatus());
    }

    @Test
    void testSettersAndGetters() {
        bookingResponse.setBookingId("2");
        bookingResponse.setUserId("2");
        bookingResponse.setEventId("2");
        bookingResponse.setQuantity(1);
        bookingResponse.setBookingStatus("CANCELLED");

        assertEquals("2", bookingResponse.getBookingId());
        assertEquals("2", bookingResponse.getUserId());
        assertEquals("2", bookingResponse.getEventId());
        assertEquals(1, bookingResponse.getQuantity());
        assertEquals("CANCELLED", bookingResponse.getBookingStatus());
    }
}