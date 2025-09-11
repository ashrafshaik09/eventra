package com.atlan.evently.repository;

import com.atlan.evently.model.Waitlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WaitlistRepository extends JpaRepository<Waitlist, UUID> {

    // Check if user is already on waitlist for event
    @Query("SELECT w FROM Waitlist w WHERE w.user.id = :userId AND w.event.id = :eventId AND w.status = 'WAITING'")
    Optional<Waitlist> findActiveWaitlistEntry(@Param("userId") UUID userId, @Param("eventId") UUID eventId);

    // Get next person in line for an event
    @Query("SELECT w FROM Waitlist w WHERE w.event.id = :eventId AND w.status = 'WAITING' ORDER BY w.position ASC LIMIT 1")
    Optional<Waitlist> findNextInLine(@Param("eventId") UUID eventId);

    // Get waitlist position for user
    @Query("SELECT COUNT(w) FROM Waitlist w WHERE w.event.id = :eventId AND w.position < :position AND w.status = 'WAITING'")
    Long getPositionInQueue(@Param("eventId") UUID eventId, @Param("position") Integer position);

    // Get all waitlist entries for an event (for admin)
    @Query("SELECT w FROM Waitlist w WHERE w.event.id = :eventId ORDER BY w.position ASC")
    List<Waitlist> findByEventIdOrderByPosition(@Param("eventId") UUID eventId);

    // Get user's waitlist entries
    List<Waitlist> findByUserIdOrderByCreatedAtDesc(UUID userId);

    // Get expired notifications that need cleanup
    @Query("SELECT w FROM Waitlist w WHERE w.status = 'NOTIFIED' AND w.expiresAt < :now")
    List<Waitlist> findExpiredNotifications(@Param("now") ZonedDateTime now);

    // Get next available position for event
    @Query("SELECT COALESCE(MAX(w.position), 0) + 1 FROM Waitlist w WHERE w.event.id = :eventId")
    Integer getNextPosition(@Param("eventId") UUID eventId);

    // Atomic position adjustment after someone leaves queue
    @Modifying
    @Query("UPDATE Waitlist w SET w.position = w.position - 1 WHERE w.event.id = :eventId AND w.position > :removedPosition AND w.status = 'WAITING'")
    int adjustPositionsAfterRemoval(@Param("eventId") UUID eventId, @Param("removedPosition") Integer removedPosition);

    // Count people waiting for event
    @Query("SELECT COUNT(w) FROM Waitlist w WHERE w.event.id = :eventId AND w.status = 'WAITING'")
    Long countWaitingForEvent(@Param("eventId") UUID eventId);
}
