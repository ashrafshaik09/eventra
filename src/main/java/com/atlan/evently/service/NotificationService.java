package com.atlan.evently.service;

import com.atlan.evently.dto.events.WaitlistNotificationEvent;
import com.atlan.evently.exception.EventException;
import com.atlan.evently.model.Notification;
import com.atlan.evently.model.User;
import com.atlan.evently.repository.NotificationRepository;
import com.atlan.evently.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing in-app notifications for users.
 * Stores notifications in database for later retrieval via API.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    /**
     * Create in-app notification for waitlist seat availability
     */
    @Transactional
    public void createWaitlistNotification(WaitlistNotificationEvent event) {
        log.info("Creating in-app waitlist notification for user {}", event.getUserId());

        try {
            UUID userUuid = parseUUID(event.getUserId(), "User ID");
            User user = getUserById(userUuid);

            long minutesRemaining = java.time.Duration.between(ZonedDateTime.now(), event.getExpiresAt()).toMinutes();
            
            Notification notification = Notification.builder()
                    .user(user)
                    .type(Notification.NotificationType.WAITLIST_SEAT_AVAILABLE)
                    .title("Seat Available!")
                    .message(String.format("A seat is now available for '%s'. Book within %d minutes!", 
                            event.getEventName(), Math.max(1, (int) minutesRemaining)))
                    .actionUrl(event.getBookingUrl())
                    .isRead(false)
                    .createdAt(ZonedDateTime.now())
                    .expiresAt(event.getExpiresAt())
                    .metadata(String.format("{\"eventId\":\"%s\",\"eventName\":\"%s\",\"waitlistId\":\"%s\"}", 
                            event.getEventId(), event.getEventName(), event.getWaitlistId()))
                    .build();

            notificationRepository.save(notification);
            
            log.info("Successfully created in-app waitlist notification for user {}", event.getUserId());
            
        } catch (Exception e) {
            log.error("Failed to create waitlist notification for user {}: {}", 
                    event.getUserId(), e.getMessage(), e);
            // Don't throw - notification creation shouldn't fail the main flow
        }
    }

    /**
     * Create booking confirmation notification
     */
    @Transactional
    public void createBookingConfirmationNotification(String userId, String eventName, String bookingId) {
        log.info("Creating booking confirmation notification for user {}", userId);

        try {
            UUID userUuid = parseUUID(userId, "User ID");
            User user = getUserById(userUuid);

            Notification notification = Notification.builder()
                    .user(user)
                    .type(Notification.NotificationType.BOOKING_CONFIRMED)
                    .title("Booking Confirmed!")
                    .message(String.format("Your booking for '%s' has been confirmed. Booking ID: %s", 
                            eventName, bookingId))
                    .actionUrl("/bookings/" + bookingId)
                    .isRead(false)
                    .createdAt(ZonedDateTime.now())
                    .metadata(String.format("{\"bookingId\":\"%s\",\"eventName\":\"%s\"}", 
                            bookingId, eventName))
                    .build();

            notificationRepository.save(notification);
            log.info("Successfully created booking confirmation notification for user {}", userId);
            
        } catch (Exception e) {
            log.error("Failed to create booking confirmation notification for user {}: {}", 
                    userId, e.getMessage(), e);
        }
    }

    /**
     * Create booking cancellation notification
     */
    @Transactional
    public void createBookingCancellationNotification(String userId, String eventName, String bookingId) {
        log.info("Creating booking cancellation notification for user {}", userId);

        try {
            UUID userUuid = parseUUID(userId, "User ID");
            User user = getUserById(userUuid);

            Notification notification = Notification.builder()
                    .user(user)
                    .type(Notification.NotificationType.BOOKING_CANCELLED)
                    .title("Booking Cancelled")
                    .message(String.format("Your booking for '%s' has been cancelled. Booking ID: %s", 
                            eventName, bookingId))
                    .actionUrl("/events") // Redirect to events list
                    .isRead(false)
                    .createdAt(ZonedDateTime.now())
                    .metadata(String.format("{\"bookingId\":\"%s\",\"eventName\":\"%s\"}", 
                            bookingId, eventName))
                    .build();

            notificationRepository.save(notification);
            log.info("Successfully created booking cancellation notification for user {}", userId);
            
        } catch (Exception e) {
            log.error("Failed to create booking cancellation notification for user {}: {}", 
                    userId, e.getMessage(), e);
        }
    }

    /**
     * Get unread notifications for user
     */
    @Transactional(readOnly = true)
    public List<Notification> getUnreadNotifications(String userId) {
        UUID userUuid = parseUUID(userId, "User ID");
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userUuid);
    }

    /**
     * Get all notifications for user (paginated)
     */
    @Transactional(readOnly = true)
    public List<Notification> getUserNotifications(String userId, int limit) {
        UUID userUuid = parseUUID(userId, "User ID");
        return notificationRepository.findTop20ByUserIdOrderByCreatedAtDesc(userUuid)
                .stream()
                .limit(limit)
                .toList();
    }

    /**
     * Mark notification as read
     */
    @Transactional
    public void markAsRead(String notificationId) {
        UUID notificationUuid = parseUUID(notificationId, "Notification ID");
        
        Notification notification = notificationRepository.findById(notificationUuid)
                .orElseThrow(() -> new EventException("Notification not found", 
                        "NOTIFICATION_NOT_FOUND", 
                        "Notification with ID " + notificationId + " does not exist"));
        
        if (!notification.getIsRead()) {
            notification.markAsRead();
            notificationRepository.save(notification);
            log.info("Marked notification {} as read for user {}", notificationId, notification.getUser().getId());
        }
    }

    /**
     * Mark all notifications as read for user
     */
    @Transactional
    public void markAllAsRead(String userId) {
        UUID userUuid = parseUUID(userId, "User ID");
        List<Notification> unreadNotifications = notificationRepository
                .findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userUuid);
        
        unreadNotifications.forEach(Notification::markAsRead);
        notificationRepository.saveAll(unreadNotifications);
        
        log.info("Marked {} notifications as read for user {}", unreadNotifications.size(), userId);
    }

    /**
     * Get notification count for user
     */
    @Transactional(readOnly = true)
    public long getUnreadCount(String userId) {
        UUID userUuid = parseUUID(userId, "User ID");
        return notificationRepository.countByUserIdAndIsReadFalse(userUuid);
    }

    /**
     * Cleanup expired notifications (scheduled job - every hour)
     */
    @Scheduled(fixedRate = 3600000) // 1 hour
    @Transactional
    public void cleanupExpiredNotifications() {
        int deletedCount = notificationRepository.deleteByExpiresAtBefore(ZonedDateTime.now());
        if (deletedCount > 0) {
            log.info("Cleaned up {} expired notifications", deletedCount);
        }
    }

    // ========== UTILITY METHODS ==========

    private User getUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EventException("User not found", 
                        "USER_NOT_FOUND", 
                        "User with ID " + userId + " does not exist"));
    }

    private UUID parseUUID(String id, String fieldName) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid " + fieldName + " format: " + id);
        }
    }
}
