package com.atlan.evently.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "bookings", 
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "event_id"}), // Prevent duplicate bookings per user per event
        @UniqueConstraint(columnNames = {"idempotency_key"}) // Ensure idempotency key uniqueness
    },
    indexes = {
        @Index(name = "idx_bookings_user_status", columnList = "user_id, status"),
        @Index(name = "idx_bookings_event_status", columnList = "event_id, status"),
        @Index(name = "idx_bookings_created_at", columnList = "created_at")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

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

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "status", nullable = false)
    private String status;

    // Idempotency key to prevent duplicate bookings on retries
    @Column(name = "idempotency_key", unique = true)
    private String idempotencyKey;

    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    // Business logic to validate booking status
    @PrePersist
    @PreUpdate
    public void validateStatus() {
        if (status != null && !status.equals("CONFIRMED") && !status.equals("CANCELLED")) {
            throw new IllegalStateException("Status must be CONFIRMED or CANCELLED");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalStateException("Quantity must be positive");
        }
    }

    // Helper methods for status checks
    public boolean isConfirmed() {
        return "CONFIRMED".equals(status);
    }

    public boolean isCancelled() {
        return "CANCELLED".equals(status);
    }

    public void confirm() {
        this.status = "CONFIRMED";
    }

    public void cancel() {
        this.status = "CANCELLED";
    }
}