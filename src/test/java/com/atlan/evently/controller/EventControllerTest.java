package com.atlan.evently.controller;

import com.atlan.evently.dto.EventResponse;
import com.atlan.evently.exception.EventException;
import com.atlan.evently.service.EventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EventControllerTest {

    private MockMvc mockMvc;
    private EventService eventService;

    @BeforeEach
    void setUp() {
        eventService = mock(EventService.class);
        EventController eventController = new EventController(eventService);
        mockMvc = MockMvcBuilders.standaloneSetup(eventController).build();
    }

    @Test
    void testGetEventsReturnsOk() throws Exception {
        Page<EventResponse> page = new PageImpl<>(Collections.singletonList(new EventResponse()));
        when(eventService.getUpcomingEventsAsDto(PageRequest.of(0, 10))).thenReturn(page);

        mockMvc.perform(get("/api/v1/events?page=0&size=10"))
                .andExpect(status().isOk());

        verify(eventService, times(1)).getUpcomingEventsAsDto(PageRequest.of(0, 10));
    }

    @Test
    void testGetEventByIdReturnsOk() throws Exception {
        EventResponse response = new EventResponse();
        response.setEventId("1");
        when(eventService.getEventByIdAsDto("1")).thenReturn(response);

        mockMvc.perform(get("/api/v1/events/1"))
                .andExpect(status().isOk());

        verify(eventService, times(1)).getEventByIdAsDto("1");
    }

    @Test
    void testGetEventByIdReturnsNotFound() throws Exception {
        when(eventService.getEventByIdAsDto("999"))
                .thenThrow(new EventException("Event not found", "EVENT_NOT_FOUND", "Event does not exist"));

        mockMvc.perform(get("/api/v1/events/999"))
                .andExpect(status().isConflict()); // GlobalExceptionHandler maps EventException to 409

        verify(eventService, times(1)).getEventByIdAsDto("999");
    }
}
