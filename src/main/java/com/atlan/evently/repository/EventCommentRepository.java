package com.atlan.evently.repository;

import com.atlan.evently.model.EventComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for EventComment entity operations.
 * Supports hierarchical comments with parent-child relationships.
 */
@Repository
public interface EventCommentRepository extends JpaRepository<EventComment, UUID> {

    /**
     * Find top-level comments for an event (no parent)
     */
    Page<EventComment> findByEventIdAndParentCommentIsNullOrderByCreatedAtDesc(UUID eventId, Pageable pageable);

    /**
     * Find replies to a specific comment
     */
    List<EventComment> findByParentCommentIdOrderByCreatedAtAsc(UUID parentCommentId);

    /**
     * Count comments for an event (including replies)
     */
    long countByEventId(UUID eventId);

    /**
     * Count top-level comments only
     */
    long countByEventIdAndParentCommentIsNull(UUID eventId);

    /**
     * Find comments by user
     */
    Page<EventComment> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    /**
     * Get recent comments across all events (for admin monitoring)
     */
    @Query("SELECT c FROM EventComment c ORDER BY c.createdAt DESC")
    List<EventComment> findRecentComments(Pageable pageable);

    /**
     * Find comments by event with user details
     */
    @Query("SELECT c FROM EventComment c JOIN FETCH c.user WHERE c.event.id = :eventId AND c.parentComment IS NULL ORDER BY c.createdAt DESC")
    List<EventComment> findTopLevelCommentsWithUserByEventId(@Param("eventId") UUID eventId);

    /**
     * Get comment statistics for events
     */
    @Query("SELECT c.event.id, COUNT(c) as commentCount FROM EventComment c GROUP BY c.event.id ORDER BY commentCount DESC")
    List<Object[]> getEventCommentStatistics();
}
