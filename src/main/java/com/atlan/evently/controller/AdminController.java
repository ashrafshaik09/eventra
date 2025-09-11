package com.atlan.evently.controller;

import com.atlan.evently.dto.AnalyticsResponse;
import com.atlan.evently.dto.EventRequest;
import com.atlan.evently.dto.EventResponse;
import com.atlan.evently.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/admin/events")
public class AdminController {

    private final AdminService adminService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventResponse> createEvent(@Valid @RequestBody EventRequest request) {
        EventResponse response = new EventResponse();
        EventResponse createdEvent = adminService.createEvent(
                request.getEventName(),
                request.getVenue(),
                request.getStartTime(),
                request.getCapacity()
        );
        response.setEventId(createdEvent.getId());
        response.setName(createdEvent.getName());
        response.setVenue(createdEvent.getVenue());
        response.setStartTime(createdEvent.getStartsAt());
        response.setCapacity(createdEvent.getCapacity());
        response.setAvailableSeats(createdEvent.getAvailableSeats());
        return ResponseEntity.status(201).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventResponse> updateEvent(@PathVariable String id,
                                                    @Valid @RequestBody EventRequest request) {
        // Placeholder: Update logic to be implemented in AdminService (Phase E)
        EventResponse response = new EventResponse();
        response.setEventId(id);
        response.setName(request.getEventName());
        response.setVenue(request.getVenue());
        response.setStartTime(request.getStartTime());
        response.setCapacity(request.getCapacity());
        response.setAvailableSeats(request.getCapacity()); // Placeholder logic
        return ResponseEntity.ok(response);
    }

    @GetMapping("/analytics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AnalyticsResponse> getAnalytics() {
        AnalyticsResponse response = new AnalyticsResponse();
        java.util.Map<String, Object> analytics = adminService.getAnalytics();
        response.setTotalBookings((Long) analytics.get("totalBookings"));
        response.setTotalCapacity((Long) analytics.get("totalCapacity"));
        response.setUtilizationPercentage((String) analytics.get("utilizationPercentage"));
        return ResponseEntity.ok(response);
    }
}