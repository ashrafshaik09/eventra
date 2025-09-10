package com.atlan.evently.repository;

import com.atlan.evently.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, String> {

    List<Booking> findByUserIdAndStatus(String userId, String status);

    List<Booking> findByUserId(String userId);
}