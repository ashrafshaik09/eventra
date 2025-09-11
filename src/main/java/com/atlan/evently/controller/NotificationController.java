package com.atlan.evently.controller;

import com.atlan.evently.model.Notification;
import com.atlan.evently.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notifications", description = "User notification management endpoints")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/users/{userId}")
    @Operation(
        summary = "Get user notifications",
        description = "Retrieve all notifications for a user with optional limit"
    )
    @ApiResponse(responseCode = "200", description = "Successfully retrieved notifications")
    @ApiResponse(responseCode = "400", description = "Invalid user ID format")
    public ResponseEntity<List<Notification>> getUserNotifications(
            @Parameter(description = "User ID") @PathVariable String userId,
            @Parameter(description = "Maximum number of notifications to return") 
            @RequestParam(defaultValue = "20") int limit) {
        
        List<Notification> notifications = notificationService.getUserNotifications(userId, limit);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/users/{userId}/unread")
    @Operation(
        summary = "Get unread notifications",
        description = "Retrieve only unread notifications for a user"
    )
    @ApiResponse(responseCode = "200", description = "Successfully retrieved unread notifications")
    public ResponseEntity<List<Notification>> getUnreadNotifications(
            @Parameter(description = "User ID") @PathVariable String userId) {
        
        List<Notification> notifications = notificationService.getUnreadNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/users/{userId}/count")
    @Operation(
        summary = "Get unread notification count",
        description = "Get the count of unread notifications for a user (useful for badges)"
    )
    @ApiResponse(responseCode = "200", description = "Successfully retrieved unread count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @Parameter(description = "User ID") @PathVariable String userId) {
        
        long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    @PutMapping("/{notificationId}/read")
    @Operation(
        summary = "Mark notification as read",
        description = "Mark a specific notification as read"
    )
    @ApiResponse(responseCode = "204", description = "Successfully marked as read")
    @ApiResponse(responseCode = "404", description = "Notification not found")
    public ResponseEntity<Void> markAsRead(
            @Parameter(description = "Notification ID") @PathVariable String notificationId) {
        
        notificationService.markAsRead(notificationId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/users/{userId}/read-all")
    @Operation(
        summary = "Mark all notifications as read",
        description = "Mark all notifications as read for a specific user"
    )
    @ApiResponse(responseCode = "204", description = "Successfully marked all as read")
    public ResponseEntity<Void> markAllAsRead(
            @Parameter(description = "User ID") @PathVariable String userId) {
        
        notificationService.markAllAsRead(userId);
        return ResponseEntity.noContent().build();
    }
}
