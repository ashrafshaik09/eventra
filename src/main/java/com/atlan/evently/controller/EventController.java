package com.atlan.evently.controller;

import com.atlan.evently.dto.EventResponse;
import com.atlan.evently.service.EventService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/events")
public class EventController {

    private final EventService eventService;

    @GetMapping
    public ResponseEntity<Page<EventResponse>> getEvents(Pageable pageable) {
        Page<EventResponse> events = eventService.getUpcomingEvents(pageable)
                .map(event -> {
                    EventResponse response = new EventResponse();
                    response.setEventId(event.getId());
                    response.setName(event.getName());
                    response.setVenue(event.getVenue());
                    response.setStartTime(event.getStartsAt());
                    response.setCapacity(event.getCapacity());
                    response.setAvailableSeats(event.getAvailableSeats());
                    return response;
                });
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEventById(@PathVariable String id) {
        return eventService.getUpcomingEvents(Pageable.unpaged())
                .stream()
                .filter(event -> event.getId().equals(id))
                .findFirst()
                .map(event -> {
                    EventResponse response = new EventResponse();
                    response.setEventId(event.getId());
                    response.setName(event.getName());
                    response.setVenue(event.getVenue());
                    response.setStartTime(event.getStartsAt());
                    response.setCapacity(event.getCapacity());
                    response.setAvailableSeats(event.getAvailableSeats());
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}