package com.atlan.evently.service;

import com.atlan.evently.dto.events.BookingCancelledEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Kafka consumer that processes booking-related events.
 * Handles booking cancellations and triggers waitlist processing.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BookingEventConsumer {

    private final WaitlistService waitlistService;

    /**
     * Process booking cancellation events and trigger waitlist notifications.
     * This consumer listens to the booking-cancelled topic and notifies
     * the next person in the waitlist when seats become available.
     * 
     * @param event The booking cancellation event
     * @param partition The Kafka partition (for debugging)
     * @param offset The message offset (for debugging)
     * @param acknowledgment Manual acknowledgment to ensure message is processed
     */
    @KafkaListener(
        topics = "booking-cancelled",
        groupId = "evently-consumer-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void handleBookingCancelled(
            @Payload BookingCancelledEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        log.info("Processing booking cancellation event - booking: {}, event: {}, quantity: {}, partition: {}, offset: {}", 
                event.getBookingId(), event.getEventId(), event.getQuantity(), partition, offset);

        try {
            // Parse event ID and trigger waitlist processing
            UUID eventId = UUID.fromString(event.getEventId());
            
            // Process available seats for waitlisted users
            // This will notify the next person(s) in line via Kafka
            waitlistService.processAvailableSeat(eventId, event.getQuantity());
            
            log.info("Successfully processed booking cancellation for event {} - {} seats made available", 
                    event.getEventId(), event.getQuantity());
            
            // Acknowledge message processing success
            acknowledgment.acknowledge();
            
        } catch (IllegalArgumentException e) {
            log.error("Invalid event ID format in booking cancelled event: {}", event.getEventId(), e);
            // Acknowledge to prevent reprocessing of malformed messages
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Failed to process booking cancellation event for booking {}: {}", 
                    event.getBookingId(), e.getMessage(), e);
            
            // Don't acknowledge - message will be retried
            // Kafka will handle retry logic based on configuration
            throw new RuntimeException("Failed to process booking cancellation", e);
        }
    }

    /**
     * Health check method to verify consumer is active.
     * Can be exposed via actuator if needed.
     */
    public boolean isHealthy() {
        return true; // Simple implementation - could add more sophisticated health checks
    }
}
