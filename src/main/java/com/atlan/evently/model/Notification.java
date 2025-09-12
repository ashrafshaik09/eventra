package com.atlan.evently.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Entity for storing in-app notifications for users.
 */
@Entity
@Table(name = "notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private NotificationType type;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "action_url", length = 500)
    private String actionUrl;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private ZonedDateTime createdAt = ZonedDateTime.now();

    @Column(name = "read_at")
    private ZonedDateTime readAt;

    @Column(name = "expires_at")
    private ZonedDateTime expiresAt;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata; // JSON string for additional data

    public enum NotificationType {
        WAITLIST_SEAT_AVAILABLE,
        BOOKING_CONFIRMED,
        BOOKING_CANCELLED,
        EVENT_REMINDER,
        SYSTEM_ANNOUNCEMENT
    }

    public void markAsRead() {
        this.isRead = true;
        this.readAt = ZonedDateTime.now();
    }

    public boolean isExpired() {
        return expiresAt != null && ZonedDateTime.now().isAfter(expiresAt);
    }
}
