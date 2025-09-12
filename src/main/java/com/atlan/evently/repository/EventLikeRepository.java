package com.atlan.evently.repository;

import com.atlan.evently.model.EventLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for EventLike entity operations.
 * Handles user likes on events with unique constraint enforcement.
 */
@Repository
public interface EventLikeRepository extends JpaRepository<EventLike, UUID> {

    /**
     * Find like by user and event (unique constraint)
     */
    Optional<EventLike> findByUserIdAndEventId(UUID userId, UUID eventId);

    /**
     * Check if user has liked an event
     */
    boolean existsByUserIdAndEventId(UUID userId, UUID eventId);

    /**
     * Count likes for an event
     */
    long countByEventId(UUID eventId);

    /**
     * Get all likes for an event
     */
    List<EventLike> findByEventIdOrderByCreatedAtDesc(UUID eventId);

    /**
     * Get all likes by a user
     */
    List<EventLike> findByUserIdOrderByCreatedAtDesc(UUID userId);

    /**
     * Delete like by user and event
     */
    void deleteByUserIdAndEventId(UUID userId, UUID eventId);

    /**
     * Get most liked events (popular events by like count)
     */
    @Query("SELECT el.event.id, COUNT(el) as likeCount FROM EventLike el GROUP BY el.event.id ORDER BY likeCount DESC")
    List<Object[]> getMostLikedEvents(@Param("limit") int limit);

    /**
     * Get events liked by user with event details
     */
    @Query("SELECT el.event FROM EventLike el WHERE el.user.id = :userId ORDER BY el.createdAt DESC")
    List<com.atlan.evently.model.Event> getEventsLikedByUser(@Param("userId") UUID userId);
}
