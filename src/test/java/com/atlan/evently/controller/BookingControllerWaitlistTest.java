package com.atlan.evently.controller;

import com.atlan.evently.dto.BookingRequest;
import com.atlan.evently.dto.BookingResponse;
import com.atlan.evently.service.BookingService;
import com.atlan.evently.service.WaitlistService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class BookingControllerWaitlistTest {

    private MockMvc mockMvc;
    private BookingService bookingService;
    private WaitlistService waitlistService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        bookingService = mock(BookingService.class);
        waitlistService = mock(WaitlistService.class);
        BookingController controller = new BookingController(bookingService, waitlistService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testJoinWaitlistReturnsCreated() throws Exception {
        String eventId = "event-123";
        BookingController.WaitlistJoinRequest request = new BookingController.WaitlistJoinRequest("user-123");
        
        WaitlistService.WaitlistResponse mockResponse = new WaitlistService.WaitlistResponse(
            "waitlist-123", "user-123", eventId, 5, "WAITING", ZonedDateTime.now()
        );
        
        when(waitlistService.joinWaitlist("user-123", eventId)).thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/bookings/events/{eventId}/waitlist", eventId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpected(jsonPath("$.waitlistId").value("waitlist-123"))
                .andExpected(jsonPath("$.position").value(5))
                .andExpected(jsonPath("$.status").value("WAITING"));

        verify(waitlistService, times(1)).joinWaitlist("user-123", eventId);
    }

    @Test
    void testGetUserWaitlistEntriesReturnsOk() throws Exception {
        String userId = "user-123";
        
        WaitlistService.WaitlistResponse mockEntry = new WaitlistService.WaitlistResponse(
            "waitlist-123", userId, "event-123", 3, "WAITING", ZonedDateTime.now()
        );
        List<WaitlistService.WaitlistResponse> mockEntries = Collections.singletonList(mockEntry);
        
        when(waitlistService.getUserWaitlistEntries(userId)).thenReturn(mockEntries);

        mockMvc.perform(get("/api/v1/bookings/users/{userId}/waitlist", userId))
                .andExpect(status().isOk())
                .andExpected(jsonPath("$").isArray())
                .andExpected(jsonPath("$[0].waitlistId").value("waitlist-123"))
                .andExpected(jsonPath("$[0].position").value(3));

        verify(waitlistService, times(1)).getUserWaitlistEntries(userId);
    }

    @Test
    void testGetWaitlistPositionReturnsOk() throws Exception {
        String eventId = "event-123";
        String userId = "user-123";
        
        WaitlistService.WaitlistResponse mockResponse = new WaitlistService.WaitlistResponse(
            "waitlist-123", userId, eventId, 2, "WAITING", ZonedDateTime.now()
        );
        
        when(waitlistService.getWaitlistPosition(userId, eventId)).thenReturn(mockResponse);

        mockMvc.perform(get("/api/v1/bookings/events/{eventId}/waitlist/position", eventId)
                .param("userId", userId))
                .andExpect(status().isOk())
                .andExpected(jsonPath("$.position").value(2))
                .andExpected(jsonPath("$.status").value("WAITING"));

        verify(waitlistService, times(1)).getWaitlistPosition(userId, eventId);
    }

    @Test
    void testLeaveWaitlistReturnsNoContent() throws Exception {
        String waitlistId = "waitlist-123";
        
        doNothing().when(waitlistService).leaveWaitlist(waitlistId);

        mockMvc.perform(delete("/api/v1/bookings/waitlist/{waitlistId}", waitlistId))
                .andExpect(status().isNoContent());

        verify(waitlistService, times(1)).leaveWaitlist(waitlistId);
    }

    @Test
    void testConvertWaitlistToBookingReturnsCreated() throws Exception {
        String waitlistId = "waitlist-123";
        
        BookingRequest bookingRequest = new BookingRequest();
        bookingRequest.setUserId("user-123");
        bookingRequest.setEventId("event-123");
        bookingRequest.setQuantity(1);
        
        BookingResponse mockBookingResponse = new BookingResponse();
        mockBookingResponse.setBookingId("booking-456");
        mockBookingResponse.setUserId("user-123");
        mockBookingResponse.setEventId("event-123");
        mockBookingResponse.setQuantity(1);
        mockBookingResponse.setBookingStatus("CONFIRMED");
        
        when(bookingService.createBooking(any(BookingRequest.class))).thenReturn(mockBookingResponse);
        doNothing().when(waitlistService).markAsConverted(waitlistId);

        mockMvc.perform(post("/api/v1/bookings/waitlist/{waitlistId}/convert", waitlistId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bookingRequest)))
                .andExpected(status().isCreated())
                .andExpected(jsonPath("$.bookingId").value("booking-456"))
                .andExpected(jsonPath("$.bookingStatus").value("CONFIRMED"));

        verify(bookingService, times(1)).createBooking(any(BookingRequest.class));
        verify(waitlistService, times(1)).markAsConverted(waitlistId);
    }

    @Test
    void testExistingBookingEndpointsStillWork() throws Exception {
        // Test that existing endpoints are not broken
        String userId = "user-123";
        
        when(bookingService.getUserBookingsAsDto(userId, null)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/bookings/users/{userId}", userId))
                .andExpect(status().isOk())
                .andExpected(jsonPath("$").isArray());

        verify(bookingService, times(1)).getUserBookingsAsDto(userId, null);
    }
}
