package com.atlan.evently.service;

import com.atlan.evently.dto.EventResponse;
import com.atlan.evently.exception.EventException;
import com.atlan.evently.mapper.EventMapper;
import com.atlan.evently.model.Event;
import com.atlan.evently.repository.EventRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    @Transactional(readOnly = true)
    public Page<Event> getUpcomingEvents(Pageable pageable) {
        return eventRepository.findAllByStartsAtAfter(ZonedDateTime.now(), pageable);
    }

    @Transactional(readOnly = true)
    public Page<EventResponse> getUpcomingEventsAsDto(Pageable pageable) {
        return getUpcomingEvents(pageable)
                .map(eventMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public EventResponse getEventByIdAsDto(String eventId) {
        UUID uuid = parseUUID(eventId, "Event ID");
        Event event = eventRepository.findByIdAndStartsAtAfter(uuid, ZonedDateTime.now());
        if (event == null) {
            throw new EventException("Event not found or has already started", 
                    "EVENT_NOT_FOUND", 
                    "Event with ID " + eventId + " does not exist or is not upcoming");
        }
        return eventMapper.toResponse(event);
    }

    private UUID parseUUID(String id, String fieldName) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid " + fieldName + " format: " + id);
        }
    }
}