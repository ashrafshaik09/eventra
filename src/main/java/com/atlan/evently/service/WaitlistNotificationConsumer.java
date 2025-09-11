package com.atlan.evently.service;

import com.atlan.evently.dto.events.WaitlistNotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Enhanced Kafka consumer that processes waitlist notification events.
 * Sends emails, creates in-app notifications, and pushes real-time WebSocket notifications.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WaitlistNotificationConsumer {

    private final EmailService emailService;
    private final NotificationService notificationService;
    private final WebSocketNotificationService webSocketNotificationService;

    /**
     * Process waitlist notification events with triple delivery:
     * 1. In-app notification (database)
     * 2. Email notification  
     * 3. Real-time WebSocket notification
     * 
     * @param event The waitlist notification event
     * @param partition The Kafka partition (for debugging)
     * @param offset The message offset (for debugging)
     * @param acknowledgment Manual acknowledgment to ensure message is processed
     */
    @KafkaListener(
        topics = "waitlist-notification",
        groupId = "evently-consumer-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void handleWaitlistNotification(
            @Payload WaitlistNotificationEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        log.info("Processing waitlist notification - user: {}, event: {}, partition: {}, offset: {}", 
                event.getUserId(), event.getEventName(), partition, offset);

        try {
            // 1. Create in-app notification (most reliable)
            notificationService.createWaitlistNotification(event);
            
            // 2. Send real-time WebSocket notification (for immediate user experience)
            try {
                webSocketNotificationService.sendWaitlistNotification(event);
            } catch (Exception wsError) {
                log.debug("WebSocket notification failed for user {}: {}", 
                        event.getUserId(), wsError.getMessage());
            }
            
            // 3. Send email notification (can fail without affecting others)
            try {
                emailService.sendWaitlistNotificationEmail(event);
            } catch (Exception emailError) {
                log.warn("Email notification failed for user {}, but other notifications succeeded: {}", 
                        event.getUserId(), emailError.getMessage());
            }
            
            log.info("Successfully processed waitlist notification for user {} event {}", 
                    event.getUserId(), event.getEventName());
            
            // Acknowledge message processing success
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Failed to process waitlist notification for user {}: {}", 
                    event.getUserId(), e.getMessage(), e);
            
            // Don't acknowledge - message will be retried
            throw new RuntimeException("Failed to process waitlist notification", e);
        }
    }

    /**
     * Health check method to verify consumer is active.
     */
    public boolean isHealthy() {
        return true;
    }
}
