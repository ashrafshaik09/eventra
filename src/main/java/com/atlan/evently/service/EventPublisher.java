package com.atlan.evently.service;

import com.atlan.evently.dto.events.BookingCancelledEvent;
import com.atlan.evently.dto.events.WaitlistNotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String BOOKING_CANCELLED_TOPIC = "booking-cancelled";
    private static final String WAITLIST_NOTIFICATION_TOPIC = "waitlist-notification";

    public void publishBookingCancelled(BookingCancelledEvent event) {
        log.info("Publishing booking cancelled event for booking: {}, event: {}", 
                event.getBookingId(), event.getEventId());
        
        kafkaTemplate.send(BOOKING_CANCELLED_TOPIC, event.getEventId(), event)
                .whenComplete((result, failure) -> {
                    if (failure != null) {
                        log.error("Failed to publish booking cancelled event: {}", failure.getMessage());
                    } else {
                        log.debug("Successfully published booking cancelled event to partition: {}, offset: {}",
                                result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
                    }
                });
    }

    public void publishWaitlistNotification(WaitlistNotificationEvent event) {
        log.info("Publishing waitlist notification for user: {}, event: {}", 
                event.getUserId(), event.getEventId());
        
        kafkaTemplate.send(WAITLIST_NOTIFICATION_TOPIC, event.getUserId(), event)
                .whenComplete((result, failure) -> {
                    if (failure != null) {
                        log.error("Failed to publish waitlist notification: {}", failure.getMessage());
                    } else {
                        log.debug("Successfully published waitlist notification to partition: {}, offset: {}",
                                result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
                    }
                });
    }
}
