package com.atlan.evently.repository;

import com.atlan.evently.model.Booking;
import com.atlan.evently.model.Event;
import com.atlan.evently.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;

import java.time.ZonedDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
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
        // Create and save user first
        user = User.builder()
                .email("user@example.com")
                .name("John Doe")
                .passwordHash("hashedPassword123")
                .role(User.UserRole.USER)
                .isActive(true)
                .createdAt(ZonedDateTime.now())
                .build();
        user = userRepository.save(user);

        // Create and save event
        event = Event.builder()
                .name("Concert 2025")
                .venue("City Hall")
                .startsAt(ZonedDateTime.now().plusDays(1))
                .capacity(100)
                .availableSeats(100)
                .createdAt(ZonedDateTime.now())
                .version(1)
                .build();
        event = eventRepository.save(event);

        // Create bookings
        Booking booking1 = Booking.builder()
                .user(user)
                .event(event)
                .quantity(2)
                .status("CONFIRMED")
                .idempotencyKey("test-key-1")
                .createdAt(ZonedDateTime.now())
                .build();
        
        Booking booking2 = Booking.builder()
                .user(user)
                .event(event)
                .quantity(1)
                .status("CANCELLED")
                .idempotencyKey("test-key-2")
                .createdAt(ZonedDateTime.now().minusHours(1))
                .build();
        
        bookingRepository.save(booking1);
        bookingRepository.save(booking2);
    }

    @Test
    void testFindByUserIdAndStatusReturnsFilteredBookings() {
        List<Booking> confirmedBookings = bookingRepository.findByUserIdAndStatus(user.getId(), "CONFIRMED");
        assertEquals(1, confirmedBookings.size());
        assertEquals("CONFIRMED", confirmedBookings.get(0).getStatus());
        assertEquals(2, confirmedBookings.get(0).getQuantity());
    }

    @Test
    void testFindByUserIdReturnsAllUserBookings() {
        List<Booking> allBookings = bookingRepository.findByUserId(user.getId());
        assertEquals(2, allBookings.size());
    }

    @Test
    void testFindByIdempotencyKeyReturnsCorrectBooking() {
        Optional<Booking> booking = bookingRepository.findByIdempotencyKey("test-key-1");
        assertTrue(booking.isPresent());
        assertEquals("CONFIRMED", booking.get().getStatus());
        assertEquals(2, booking.get().getQuantity());
    }

    @Test
    void testFindExistingBookingReturnsUserBookingForEvent() {
        Optional<Booking> existingBooking = bookingRepository.findExistingBooking(user.getId(), event.getId());
        assertTrue(existingBooking.isPresent());
        assertEquals("CONFIRMED", existingBooking.get().getStatus());
    }

    @Test
    void testReserveSeatsUpdatesAvailableSeats() {
        int initialSeats = event.getAvailableSeats();
        int reserveQuantity = 5;
        
        int rowsUpdated = bookingRepository.reserveSeats(event.getId(), reserveQuantity);
        
        assertEquals(1, rowsUpdated); // One row should be updated
        
        // Refresh event to get updated state
        Event updatedEvent = eventRepository.findById(event.getId()).orElseThrow();
        assertEquals(initialSeats - reserveQuantity, updatedEvent.getAvailableSeats());
    }

    @Test
    void testReserveSeatsFailsWhenInsufficientSeats() {
        int excessiveQuantity = event.getAvailableSeats() + 10;
        
        int rowsUpdated = bookingRepository.reserveSeats(event.getId(), excessiveQuantity);
        
        assertEquals(0, rowsUpdated); // No rows should be updated
        
        // Event should remain unchanged
        Event unchangedEvent = eventRepository.findById(event.getId()).orElseThrow();
        assertEquals(100, unchangedEvent.getAvailableSeats());
    }

    @Test
    void testRestoreSeatsIncreasesAvailableSeats() {
        int initialSeats = event.getAvailableSeats();
        int restoreQuantity = 3;
        
        int rowsUpdated = bookingRepository.restoreSeats(event.getId(), restoreQuantity);
        
        assertEquals(1, rowsUpdated); // One row should be updated
        
        // Refresh event to get updated state
        Event updatedEvent = eventRepository.findById(event.getId()).orElseThrow();
        assertEquals(initialSeats + restoreQuantity, updatedEvent.getAvailableSeats());
    }

    @Test
    void testCountConfirmedBookingsByEventId() {
        long confirmedCount = bookingRepository.countConfirmedBookingsByEventId(event.getId());
        assertEquals(1, confirmedCount); // Only one CONFIRMED booking
    }
}
