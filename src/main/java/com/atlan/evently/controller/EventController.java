package com.atlan.evently.controller;

import com.atlan.evently.dto.EventResponse;
import com.atlan.evently.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/events")
@Tag(name = "Events", description = "Public event browsing endpoints")
public class EventController {

    private final EventService eventService;

    @GetMapping
    @Operation(
        summary = "Browse upcoming events",
        description = "Get a paginated list of all upcoming events with details (name, venue, time, capacity). Results are cached for performance."
    )
    @ApiResponse(responseCode = "200", description = "Successfully retrieved events")
    public ResponseEntity<Page<EventResponse>> getUpcomingEvents(
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        Page<EventResponse> events = eventService.getUpcomingEventsAsDto(pageable);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{eventId}")
    @Operation(
        summary = "Get event details",
        description = "Get detailed information about a specific event including capacity and availability"
    )
    @ApiResponse(responseCode = "200", description = "Successfully retrieved event details")
    @ApiResponse(responseCode = "404", description = "Event not found or has already started")
    public ResponseEntity<EventResponse> getEventById(
            @Parameter(description = "Event ID") @PathVariable String eventId) {
        EventResponse event = eventService.getEventByIdAsDto(eventId);
        return ResponseEntity.ok(event);
    }
}