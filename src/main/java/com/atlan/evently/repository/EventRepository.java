package com.atlan.evently.repository;

import com.atlan.evently.model.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {

    Page<Event> findAllByStartsAtAfter(ZonedDateTime startsAt, Pageable pageable);

    Event findByIdAndStartsAtAfter(UUID id, ZonedDateTime startsAt);

    // ========== ENHANCED QUERIES FOR NEW FEATURES ==========

    /**
     * Find events by category
     */
    Page<Event> findByCategoryIdAndStartsAtAfterOrderByStartsAtAsc(UUID categoryId, ZonedDateTime now, Pageable pageable);

    /**
     * Find online events
     */
    Page<Event> findByIsOnlineTrueAndStartsAtAfterOrderByStartsAtAsc(ZonedDateTime now, Pageable pageable);

    /**
     * Find events by price range
     */
    Page<Event> findByTicketPriceBetweenAndStartsAtAfterOrderByTicketPriceAsc(
            BigDecimal minPrice, BigDecimal maxPrice, ZonedDateTime now, Pageable pageable);

    /**
     * Search events by name, description, or tags
     */
    @Query("SELECT e FROM Event e WHERE e.startsAt > :now AND " +
           "(LOWER(e.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(e.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(e.tags) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Event> searchEvents(@Param("searchTerm") String searchTerm, @Param("now") ZonedDateTime now, Pageable pageable);

    /**
     * Find events with available seats
     */
    Page<Event> findByAvailableSeatsGreaterThanAndStartsAtAfterOrderByStartsAtAsc(
            Integer minSeats, ZonedDateTime now, Pageable pageable);

    /**
     * Find sold out events
     */
    Page<Event> findByAvailableSeatsEqualsAndStartsAtAfterOrderByStartsAtAsc(
            Integer availableSeats, ZonedDateTime now, Pageable pageable);

    /**
     * Get most popular events by booking count
     */
    @Query("SELECT e, COUNT(b) as bookingCount FROM Event e LEFT JOIN e.bookings b " +
           "WHERE e.startsAt > :now GROUP BY e ORDER BY bookingCount DESC")
    List<Object[]> getMostPopularEvents(@Param("now") ZonedDateTime now, Pageable pageable);

    /**
     * Get events by multiple categories
     */
    @Query("SELECT e FROM Event e WHERE e.category.id IN :categoryIds AND e.startsAt > :now ORDER BY e.startsAt ASC")
    Page<Event> findByCategoryIdsAndStartsAtAfter(@Param("categoryIds") List<UUID> categoryIds, 
                                                   @Param("now") ZonedDateTime now, Pageable pageable);

    /**
     * Find events ending soon (for cleanup/archival)
     */
    List<Event> findByEndsAtBefore(ZonedDateTime endTime);

    /**
     * Get events with high engagement (likes + comments)
     */
    @Query("SELECT e, (COUNT(DISTINCT l) + COUNT(DISTINCT c)) as engagement FROM Event e " +
           "LEFT JOIN e.likes l LEFT JOIN e.comments c " +
           "WHERE e.startsAt > :now GROUP BY e ORDER BY engagement DESC")
    List<Object[]> getHighEngagementEvents(@Param("now") ZonedDateTime now, Pageable pageable);

    /**
     * Find free events
     */
    Page<Event> findByTicketPriceEqualsAndStartsAtAfterOrderByStartsAtAsc(
            BigDecimal price, ZonedDateTime now, Pageable pageable);

    /**
     * Get venue-based events
     */
    Page<Event> findByVenueContainingIgnoreCaseAndStartsAtAfterOrderByStartsAtAsc(
            String venue, ZonedDateTime now, Pageable pageable);
}