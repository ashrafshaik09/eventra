package com.atlan.evently.service;

import com.atlan.evently.model.Booking;
import com.atlan.evently.repository.BookingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;

    public BookingService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @Transactional(readOnly = true)
    public List<Booking> getUserBookings(String userId, String status) {
        if (status != null && !status.equals("CONFIRMED") && !status.equals("CANCELLED")) {
            throw new IllegalArgumentException("Status must be CONFIRMED or CANCELLED");
        }
        if (status != null) {
            return bookingRepository.findByUserIdAndStatus(userId, status);
        }
        return bookingRepository.findByUserId(userId);
    }
}