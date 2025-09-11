package com.atlan.evently.repository;

import com.atlan.evently.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {

    List<Booking> findByUserIdAndStatus(UUID userId, String status);

    List<Booking> findByUserId(UUID userId);
}