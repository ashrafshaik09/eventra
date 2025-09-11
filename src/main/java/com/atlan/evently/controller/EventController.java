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
        Page<EventResponse> events = eventService.getUpcomingEventsAsDto(pageable);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEventById(@PathVariable String id) {
        EventResponse event = eventService.getEventByIdAsDto(id);
        return ResponseEntity.ok(event);
    }
}