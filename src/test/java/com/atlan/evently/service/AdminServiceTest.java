package com.atlan.evently.service;

import com.atlan.evently.model.Event;
import com.atlan.evently.repository.BookingRepository;
import com.atlan.evently.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.ZonedDateTime;
import java.util.Collections;

import com.atlan.evently.dto.*;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class AdminServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private AdminService adminService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateEventWithValidDetails() {
        ZonedDateTime now = ZonedDateTime.now();
        Event event = Event.builder()
                .name("Concert 2025")
                .venue("City Hall")
                .startsAt(now.plusDays(1))
                .capacity(100)
                .availableSeats(100)
                .createdAt(now)
                .version(1)
                .build();
        when(eventRepository.save(any(Event.class))).thenReturn(event);

        Event result = adminService.createEvent("Concert 2025", "City Hall", now.plusDays(1), 100);

        assertNotNull(result);
        assertEquals("Concert 2025", result.getName());
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    void testCreateEventWithInvalidDetailsThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> adminService.createEvent("", "City Hall", ZonedDateTime.now(), 0));
    }

    @Test
    void testGetAnalyticsReturnsValidData() {
        when(bookingRepository.count()).thenReturn(50L);
        when(eventRepository.count()).thenReturn(2L);
        when(eventRepository.findAll()).thenReturn(Collections.singletonList(
                Event.builder().capacity(100).build()
        ));

        AnalyticsResponse result = adminService.getAnalytics();

        assertNotNull(result);
        assertEquals(50L, result.getTotalBookings());
        assertEquals(100L, result.getTotalCapacity());
        assertEquals("50.00", result.getUtilizationPercentage());
    }
}