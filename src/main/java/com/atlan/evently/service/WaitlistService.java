package com.atlan.evently.service;

import com.atlan.evently.dto.events.WaitlistNotificationEvent;
import com.atlan.evently.exception.BookingConflictException;
import com.atlan.evently.exception.DuplicateBookingException;
import com.atlan.evently.exception.EventException;
import com.atlan.evently.model.Event;
import com.atlan.evently.model.User;
import com.atlan.evently.model.Waitlist;
import com.atlan.evently.repository.EventRepository;
import com.atlan.evently.repository.UserRepository;
import com.atlan.evently.repository.WaitlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
public class WaitlistService {

    private final WaitlistRepository waitlistRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EventPublisher eventPublisher;

    @Value("${evently.waitlist.booking-window-minutes:10}")
    private int bookingWindowMinutes;

    @Value("${evently.waitlist.max-position:100}")
    private int maxWaitlistPosition;

    // ========== CORE WAITLIST OPERATIONS ==========

    /**
     * Join waitlist for a sold-out event
     * Implements FIFO queue with position tracking
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public WaitlistResponse joinWaitlist(String userId, String eventId) {
        log.info("User {} attempting to join waitlist for event {}", userId, eventId);

        UUID userUuid = parseUUID(userId, "User ID");
        UUID eventUuid = parseUUID(eventId, "Event ID");

        // Step 1: Validate entities exist
        User user = userRepository.findById(userUuid)
                .orElseThrow(() -> new EventException("User not found", 
                        "USER_NOT_FOUND", 
                        "User with ID " + userId + " does not exist"));

        Event event = eventRepository.findById(eventUuid)
                .orElseThrow(() -> new EventException("Event not found", 
                        "EVENT_NOT_FOUND", 
                        "Event with ID " + eventId + " does not exist"));

        // Step 2: Business validation
        if (event.getStartsAt().isBefore(ZonedDateTime.now())) {
            throw new EventException("Event has already started", 
                    "EVENT_STARTED", 
                    "Cannot join waitlist for past events");
        }

        if (event.getAvailableSeats() > 0) {
            throw new BookingConflictException(
                "Event has available seats", 
                String.format("Event has %d available seats. Book directly instead.", event.getAvailableSeats())
            );
        }

        // Step 3: Check if user is already on waitlist
        Optional<Waitlist> existingEntry = waitlistRepository.findActiveWaitlistEntry(userUuid, eventUuid);
        if (existingEntry.isPresent()) {
            Waitlist existing = existingEntry.get();
            long currentPosition = waitlistRepository.getPositionInQueue(eventUuid, existing.getPosition()) + 1;
            
            return new WaitlistResponse(
                existing.getId().toString(),
                userId,
                eventId,
                (int) currentPosition,
                existing.getStatus().toString(),
                existing.getCreatedAt()
            );
        }

        // Step 4: Get next position in queue
        Integer nextPosition = waitlistRepository.getNextPosition(eventUuid);
        
        if (nextPosition > maxWaitlistPosition) {
            throw new BookingConflictException(
                "Waitlist is full", 
                String.format("Maximum waitlist size (%d) reached for this event", maxWaitlistPosition)
            );
        }

        // Step 5: Create waitlist entry
        try {
            Waitlist waitlistEntry = Waitlist.builder()
                    .user(user)
                    .event(event)
                    .position(nextPosition)
                    .status(Waitlist.WaitlistStatus.WAITING)
                    .createdAt(ZonedDateTime.now())
                    .build();

            Waitlist savedEntry = waitlistRepository.save(waitlistEntry);
            
            log.info("User {} joined waitlist for event {} at position {}", 
                    userId, eventId, nextPosition);

            return new WaitlistResponse(
                savedEntry.getId().toString(),
                userId,
                eventId,
                nextPosition,
                savedEntry.getStatus().toString(),
                savedEntry.getCreatedAt()
            );

        } catch (DataIntegrityViolationException e) {
            // Handle race condition where user tries to join twice simultaneously
            throw new DuplicateBookingException(
                "Already on waitlist", 
                "User is already on the waitlist for this event"
            );
        }
    }

    /**
     * Process booking cancellation and notify next person in waitlist
     * This is triggered by Kafka consumer when booking is cancelled
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void processAvailableSeat(UUID eventId, int quantity) {
        log.info("Processing {} available seats for event {}", quantity, eventId);

        // Notify waitlisted users (one at a time to maintain FIFO order)
        for (int i = 0; i < quantity; i++) {
            Optional<Waitlist> nextInLine = waitlistRepository.findNextInLine(eventId);
            
            if (nextInLine.isEmpty()) {
                log.info("No more users waiting for event {}", eventId);
                break;
            }

            Waitlist waitlistEntry = nextInLine.get();
            notifyWaitlistedUser(waitlistEntry);
        }
    }

    /**
     * Notify individual waitlisted user about available seat
     */
    private void notifyWaitlistedUser(Waitlist waitlistEntry) {
        log.info("Notifying waitlisted user {} for event {}", 
                waitlistEntry.getUser().getId(), waitlistEntry.getEvent().getId());

        // Update waitlist status to NOTIFIED
        waitlistEntry.notifyUser(bookingWindowMinutes);
        waitlistRepository.save(waitlistEntry);

        // Create notification event for Kafka
        WaitlistNotificationEvent notificationEvent = new WaitlistNotificationEvent(
            waitlistEntry.getId().toString(),
            waitlistEntry.getUser().getId().toString(),
            waitlistEntry.getUser().getEmail(),
            waitlistEntry.getUser().getName(),
            waitlistEntry.getEvent().getId().toString(),
            waitlistEntry.getEvent().getName(),
            waitlistEntry.getEvent().getVenue(),
            waitlistEntry.getEvent().getStartsAt(),
            waitlistEntry.getEvent().getAvailableSeats(),
            waitlistEntry.getExpiresAt(),
            String.format("http://localhost:8080/api/v1/bookings?eventId=%s&userId=%s&waitlistId=%s",
                    waitlistEntry.getEvent().getId(),
                    waitlistEntry.getUser().getId(),
                    waitlistEntry.getId())
        );

        // Publish to Kafka for email/WebSocket notifications
        eventPublisher.publishWaitlistNotification(notificationEvent);
    }

    // ========== WAITLIST MANAGEMENT ==========

    @Transactional(readOnly = true)
    public List<WaitlistResponse> getUserWaitlistEntries(String userId) {
        UUID userUuid = parseUUID(userId, "User ID");
        List<Waitlist> entries = waitlistRepository.findByUserIdOrderByCreatedAtDesc(userUuid);
        
        return entries.stream()
                .map(this::toWaitlistResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public WaitlistResponse getWaitlistPosition(String userId, String eventId) {
        UUID userUuid = parseUUID(userId, "User ID");
        UUID eventUuid = parseUUID(eventId, "Event ID");
        
        Waitlist entry = waitlistRepository.findActiveWaitlistEntry(userUuid, eventUuid)
                .orElseThrow(() -> new EventException("Not on waitlist", 
                        "NOT_ON_WAITLIST", 
                        "User is not on the waitlist for this event"));

        long currentPosition = waitlistRepository.getPositionInQueue(eventUuid, entry.getPosition()) + 1;
        
        WaitlistResponse response = toWaitlistResponse(entry);
        response.setPosition((int) currentPosition);
        return response;
    }

    @Transactional
    public void leaveWaitlist(String waitlistId) {
        UUID waitlistUuid = parseUUID(waitlistId, "Waitlist ID");
        
        Waitlist entry = waitlistRepository.findById(waitlistUuid)
                .orElseThrow(() -> new EventException("Waitlist entry not found", 
                        "WAITLIST_NOT_FOUND", 
                        "Waitlist entry with ID " + waitlistId + " does not exist"));

        if (!entry.isWaiting()) {
            throw new EventException("Cannot leave waitlist", 
                    "WAITLIST_STATUS_INVALID", 
                    "Can only leave waitlist if status is WAITING");
        }

        // Remove from waitlist and adjust positions
        int removedPosition = entry.getPosition();
        UUID eventId = entry.getEvent().getId();
        
        waitlistRepository.delete(entry);
        waitlistRepository.adjustPositionsAfterRemoval(eventId, removedPosition);
        
        log.info("User {} left waitlist for event {}, position {} removed", 
                entry.getUser().getId(), eventId, removedPosition);
    }

    /**
     * Mark waitlist entry as converted when user successfully books
     */
    @Transactional
    public void markAsConverted(String waitlistId) {
        UUID waitlistUuid = parseUUID(waitlistId, "Waitlist ID");
        
        Waitlist entry = waitlistRepository.findById(waitlistUuid)
                .orElseThrow(() -> new EventException("Waitlist entry not found", 
                        "WAITLIST_NOT_FOUND", 
                        "Waitlist entry with ID " + waitlistId + " does not exist"));

        entry.convert();
        waitlistRepository.save(entry);
        
        log.info("Waitlist entry {} marked as converted for user {}", 
                waitlistId, entry.getUser().getId());
    }

    // ========== SCHEDULED CLEANUP ==========

    /**
     * Clean up expired waitlist notifications every 5 minutes
     * Users who were notified but didn't book within the time window
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    @Transactional
    public void cleanupExpiredNotifications() {
        List<Waitlist> expiredEntries = waitlistRepository.findExpiredNotifications(ZonedDateTime.now());
        
        if (!expiredEntries.isEmpty()) {
            log.info("Cleaning up {} expired waitlist notifications", expiredEntries.size());
            
            for (Waitlist entry : expiredEntries) {
                entry.expire();
                waitlistRepository.save(entry);
                
                // Notify next person in line (if any)
                processAvailableSeat(entry.getEvent().getId(), 1);
            }
        }
    }

    // ========== ADMIN OPERATIONS ==========

    @Transactional(readOnly = true)
    public List<WaitlistResponse> getEventWaitlist(String eventId) {
        UUID eventUuid = parseUUID(eventId, "Event ID");
        List<Waitlist> entries = waitlistRepository.findByEventIdOrderByPosition(eventUuid);
        
        return entries.stream()
                .map(this::toWaitlistResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public long getWaitlistCount(String eventId) {
        UUID eventUuid = parseUUID(eventId, "Event ID");
        return waitlistRepository.countWaitingForEvent(eventUuid);
    }

    // ========== UTILITY METHODS ==========

    private WaitlistResponse toWaitlistResponse(Waitlist waitlist) {
        return new WaitlistResponse(
            waitlist.getId().toString(),
            waitlist.getUser().getId().toString(),  
            waitlist.getEvent().getId().toString(),
            waitlist.getPosition(),
            waitlist.getStatus().toString(),
            waitlist.getCreatedAt()
        );
    }

    private UUID parseUUID(String id, String fieldName) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid " + fieldName + " format: " + id);
        }
    }

    // ========== RESPONSE DTO (Inner Class) ==========
    
    public static class WaitlistResponse {
        private String waitlistId;
        private String userId;
        private String eventId;
        private Integer position;
        private String status;
        private ZonedDateTime createdAt;

        public WaitlistResponse(String waitlistId, String userId, String eventId, 
                               Integer position, String status, ZonedDateTime createdAt) {
            this.waitlistId = waitlistId;
            this.userId = userId;
            this.eventId = eventId;
            this.position = position;
            this.status = status;
            this.createdAt = createdAt;
        }

        // Getters and setters
        public String getWaitlistId() { return waitlistId; }
        public void setWaitlistId(String waitlistId) { this.waitlistId = waitlistId; }
        
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        
        public String getEventId() { return eventId; }
        public void setEventId(String eventId) { this.eventId = eventId; }
        
        public Integer getPosition() { return position; }
        public void setPosition(Integer position) { this.position = position; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public ZonedDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }
    }
}
