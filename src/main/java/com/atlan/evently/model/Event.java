package com.atlan.evently.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Enhanced Event entity with comprehensive event management features.
 * Supports both online and offline events with rich metadata.
 */
@Entity
@Table(name = "events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Event {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private UUID id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "venue", nullable = false, length = 255)
    private String venue;

    @Column(name = "tags", length = 500)
    private String tags; // Comma-separated tags for search/filtering

    @Column(name = "is_online", nullable = false)
    @Builder.Default
    private Boolean isOnline = false;

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    @Column(name = "starts_at", nullable = false)
    private ZonedDateTime startsAt;

    @Column(name = "ends_at")
    private ZonedDateTime endsAt;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @Column(name = "available_seats", nullable = false)
    private Integer availableSeats;

    @Column(name = "ticket_price", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal ticketPrice = BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private ZonedDateTime createdAt = ZonedDateTime.now();

    @Version
    @Column(name = "version")
    private Integer version;

    // Category relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private EventCategory category;

    // Relationships
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Booking> bookings = new ArrayList<>();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<EventLike> likes = new ArrayList<>();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<EventComment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Transaction> transactions = new ArrayList<>();

    // Business methods
    public boolean isActive() {
        return startsAt.isAfter(ZonedDateTime.now());
    }

    public boolean isSoldOut() {
        return availableSeats <= 0;
    }

    public boolean isLive() {
        ZonedDateTime now = ZonedDateTime.now();
        return now.isAfter(startsAt) && (endsAt == null || now.isBefore(endsAt));
    }

    public boolean hasEnded() {
        return endsAt != null && ZonedDateTime.now().isAfter(endsAt);
    }

    public int getBookedSeats() {
        return capacity - availableSeats;
    }

    public double getUtilizationPercentage() {
        return capacity > 0 ? ((double) getBookedSeats() / capacity) * 100.0 : 0.0;
    }

    // Helper methods for tags
    public List<String> getTagsList() {
        if (tags == null || tags.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return List.of(tags.split(","));
    }

    public void setTagsList(List<String> tagsList) {
        this.tags = tagsList != null ? String.join(",", tagsList) : null;
    }

    // Like count helper
    public long getLikeCount() {
        return likes != null ? likes.size() : 0;
    }

    // Comment count helper
    public long getCommentCount() {
        return comments != null ? comments.size() : 0;
    }
}