package com.atlan.evently.controller;

import com.atlan.evently.dto.EventResponse;
import com.atlan.evently.service.EventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.ZonedDateTime;
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
        when(eventService.getUpcomingEvents(PageRequest.of(0, 10))).thenReturn(page);

        mockMvc.perform(get("/api/v1/events?page=0&size=10"))
                .andExpect(status().isOk());

        verify(eventService, times(1)).getUpcomingEvents(PageRequest.of(0, 10));
    }

    @Test
    void testGetEventByIdReturnsOk() throws Exception {
        EventResponse response = new EventResponse();
        response.setEventId("1");
        Page<EventResponse> page = new PageImpl<>(Collections.singletonList(response));
        when(eventService.getUpcomingEvents(Pageable.unpaged())).thenReturn(page);

        mockMvc.perform(get("/api/v1/events/1"))
                .andExpect(status().isOk());

        verify(eventService, times(1)).getUpcomingEvents(Pageable.unpaged());
    }

    @Test
    void testGetEventByIdReturnsNotFound() throws Exception {
        Page<EventResponse> page = new PageImpl<>(Collections.emptyList());
        when(eventService.getUpcomingEvents(Pageable.unpaged())).thenReturn(page);

        mockMvc.perform(get("/api/v1/events/999"))
                .andExpect(status().isNotFound());

        verify(eventService, times(1)).getUpcomingEvents(Pageable.unpaged());
    }
}