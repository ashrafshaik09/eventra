package com.atlan.evently.service;

import com.atlan.evently.dto.AnalyticsResponse;
import com.atlan.evently.dto.EventRequest;
import com.atlan.evently.dto.EventResponse;
import com.atlan.evently.exception.EventException;
import com.atlan.evently.mapper.EventMapper;
import com.atlan.evently.model.Event;
import com.atlan.evently.repository.BookingRepository;
import com.atlan.evently.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class AdminService {

    private final EventRepository eventRepository;
    private final BookingRepository bookingRepository;
    private final EventMapper eventMapper;

    @Transactional
    public EventResponse createEvent(EventRequest request) {
        validateEventRequest(request);
        Event event = eventMapper.toEntity(request);
        Event savedEvent = eventRepository.save(event);
        return eventMapper.toResponse(savedEvent);
    }

    @Transactional
    public EventResponse updateEvent(String eventId, EventRequest request) {
        validateEventRequest(request);
        UUID uuid = parseUUID(eventId, "Event ID");
        Event existingEvent = eventRepository.findById(uuid)
                .orElseThrow(() -> new EventException("Event not found",
                        "EVENT_NOT_FOUND",
                        "Event with ID " + eventId + " does not exist"));

        // Update fields while preserving metadata
        existingEvent.setName(request.getEventName().trim());
        existingEvent.setVenue(request.getVenue().trim());
        existingEvent.setStartsAt(request.getStartTime());
        existingEvent.setCapacity(request.getCapacity());
        existingEvent.setAvailableSeats(request.getCapacity()); // Reset available seats for simplicity

        Event savedEvent = eventRepository.save(existingEvent);
        return eventMapper.toResponse(savedEvent);
    }

    @Transactional(readOnly = true)
    public AnalyticsResponse getAnalytics() {
        long totalBookings = bookingRepository.count();
        long totalCapacity = eventRepository.count() > 0 ?
                eventRepository.findAll().stream()
                        .mapToLong(Event::getCapacity)
                        .sum() : 0;

        double utilization = totalBookings > 0 && totalCapacity > 0 ?
                (double) totalBookings / totalCapacity * 100 : 0.0;

        AnalyticsResponse response = new AnalyticsResponse();
        response.setTotalBookings(totalBookings);
        response.setTotalCapacity(totalCapacity);
        response.setUtilizationPercentage(String.format("%.2f", utilization));
        return response;
    }

    private void validateEventRequest(EventRequest request) {
        if (request.getEventName() == null || request.getEventName().trim().isEmpty()) {
            throw new IllegalArgumentException("Event name is required");
        }
        if (request.getVenue() == null || request.getVenue().trim().isEmpty()) {
            throw new IllegalArgumentException("Venue is required");
        }
        if (request.getStartTime() == null) {
            throw new IllegalArgumentException("Start time is required");
        }
        if (request.getCapacity() == null || request.getCapacity() <= 0) {
            throw new IllegalArgumentException("Capacity must be positive");
        }
    }

    private UUID parseUUID(String id, String fieldName) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid " + fieldName + " format: " + id);
        }
    }

    // Legacy method for backward compatibility (will be removed in Phase D)
    @Transactional
    @Deprecated
    public Event createEvent(String name, String venue, ZonedDateTime startsAt, Integer capacity) {
        EventRequest request = new EventRequest();
        request.setEventName(name);
        request.setVenue(venue);
        request.setStartTime(startsAt);
        request.setCapacity(capacity);

        EventResponse response = createEvent(request);
        UUID uuid = parseUUID(response.getEventId(), "Event ID");
        return eventRepository.findById(uuid).orElse(null);
    }
}