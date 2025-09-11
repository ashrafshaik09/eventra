package com.atlan.evently.service;

import com.atlan.evently.dto.EventResponse;
import com.atlan.evently.exception.EventException;
import com.atlan.evently.mapper.EventMapper;
import com.atlan.evently.model.Event;
import com.atlan.evently.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
public class EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    @Transactional(readOnly = true)
    public Page<Event> getUpcomingEvents(Pageable pageable) {
        return eventRepository.findAllByStartsAtAfter(ZonedDateTime.now(), pageable);
    }

    /**
     * Cached event listing for high-frequency access
     * Cache key includes page parameters for proper pagination caching
     */
    @Cacheable(value = "events", key = "#pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort.toString()")
    @Transactional(readOnly = true)
    public Page<EventResponse> getUpcomingEventsAsDto(Pageable pageable) {
        log.debug("Cache miss - fetching events from database for page: {}", pageable.getPageNumber());
        return getUpcomingEvents(pageable)
                .map(eventMapper::toResponse);
    }

    /**
     * Cached event details for individual event access
     * High cache hit ratio expected for popular events
     */
    @Cacheable(value = "event-details", key = "#eventId")
    @Transactional(readOnly = true)
    public EventResponse getEventByIdAsDto(String eventId) {
        log.debug("Cache miss - fetching event details from database for eventId: {}", eventId);
        UUID uuid = parseUUID(eventId, "Event ID");
        Event event = eventRepository.findByIdAndStartsAtAfter(uuid, ZonedDateTime.now());
        if (event == null) {
            throw new EventException("Event not found or has already started", 
                    "EVENT_NOT_FOUND", 
                    "Event with ID " + eventId + " does not exist or is not upcoming");
        }
        return eventMapper.toResponse(event);
    }

    /**
     * Evict caches when event is updated
     * Ensures cache consistency after admin modifications
     */
    @CacheEvict(value = {"events", "event-details"}, allEntries = true)
    public void evictEventCaches() {
        log.info("Evicting all event caches due to event modification");
    }

    /**
     * Evict specific event from cache
     * Used when individual event is updated
     */
    @CacheEvict(value = "event-details", key = "#eventId")
    public void evictEventCache(String eventId) {
        log.info("Evicting event cache for eventId: {}", eventId);
    }

    private UUID parseUUID(String id, String fieldName) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid " + fieldName + " format: " + id);
        }
    }
}