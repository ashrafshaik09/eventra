package com.atlan.evently.repository;

import com.atlan.evently.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    // Get unread notifications for user
    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(UUID userId);

    // Get recent notifications for user (read and unread)
    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId ORDER BY n.createdAt DESC LIMIT 20")
    List<Notification> findTop20ByUserIdOrderByCreatedAtDesc(@Param("userId") UUID userId);

    // Count unread notifications for user
    long countByUserIdAndIsReadFalse(UUID userId);

    // Cleanup expired notifications
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.expiresAt < :now")
    int deleteByExpiresAtBefore(@Param("now") ZonedDateTime now);

    // Get notifications by type for user
    List<Notification> findByUserIdAndTypeOrderByCreatedAtDesc(UUID userId, Notification.NotificationType type);
}
