package com.atlan.evently.service;

import com.atlan.evently.dto.events.WaitlistNotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service for sending real-time notifications via WebSocket.
 * Pushes notifications to connected browser clients instantly.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Send real-time waitlist notification to user's browser
     */
    public void sendWaitlistNotification(WaitlistNotificationEvent event) {
        log.info("Sending real-time waitlist notification to user {}", event.getUserId());

        try {
            Map<String, Object> notification = Map.of(
                "type", "WAITLIST_SEAT_AVAILABLE",
                "title", "Seat Available!",
                "message", String.format("A seat is now available for '%s'. Book within %d minutes!", 
                        event.getEventName(), 
                        java.time.Duration.between(java.time.ZonedDateTime.now(), event.getExpiresAt()).toMinutes()),
                "eventId", event.getEventId(),
                "eventName", event.getEventName(),
                "bookingUrl", event.getBookingUrl(),
                "expiresAt", event.getExpiresAt().toString(),
                "timestamp", java.time.ZonedDateTime.now().toString()
            );

            // Send to specific user (requires user to be connected to /user/{userId}/notifications)
            messagingTemplate.convertAndSendToUser(
                event.getUserId(), 
                "/notifications", 
                notification
            );

            log.info("Successfully sent real-time waitlist notification to user {}", event.getUserId());
            
        } catch (Exception e) {
            log.error("Failed to send real-time waitlist notification to user {}: {}", 
                    event.getUserId(), e.getMessage(), e);
            // Don't throw - WebSocket failure shouldn't affect other notifications
        }
    }

    /**
     * Send real-time booking confirmation notification
     */
    public void sendBookingConfirmation(String userId, String eventName, String bookingId) {
        log.info("Sending real-time booking confirmation to user {}", userId);

        try {
            Map<String, Object> notification = Map.of(
                "type", "BOOKING_CONFIRMED",
                "title", "Booking Confirmed!",
                "message", String.format("Your booking for '%s' has been confirmed.", eventName),
                "bookingId", bookingId,
                "eventName", eventName,
                "timestamp", java.time.ZonedDateTime.now().toString()
            );

            messagingTemplate.convertAndSendToUser(userId, "/notifications", notification);
            log.info("Successfully sent real-time booking confirmation to user {}", userId);
            
        } catch (Exception e) {
            log.error("Failed to send real-time booking confirmation to user {}: {}", 
                    userId, e.getMessage(), e);
        }
    }

    /**
     * Send notification count update to user
     */
    public void sendNotificationCountUpdate(String userId, long unreadCount) {
        try {
            Map<String, Object> update = Map.of(
                "type", "COUNT_UPDATE",
                "unreadCount", unreadCount,
                "timestamp", java.time.ZonedDateTime.now().toString()
            );

            messagingTemplate.convertAndSendToUser(userId, "/notifications/count", update);
            
        } catch (Exception e) {
            log.error("Failed to send notification count update to user {}: {}", userId, e.getMessage());
        }
    }
}
