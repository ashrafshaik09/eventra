package com.atlan.evently.service;

import com.atlan.evently.dto.events.BookingCancelledEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.support.Acknowledgment;

import java.time.ZonedDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class BookingEventConsumerTest {

    private BookingEventConsumer consumer;
    private WaitlistService waitlistService;
    private Acknowledgment acknowledgment;

    @BeforeEach
    void setUp() {
        waitlistService = mock(WaitlistService.class);
        acknowledgment = mock(Acknowledgment.class);
        consumer = new BookingEventConsumer(waitlistService);
    }

    @Test
    void testHandleBookingCancelledSuccess() {
        // Arrange
        UUID eventId = UUID.randomUUID();
        BookingCancelledEvent event = new BookingCancelledEvent(
            "booking-123",
            "user-456", 
            eventId.toString(),
            2,
            ZonedDateTime.now(),
            "USER_CANCELLED"
        );

        doNothing().when(waitlistService).processAvailableSeat(any(UUID.class), anyInt());

        // Act
        consumer.handleBookingCancelled(event, 0, 123L, acknowledgment);

        // Assert
        ArgumentCaptor<UUID> eventIdCaptor = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<Integer> quantityCaptor = ArgumentCaptor.forClass(Integer.class);
        
        verify(waitlistService).processAvailableSeat(eventIdCaptor.capture(), quantityCaptor.capture());
        assertEquals(eventId, eventIdCaptor.getValue());
        assertEquals(2, quantityCaptor.getValue());
        
        verify(acknowledgment).acknowledge();
    }

    @Test
    void testHandleBookingCancelledWithInvalidEventId() {
        // Arrange
        BookingCancelledEvent event = new BookingCancelledEvent(
            "booking-123",
            "user-456",
            "invalid-uuid", // Invalid UUID format
            2,
            ZonedDateTime.now(),
            "USER_CANCELLED"
        );

        // Act
        consumer.handleBookingCancelled(event, 0, 123L, acknowledgment);

        // Assert
        verify(waitlistService, never()).processAvailableSeat(any(UUID.class), anyInt());
        verify(acknowledgment).acknowledge(); // Should acknowledge to prevent reprocessing
    }

    @Test
    void testHandleBookingCancelledWithServiceError() {
        // Arrange
        UUID eventId = UUID.randomUUID();
        BookingCancelledEvent event = new BookingCancelledEvent(
            "booking-123",
            "user-456",
            eventId.toString(),
            2,
            ZonedDateTime.now(),
            "USER_CANCELLED"
        );

        doThrow(new RuntimeException("Database error"))
                .when(waitlistService).processAvailableSeat(any(UUID.class), anyInt());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            consumer.handleBookingCancelled(event, 0, 123L, acknowledgment);
        });

        verify(waitlistService).processAvailableSeat(eventId, 2);
        verify(acknowledgment, never()).acknowledge(); // Should NOT acknowledge on error
    }

    @Test
    void testIsHealthy() {
        assertTrue(consumer.isHealthy());
    }
}
