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

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
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

    // ========== ENHANCED EVENT QUERIES ==========

    /**
     * Find events by category with caching
     */
    @Cacheable(value = "events-by-category", key = "#categoryId + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public Page<EventResponse> getEventsByCategory(String categoryId, Pageable pageable) {
        log.debug("Cache miss - fetching events by category {} from database", categoryId);
        UUID categoryUuid = parseUUID(categoryId, "Category ID");
        return eventRepository.findByCategoryIdAndStartsAtAfterOrderByStartsAtAsc(
                categoryUuid, ZonedDateTime.now(), pageable)
                .map(eventMapper::toResponse);
    }

    /**
     * Search events by name, description, or tags
     */
    @Transactional(readOnly = true)
    public Page<EventResponse> searchEvents(String searchTerm, Pageable pageable) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getUpcomingEventsAsDto(pageable);
        }
        
        return eventRepository.searchEvents(searchTerm.trim(), ZonedDateTime.now(), pageable)
                .map(eventMapper::toResponse);
    }

    /**
     * Find events by price range
     */
    @Transactional(readOnly = true)
    public Page<EventResponse> getEventsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        return eventRepository.findByTicketPriceBetweenAndStartsAtAfterOrderByTicketPriceAsc(
                minPrice, maxPrice, ZonedDateTime.now(), pageable)
                .map(eventMapper::toResponse);
    }

    /**
     * Find online events only
     */
    @Cacheable(value = "online-events", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public Page<EventResponse> getOnlineEvents(Pageable pageable) {
        log.debug("Cache miss - fetching online events from database");
        return eventRepository.findByIsOnlineTrueAndStartsAtAfterOrderByStartsAtAsc(
                ZonedDateTime.now(), pageable)
                .map(eventMapper::toResponse);
    }

    /**
     * Find free events
     */
    @Cacheable(value = "free-events", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public Page<EventResponse> getFreeEvents(Pageable pageable) {
        log.debug("Cache miss - fetching free events from database");
        return eventRepository.findByTicketPriceEqualsAndStartsAtAfterOrderByStartsAtAsc(
                BigDecimal.ZERO, ZonedDateTime.now(), pageable)
                .map(eventMapper::toResponse);
    }

    /**
     * Find events with available seats
     */
    @Transactional(readOnly = true)
    public Page<EventResponse> getAvailableEvents(Integer minSeats, Pageable pageable) {
        return eventRepository.findByAvailableSeatsGreaterThanAndStartsAtAfterOrderByStartsAtAsc(
                minSeats != null ? minSeats : 0, ZonedDateTime.now(), pageable)
                .map(eventMapper::toResponse);
    }

    /**
     * Find sold out events
     */
    @Transactional(readOnly = true)
    public Page<EventResponse> getSoldOutEvents(Pageable pageable) {
        return eventRepository.findByAvailableSeatsEqualsAndStartsAtAfterOrderByStartsAtAsc(
                0, ZonedDateTime.now(), pageable)
                .map(eventMapper::toResponse);
    }

    /**
     * Find events by venue
     */
    @Transactional(readOnly = true)
    public Page<EventResponse> getEventsByVenue(String venue, Pageable pageable) {
        return eventRepository.findByVenueContainingIgnoreCaseAndStartsAtAfterOrderByStartsAtAsc(
                venue, ZonedDateTime.now(), pageable)
                .map(eventMapper::toResponse);
    }

    /**
     * Find events by multiple categories
     */
    @Transactional(readOnly = true)
    public Page<EventResponse> getEventsByCategories(List<String> categoryIds, Pageable pageable) {
        List<UUID> categoryUuids = categoryIds.stream()
                .map(id -> parseUUID(id, "Category ID"))
                .toList();
        
        return eventRepository.findByCategoryIdsAndStartsAtAfter(
                categoryUuids, ZonedDateTime.now(), pageable)
                .map(eventMapper::toResponse);
    }

    /**
     * Get most popular events with enhanced analytics
     */
    @Cacheable(value = "popular-events", key = "#limit")
    @Transactional(readOnly = true)
    public List<EventResponse> getMostPopularEvents(int limit, Pageable pageable) {
        log.debug("Cache miss - fetching popular events from database");
        return eventRepository.getMostPopularEvents(ZonedDateTime.now(), pageable)
                .stream()
                .limit(limit)
                .map(result -> {
                    Event event = (Event) result[0];
                    // Long bookingCount = (Long) result[1]; // Available for additional analytics
                    return eventMapper.toResponse(event);
                })
                .toList();
    }

    /**
     * Get high engagement events (likes + comments)
     */
    @Cacheable(value = "high-engagement-events", key = "#limit")
    @Transactional(readOnly = true)
    public List<EventResponse> getHighEngagementEvents(int limit, Pageable pageable) {
        log.debug("Cache miss - fetching high engagement events from database");
        return eventRepository.getHighEngagementEvents(ZonedDateTime.now(), pageable)
                .stream()
                .limit(limit)
                .map(result -> {
                    Event event = (Event) result[0];
                    // Long engagement = (Long) result[1]; // Available for sorting/filtering
                    return eventMapper.toResponse(event);
                })
                .toList();
    }

    /**
     * Evict caches when event is updated
     * Ensures cache consistency after admin modifications
     */
    @CacheEvict(value = {"events", "event-details", "events-by-category", "online-events", 
                        "free-events", "popular-events", "high-engagement-events"}, allEntries = true)
    public void evictAllEventCaches() {
        log.info("Evicting all event caches due to event modification");
    }

    @CacheEvict(value = {"event-details", "events-by-category", "popular-events", 
                        "high-engagement-events"}, key = "#eventId")
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