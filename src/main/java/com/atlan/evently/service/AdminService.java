package com.atlan.evently.service;

import com.atlan.evently.model.Event;
import com.atlan.evently.repository.BookingRepository;
import com.atlan.evently.repository.EventRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class AdminService {

    private final EventRepository eventRepository;
    private final BookingRepository bookingRepository;

    @Transactional
    public Event createEvent(String name, String venue, ZonedDateTime startsAt, Integer capacity) {
        if (name == null || name.trim().isEmpty() || venue == null || venue.trim().isEmpty() ||
                startsAt == null || capacity == null || capacity <= 0) {
            throw new IllegalArgumentException("Invalid event details: name, venue, start time, and capacity are required, capacity must be positive");
        }
        Event event = Event.builder()
                .name(name.trim())
                .venue(venue.trim())
                .startsAt(startsAt)
                .capacity(capacity)
                .availableSeats(capacity)
                .createdAt(ZonedDateTime.now())
                .version(1)
                .build();
        return eventRepository.save(event);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getAnalytics() {
        long totalBookings = bookingRepository.count();
        long totalCapacity = eventRepository.count() > 0 ? eventRepository.findAll().stream()
                .mapToLong(Event::getCapacity)
                .sum() : 0;
        double utilization = totalBookings > 0 && totalCapacity > 0 ? (double) totalBookings / totalCapacity * 100 : 0.0;

        Map<String, Object> analytics = new HashMap<>();
        analytics.put("totalBookings", totalBookings);
        analytics.put("totalCapacity", totalCapacity);
        analytics.put("utilizationPercentage", String.format("%.2f", utilization));
        return analytics;
    }
}