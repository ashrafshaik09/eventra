package com.atlan.evently.controller;

import com.atlan.evently.dto.AnalyticsResponse;
import com.atlan.evently.dto.BookingResponse;
import com.atlan.evently.dto.EventRequest;
import com.atlan.evently.dto.EventResponse;
import com.atlan.evently.dto.UserResponse;
import com.atlan.evently.dto.UserRegistrationRequest; // Fix import
import com.atlan.evently.service.AdminService;
import com.atlan.evently.service.BookingService;
import com.atlan.evently.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.ZonedDateTime;
import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AdminControllerTest {

    private MockMvc mockMvc;
    private AdminService adminService;
    private BookingService bookingService;
    private UserService userService;

    @BeforeEach
    void setUp() {
        adminService = mock(AdminService.class);
        bookingService = mock(BookingService.class);
        userService = mock(UserService.class);
        // Fix constructor call - add UserService parameter
        AdminController adminController = new AdminController(adminService, bookingService, userService);
        mockMvc = MockMvcBuilders.standaloneSetup(adminController).build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateEventReturnsCreated() throws Exception {
        EventRequest request = new EventRequest();
        request.setEventName("Concert 2025");
        request.setVenue("City Hall");
        request.setStartTime(ZonedDateTime.now().plusDays(1));
        request.setCapacity(100);
        EventResponse response = new EventResponse();
        response.setEventId("123e4567-e89b-12d3-a456-426614174000"); // Proper UUID string
        when(adminService.createEvent(any(EventRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/admin/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"eventName\":\"Concert 2025\",\"venue\":\"City Hall\",\"startTime\":\"2025-09-12T12:00:00Z\",\"capacity\":100}"))
                .andExpect(status().isCreated());

        verify(adminService, times(1)).createEvent(any(EventRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateEventReturnsOk() throws Exception {
        String eventId = "123e4567-e89b-12d3-a456-426614174000";
        EventResponse response = new EventResponse();
        response.setEventId(eventId);
        when(adminService.updateEvent(eq(eventId), any(EventRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/admin/events/" + eventId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"eventName\":\"Updated Concert\",\"venue\":\"New Venue\",\"startTime\":\"2025-09-13T12:00:00Z\",\"capacity\":150}"))
                .andExpect(status().isOk());

        verify(adminService, times(1)).updateEvent(eq(eventId), any(EventRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAnalyticsReturnsOk() throws Exception {
        AnalyticsResponse response = new AnalyticsResponse();
        response.setTotalBookings(50L);
        response.setTotalCapacity(100L);
        response.setUtilizationPercentage("50.00");
        when(adminService.getAnalytics()).thenReturn(response);

        mockMvc.perform(get("/api/v1/admin/events/analytics"))
                .andExpect(status().isOk());

        verify(adminService, times(1)).getAnalytics();
    }

    @Test
    void testUnauthorizedAccessReturnsForbidden() throws Exception {
        mockMvc.perform(post("/api/v1/admin/events"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllBookingsReturnsOk() throws Exception {
        when(adminService.getAllBookings(null, null)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/admin/bookings"))
                .andExpect(status().isOk());

        verify(adminService, times(1)).getAllBookings(null, null);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllBookingsWithFiltersReturnsOk() throws Exception {
        String eventId = "123e4567-e89b-12d3-a456-426614174000";
        String status = "CONFIRMED";

        when(adminService.getAllBookings(status, eventId)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/admin/bookings")
                .param("status", status)
                .param("eventId", eventId))
                .andExpect(status().isOk());

        verify(adminService, times(1)).getAllBookings(status, eventId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testAdminCancelBookingReturnsNoContent() throws Exception {
        String bookingId = "123e4567-e89b-12d3-a456-426614174001";
        doNothing().when(bookingService).cancelBooking(bookingId);

        mockMvc.perform(delete("/api/v1/admin/bookings/" + bookingId))
                .andExpect(status().isNoContent());

        verify(bookingService, times(1)).cancelBooking(bookingId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetBookingDetailsReturnsOk() throws Exception {
        String bookingId = "123e4567-e89b-12d3-a456-426614174001";
        BookingResponse mockResponse = new BookingResponse();
        mockResponse.setBookingId(bookingId);
        mockResponse.setBookingStatus("CONFIRMED");

        when(adminService.getBookingById(bookingId)).thenReturn(mockResponse);

        mockMvc.perform(get("/api/v1/admin/bookings/" + bookingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingId").value(bookingId))
                .andExpect(jsonPath("$.bookingStatus").value("CONFIRMED"));

        verify(adminService, times(1)).getBookingById(bookingId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testAdminGetUserBookingsReturnsOk() throws Exception {
        String userId = "123e4567-e89b-12d3-a456-426614174000";
        when(bookingService.getUserBookingsAsDto(userId, null)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/admin/bookings/users/" + userId))
                .andExpect(status().isOk());

        verify(bookingService, times(1)).getUserBookingsAsDto(userId, null);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllUsersReturnsOk() throws Exception {
        Page<UserResponse> mockPage = Page.empty();
        when(adminService.getAllUsers(any(Pageable.class), isNull(), isNull())).thenReturn(mockPage);

        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isOk());

        verify(adminService, times(1)).getAllUsers(any(Pageable.class), isNull(), isNull());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateUserReturnsCreated() throws Exception {
        UserResponse mockResponse = new UserResponse();
        mockResponse.setUserId("123e4567-e89b-12d3-a456-426614174000");
        mockResponse.setName("New User");
        mockResponse.setEmail("newuser@example.com");
        mockResponse.setRole("USER");
        
        when(adminService.createUser(any(UserRegistrationRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/admin/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"New User\",\"email\":\"newuser@example.com\",\"password\":\"password123\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New User"))
                .andExpect(jsonPath("$.email").value("newuser@example.com"));

        verify(adminService, times(1)).createUser(any(UserRegistrationRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testPromoteUserToAdminReturnsNoContent() throws Exception {
        String userId = "123e4567-e89b-12d3-a456-426614174000";
        doNothing().when(adminService).promoteUserToAdmin(userId);

        mockMvc.perform(put("/api/v1/admin/users/" + userId + "/promote"))
                .andExpect(status().isNoContent());

        verify(adminService, times(1)).promoteUserToAdmin(userId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeactivateUserReturnsNoContent() throws Exception {
        String userId = "123e4567-e89b-12d3-a456-426614174000";
        doNothing().when(adminService).deactivateUser(userId);

        mockMvc.perform(put("/api/v1/admin/users/" + userId + "/deactivate"))
                .andExpect(status().isNoContent());

        verify(adminService, times(1)).deactivateUser(userId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteUserReturnsNoContent() throws Exception {
        String userId = "123e4567-e89b-12d3-a456-426614174000";
        doNothing().when(adminService).deleteUser(userId);

        mockMvc.perform(delete("/api/v1/admin/users/" + userId))
                .andExpect(status().isNoContent());

        verify(adminService, times(1)).deleteUser(userId);
    }
}