package com.atlan.evently.mapper;

import com.atlan.evently.dto.EventRequest;
import com.atlan.evently.dto.EventResponse;
import com.atlan.evently.model.Event;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EventMapperTest {

    private final EventMapper mapper = EventMapper.INSTANCE;

    @Test
    void testToResponseMapping() {
        Event event = new Event();
        event.setId("1");
        event.setName("Concert 2025");
        event.setVenue("City Hall");
        event.setStartsAt(ZonedDateTime.now().plusDays(1));
        event.setCapacity(100);
        event.setAvailableSeats(90);

        EventResponse response = mapper.toResponse(event);

        assertNotNull(response);
        assertEquals("1", response.getEventId());
        assertEquals("Concert 2025", response.getName());
        assertEquals("City Hall", response.getVenue());
        assertEquals(event.getStartsAt(), response.getStartTime());
        assertEquals(100, response.getCapacity());
        assertEquals(90, response.getAvailableSeats());
    }

    @Test
    void testToEntityMapping() {
        EventRequest request = new EventRequest();
        request.setEventName("Workshop 2025");
        request.setVenue("Community Center");
        request.setStartTime(ZonedDateTime.now().plusDays(2));
        request.setCapacity(50);

        Event entity = mapper.toEntity(request);

        assertNotNull(entity);
        assertEquals("Workshop 2025", entity.getName());
        assertEquals("Community Center", entity.getVenue());
        assertEquals(request.getStartTime(), entity.getStartsAt());
        assertEquals(50, entity.getCapacity());
        assertEquals(50, entity.getAvailableSeats());
        assertNotNull(entity.getCreatedAt());
        assertEquals(1, entity.getVersion());
    }
}