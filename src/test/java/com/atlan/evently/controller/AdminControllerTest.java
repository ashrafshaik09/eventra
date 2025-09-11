package com.atlan.evently.controller;

import com.atlan.evently.dto.AnalyticsResponse;
import com.atlan.evently.dto.EventRequest;
import com.atlan.evently.dto.EventResponse;
import com.atlan.evently.service.AdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.ZonedDateTime;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AdminControllerTest {

    private MockMvc mockMvc;
    private AdminService adminService;

    @BeforeEach
    void setUp() {
        adminService = mock(AdminService.class);
        AdminController adminController = new AdminController(adminService);
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
        response.setEventId("1");
        when(adminService.createEvent(anyString(), anyString(), any(ZonedDateTime.class), anyInt())).thenReturn(response);

        mockMvc.perform(post("/api/v1/admin/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"eventName\":\"Concert 2025\",\"venue\":\"City Hall\",\"startTime\":\"2025-09-12T12:00:00Z\",\"capacity\":100}"))
                .andExpect(status().isCreated());

        verify(adminService, times(1)).createEvent("Concert 2025", "City Hall", ZonedDateTime.parse("2025-09-12T12:00:00Z"), 100);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateEventReturnsOk() throws Exception {
        EventRequest request = new EventRequest();
        request.setEventName("Updated Concert");
        request.setVenue("New Venue");
        request.setStartTime(ZonedDateTime.now().plusDays(2));
        request.setCapacity(150);

        mockMvc.perform(put("/api/v1/admin/events/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"eventName\":\"Updated Concert\",\"venue\":\"New Venue\",\"startTime\":\"2025-09-13T12:00:00Z\",\"capacity\":150}"))
                .andExpect(status().isOk());

        // Note: Actual update logic will be tested after AdminService implementation
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAnalyticsReturnsOk() throws Exception {
        AnalyticsResponse response = new AnalyticsResponse();
        response.setTotalBookings(50L);
        response.setTotalCapacity(100L);
        response.setUtilizationPercentage("50.00");
        when(adminService.getAnalytics()).thenReturn(java.util.Map.of(
                "totalBookings", 50L,
                "totalCapacity", 100L,
                "utilizationPercentage", "50.00"
        ));

        mockMvc.perform(get("/api/v1/admin/events/analytics"))
                .andExpect(status().isOk());

        verify(adminService, times(1)).getAnalytics();
    }

    @Test
    void testUnauthorizedAccessReturnsForbidden() throws Exception {
        mockMvc.perform(post("/api/v1/admin/events"))
                .andExpect(status().isForbidden());
    }
}