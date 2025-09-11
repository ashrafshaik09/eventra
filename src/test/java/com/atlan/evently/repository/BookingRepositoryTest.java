package com.atlan.evently.repository;

import com.atlan.evently.model.Booking;
import com.atlan.evently.model.Event;
import com.atlan.evently.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.ZonedDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class BookingRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    private User user;
    private Event event;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(UUID.randomUUID())
                .email("user@example.com")
                .name("John Doe")
                .createdAt(ZonedDateTime.now())
                .build();
        userRepository.save(user);

        event = Event.builder()
                .id(UUID.randomUUID())
                .name("Concert 2025")
                .venue("City Hall")
                .startsAt(ZonedDateTime.now().plusDays(1))
                .capacity(100)
                .availableSeats(100)
                .createdAt(ZonedDateTime.now())
                .version(1)
                .build();
        eventRepository.save(event);

        Booking booking1 = Booking.builder()
                .id(UUID.randomUUID())
                .user(user)
                .event(event)
                .quantity(2)
                .status("CONFIRMED")
                .createdAt(ZonedDateTime.now())
                .build();
        Booking booking2 = Booking.builder()
                .id(UUID.randomUUID())
                .user(user)
                .event(event)
                .quantity(1)
                .status("CANCELLED")
                .createdAt(ZonedDateTime.now())
                .build();
        bookingRepository.save(booking1);
        bookingRepository.save(booking2);
    }

    @Test
    void testFindByUserIdAndStatusReturnsFilteredBookings() {
        List<Booking> bookings = bookingRepository.findByUserIdAndStatus("1", "CONFIRMED");
        assertEquals(1, bookings.size());
        assertEquals("CONFIRMED", bookings.get(0).getStatus());
    }

    @Test
    void testFindByUserIdReturnsAllUserBookings() {
        List<Booking> bookings = bookingRepository.findByUserId("1");
        assertEquals(2, bookings.size());
    }
}
