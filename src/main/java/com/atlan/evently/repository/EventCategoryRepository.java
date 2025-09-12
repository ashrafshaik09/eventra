package com.atlan.evently.repository;

import com.atlan.evently.model.EventCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for EventCategory entity operations.
 * Supports category management and filtering for event organization.
 */
@Repository
public interface EventCategoryRepository extends JpaRepository<EventCategory, UUID> {

    /**
     * Find all active categories ordered by name
     */
    List<EventCategory> findByIsActiveTrueOrderByName();

    /**
     * Find category by name (case-insensitive)
     */
    Optional<EventCategory> findByNameIgnoreCase(String name);

    /**
     * Check if category name exists (case-insensitive)
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Find active categories with pagination
     */
    Page<EventCategory> findByIsActiveTrue(Pageable pageable);

    /**
     * Search categories by name containing text (case-insensitive)
     */
    @Query("SELECT c FROM EventCategory c WHERE c.isActive = true AND LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<EventCategory> findActiveByNameContaining(String searchTerm);

    /**
     * Get category usage statistics (count of events per category)
     */
    @Query("SELECT c.id, c.name, COUNT(e) as eventCount FROM EventCategory c LEFT JOIN c.events e GROUP BY c.id, c.name ORDER BY eventCount DESC")
    List<Object[]> getCategoryUsageStatistics();
}
