package com.atlan.evently.repository;

import com.atlan.evently.model.Booking;
import com.atlan.evently.model.Event;
import com.atlan.evently.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private User user;
    private Event event;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .email("user@example.com")
                .name("John Doe")
                .createdAt(ZonedDateTime.now())
                .build();
        
        event = Event.builder()
                .name("Concert 2025")
                .venue("City Hall")
                .startsAt(ZonedDateTime.now().plusDays(1))
                .capacity(100)
                .availableSeats(100)
                .createdAt(ZonedDateTime.now())
                .version(1)
                .build();
        
        // Save user and event first
        user = userRepository.save(user);
        event = eventRepository.save(event);
        
        // Create booking
        Booking booking = Booking.builder()
                .user(user)
                .event(event)
                .quantity(2)
                .status("CONFIRMED")
                .createdAt(ZonedDateTime.now())
                .build();
        
        bookingRepository.save(booking);
    }

    @Test
    void testFindByIdWithBookingsReturnsUserWithBookings() {
        Optional<User> foundUser = userRepository.findById(user.getId());
        assertTrue(foundUser.isPresent());
        assertEquals("John Doe", foundUser.get().getName());
        assertEquals("user@example.com", foundUser.get().getEmail());
        assertNotNull(foundUser.get().getBookings());
    }

    @Test
    void testFindByEmailReturnsUser() {
        Optional<User> foundUser = userRepository.findByEmail("user@example.com");
        assertTrue(foundUser.isPresent());
        assertEquals("John Doe", foundUser.get().getName());
        assertEquals(user.getId(), foundUser.get().getId());
    }

    @Test
    void testExistsByEmailReturnsTrueForExistingEmail() {
        assertTrue(userRepository.existsByEmail("user@example.com"));
        assertFalse(userRepository.existsByEmail("nonexistent@example.com"));
    }
}