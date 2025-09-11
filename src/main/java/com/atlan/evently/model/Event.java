package com.atlan.evently.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "events", indexes = {
    @Index(name = "idx_events_starts_at", columnList = "starts_at"),
    @Index(name = "idx_events_name", columnList = "name"),
    @Index(name = "idx_events_available_seats", columnList = "available_seats") // For concurrent booking queries
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID")
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "venue", nullable = false)
    private String venue;

    @Column(name = "starts_at", nullable = false)
    private ZonedDateTime startsAt;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @Column(name = "available_seats", nullable = false)
    private Integer availableSeats;

    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    // Optimistic locking for concurrency control
    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    // Business logic to ensure available_seats does not exceed capacity
    @PreUpdate
    @PrePersist
    public void validateAvailability() {
        if (availableSeats > capacity) {
            throw new IllegalStateException("Available seats cannot exceed capacity");
        }
        if (availableSeats < 0) {
            throw new IllegalStateException("Available seats cannot be negative");
        }
    }

    // Helper method to check if booking is possible
    public boolean canAccommodate(Integer requestedSeats) {
        return availableSeats != null && requestedSeats != null 
               && availableSeats >= requestedSeats && requestedSeats > 0;
    }

    // Helper method for atomic seat reservation
    public void reserveSeats(Integer quantity) {
        if (!canAccommodate(quantity)) {
            throw new IllegalStateException(
                String.format("Cannot reserve %d seats. Only %d available.", quantity, availableSeats)
            );
        }
        this.availableSeats -= quantity;
    }

    // Helper method for seat restoration
    public void restoreSeats(Integer quantity) {
        this.availableSeats += quantity;
        if (this.availableSeats > capacity) {
            this.availableSeats = capacity; // Safety cap
        }
    }
}