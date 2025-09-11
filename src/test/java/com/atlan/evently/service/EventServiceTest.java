package com.atlan.evently.service;

import com.atlan.evently.model.Event;
import com.atlan.evently.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import java.util.*;
import java.time.ZonedDateTime;
import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventService eventService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetUpcomingEventsReturnsPagedEvents() {
        ZonedDateTime now = ZonedDateTime.now();
        Event event = Event.builder()
                .id(UUID.randomUUID())
                .name("Concert 2025")
                .venue("City Hall")
                .startsAt(now.plusDays(1))
                .capacity(100)
                .availableSeats(100)
                .createdAt(now)
                .version(1)
                .build();
        Page<Event> page = new PageImpl<>(Collections.singletonList(event));
        when(eventRepository.findAllByStartsAtAfter(now, PageRequest.of(0, 10))).thenReturn(page);

        Page<Event> result = eventService.getUpcomingEvents(PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Concert 2025", result.getContent().get(0).getName());
        verify(eventRepository, times(1)).findAllByStartsAtAfter(now, PageRequest.of(0, 10));
    }
}