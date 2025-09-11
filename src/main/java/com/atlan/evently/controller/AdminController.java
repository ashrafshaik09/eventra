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

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/admin/events")
public class AdminController {

    private final AdminService adminService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventResponse> createEvent(@Valid @RequestBody EventRequest request) {
        EventResponse response = adminService.createEvent(request);
        return ResponseEntity.status(201).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventResponse> updateEvent(@PathVariable String id,
                                                    @Valid @RequestBody EventRequest request) {
        EventResponse response = adminService.updateEvent(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/analytics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AnalyticsResponse> getAnalytics() {
        AnalyticsResponse response = adminService.getAnalytics();
        return ResponseEntity.ok(response);
    }
}