package com.atlan.evently.service;

import com.atlan.evently.model.Booking;
import com.atlan.evently.repository.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetUserBookingsWithValidStatus() {
        List<Booking> bookings = Collections.singletonList(new Booking());
        when(bookingRepository.findByUserIdAndStatus("1", "CONFIRMED")).thenReturn(bookings);

        List<Booking> result = bookingService.getUserBookings("1", "CONFIRMED");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bookingRepository, times(1)).findByUserIdAndStatus("1", "CONFIRMED");
    }

    @Test
    void testGetUserBookingsWithoutStatus() {
        List<Booking> bookings = Collections.singletonList(new Booking());
        when(bookingRepository.findByUserId("1")).thenReturn(bookings);

        List<Booking> result = bookingService.getUserBookings("1", null);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bookingRepository, times(1)).findByUserId("1");
    }

    @Test
    void testGetUserBookingsWithInvalidStatusThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> bookingService.getUserBookings("1", "PENDING"));
    }
}