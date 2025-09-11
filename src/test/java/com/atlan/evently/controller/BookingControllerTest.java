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

import static org.mockito.ArgumentMatchers.any;
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
        String userId = "123e4567-e89b-12d3-a456-426614174000";
        when(bookingService.getUserBookingsAsDto(userId, null)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/bookings/users/" + userId))
                .andExpect(status().isOk());

        verify(bookingService, times(1)).getUserBookingsAsDto(userId, null);
    }

    @Test
    void testCreateBookingReturnsCreated() throws Exception {
        BookingResponse mockResponse = new BookingResponse();
        mockResponse.setBookingId("123e4567-e89b-12d3-a456-426614174001");
        mockResponse.setUserId("123e4567-e89b-12d3-a456-426614174000");
        mockResponse.setEventId("123e4567-e89b-12d3-a456-426614174002");
        mockResponse.setQuantity(2);
        mockResponse.setBookingStatus("CONFIRMED");

        when(bookingService.createBooking(any(BookingRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"123e4567-e89b-12d3-a456-426614174000\",\"eventId\":\"123e4567-e89b-12d3-a456-426614174002\",\"quantity\":2}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bookingId").value("123e4567-e89b-12d3-a456-426614174001"))
                .andExpect(jsonPath("$.bookingStatus").value("CONFIRMED"));

        verify(bookingService, times(1)).createBooking(any(BookingRequest.class));
    }

    @Test
    void testCancelBookingReturnsNoContent() throws Exception {
        String bookingId = "123e4567-e89b-12d3-a456-426614174001";
        doNothing().when(bookingService).cancelBooking(bookingId);

        mockMvc.perform(delete("/api/v1/bookings/" + bookingId))
                .andExpect(status().isNoContent());

        verify(bookingService, times(1)).cancelBooking(bookingId);
    }
}