package com.atlan.evently.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Entity representing user likes on events.
 * Each user can like an event only once (enforced by unique constraint).
 */
@Entity
@Table(name = "event_likes", 
       uniqueConstraints = @UniqueConstraint(name = "uk_event_likes_user_event", 
                                           columnNames = {"user_id", "event_id"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventLike {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private ZonedDateTime createdAt = ZonedDateTime.now();
}
