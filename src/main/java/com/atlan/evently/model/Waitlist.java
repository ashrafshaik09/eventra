package com.atlan.evently.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "waitlist", 
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "event_id"}) // Prevent duplicate waitlist entries
    },
    indexes = {
        @Index(name = "idx_waitlist_event_position", columnList = "event_id, position"),
        @Index(name = "idx_waitlist_status", columnList = "status"),
        @Index(name = "idx_waitlist_expires_at", columnList = "expires_at")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Waitlist {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "position", nullable = false)
    private Integer position; // Position in queue (1 = first)

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private WaitlistStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @Column(name = "notified_at")
    private ZonedDateTime notifiedAt; // When user was notified of available seat

    @Column(name = "expires_at")
    private ZonedDateTime expiresAt; // When their booking window expires (10 minutes from notification)

    public enum WaitlistStatus {
        WAITING,     // In queue waiting
        NOTIFIED,    // Has been notified and has booking window
        EXPIRED,     // Booking window expired
        CONVERTED    // Successfully converted to booking
    }

    // Helper methods
    public boolean isWaiting() {
        return WaitlistStatus.WAITING.equals(status);
    }

    public boolean isNotified() {
        return WaitlistStatus.NOTIFIED.equals(status);
    }

    public boolean hasExpired() {
        return WaitlistStatus.EXPIRED.equals(status) || 
               (expiresAt != null && ZonedDateTime.now().isAfter(expiresAt));
    }

    public void notifyUser(int bookingWindowMinutes) {
        this.status = WaitlistStatus.NOTIFIED;
        this.notifiedAt = ZonedDateTime.now();
        this.expiresAt = ZonedDateTime.now().plusMinutes(bookingWindowMinutes);
    }

    public void expire() {
        this.status = WaitlistStatus.EXPIRED;
    }

    public void convert() {
        this.status = WaitlistStatus.CONVERTED;
    }
}
