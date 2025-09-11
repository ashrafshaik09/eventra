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
import java.util.Optional;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User user;
    private Event event;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id("1")
                .email("user@example.com")
                .name("John Doe")
                .createdAt(ZonedDateTime.now())
                .build();
        event = Event.builder()
                .id("1")
                .name("Concert 2025")
                .venue("City Hall")
                .startsAt(ZonedDateTime.now().plusDays(1))
                .capacity(100)
                .availableSeats(100)
                .createdAt(ZonedDateTime.now())
                .version(1)
                .build();
        Booking booking = Booking.builder()
                .id("1")
                .user(user)
                .event(event)
                .quantity(2)
                .status("CONFIRMED")
                .createdAt(ZonedDateTime.now())
                .build();
        user.getBookings().add(booking);
        userRepository.save(user);
    }

    @Test
    void testFindByIdWithBookingsReturnsUserWithBookings() {
        Optional<User> foundUser = userRepository.findByIdWithBookings("1");
        assertTrue(foundUser.isPresent());
        assertEquals("John Doe", foundUser.get().getName());
        assertEquals(1, foundUser.get().getBookings().size());
        assertEquals("CONFIRMED", foundUser.get().getBookings().get(0).getStatus());
    }
}