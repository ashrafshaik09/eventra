package com.atlan.evently.controller;

import com.atlan.evently.dto.BookingRequest;
import com.atlan.evently.dto.BookingResponse;
import com.atlan.evently.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class BookingControllerTest {

    private MockMvc mockMvc;
    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        bookingService = mock(BookingService.class);
        BookingController bookingController = new BookingController(bookingService);
        mockMvc = MockMvcBuilders.standaloneSetup(bookingController).build();
    }

    @Test
    void testGetUserBookingsReturnsOk() throws Exception {
        when(bookingService.getUserBookings("1", null)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/bookings/users/1"))
                .andExpect(status().isOk());

        verify(bookingService, times(1)).getUserBookings("1", null);
    }

    @Test
    void testCreateBookingReturnsCreated() throws Exception {
        BookingRequest request = new BookingRequest();
        request.setUserId("1");
        request.setEventId("1");
        request.setQuantity(2);

        mockMvc.perform(post("/api/v1/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"1\",\"eventId\":\"1\",\"quantity\":2}"))
                .andExpect(status().isCreated());

        // Note: Actual response mapping will be tested after BookingService implementation
    }

    @Test
    void testCancelBookingReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/bookings/1"))
                .andExpect(status().isNoContent());

        // Note: Actual cancellation logic will be tested after BookingService implementation
    }
}