package com.atlan.evently.repository;

import com.atlan.evently.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    @EntityGraph(attributePaths = "bookings")
    Optional<User> findByIdWithBookings(String id);
}