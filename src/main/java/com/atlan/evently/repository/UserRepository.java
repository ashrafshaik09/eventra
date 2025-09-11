package com.atlan.evently.repository;

import com.atlan.evently.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    @EntityGraph(attributePaths = "bookings")
    Optional<User> findById(UUID id);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    // Admin-specific queries
    Page<User> findByRole(User.UserRole role, Pageable pageable);
    
    Page<User> findByIsActive(Boolean isActive, Pageable pageable);
    
    Page<User> findByRoleAndIsActive(User.UserRole role, Boolean isActive, Pageable pageable);
    
    // Search functionality
    Page<User> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String nameQuery, String emailQuery, Pageable pageable);

    // Helper method to convert String to UUID
    default Optional<User> findById(String id) {
        try {
            return findById(UUID.fromString(id));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}