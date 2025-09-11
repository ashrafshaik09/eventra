package com.atlan.evently.repository;

import com.atlan.evently.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {
    
    List<Booking> findByUserId(UUID userId);
    
    List<Booking> findByUserIdAndStatus(UUID userId, String status);

    // Admin-specific queries
    List<Booking> findByStatus(String status);
    
    List<Booking> findByEventId(UUID eventId);
    
    List<Booking> findByStatusAndEventId(String status, UUID eventId);

    // Custom queries for advanced admin analytics
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.event.id = :eventId AND b.status = 'CONFIRMED'")
    long countConfirmedBookingsByEventId(@Param("eventId") UUID eventId);

    @Query("SELECT b FROM Booking b JOIN FETCH b.user JOIN FETCH b.event ORDER BY b.createdAt DESC")
    List<Booking> findAllWithUserAndEvent();

    // Helper methods to convert String to UUID
    default List<Booking> findByUserId(String userId) {
        try {
            return findByUserId(UUID.fromString(userId));
        } catch (IllegalArgumentException e) {
            return List.of();
        }
    }

    default List<Booking> findByUserIdAndStatus(String userId, String status) {
        try {
            return findByUserIdAndStatus(UUID.fromString(userId), status);
        } catch (IllegalArgumentException e) {
            return List.of();
        }
    }

    default Optional<Booking> findById(String id) {
        try {
            return findById(UUID.fromString(id));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}