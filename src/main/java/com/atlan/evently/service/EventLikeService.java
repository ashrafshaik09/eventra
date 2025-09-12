package com.atlan.evently.service;

import com.atlan.evently.dto.EventLikeResponse;
import com.atlan.evently.exception.EventException;
import com.atlan.evently.mapper.EventLikeMapper;
import com.atlan.evently.model.Event;
import com.atlan.evently.model.EventLike;
import com.atlan.evently.model.User;
import com.atlan.evently.repository.EventLikeRepository;
import com.atlan.evently.repository.EventRepository;
import com.atlan.evently.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for managing event likes with proper user validation and analytics.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventLikeService {

    private final EventLikeRepository eventLikeRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EventLikeMapper eventLikeMapper;

    @Transactional
    public EventLikeResponse likeEvent(String userId, String eventId) {
        log.info("User {} attempting to like event {}", userId, eventId);
        
        UUID userUuid = parseUUID(userId, "User ID");
        UUID eventUuid = parseUUID(eventId, "Event ID");

        // Validate user and event exist
        User user = getUserById(userUuid);
        Event event = getEventById(eventUuid);

        // Check if already liked
        Optional<EventLike> existingLike = eventLikeRepository.findByUserIdAndEventId(userUuid, eventUuid);
        if (existingLike.isPresent()) {
            throw new EventException("Event already liked",
                    "EVENT_ALREADY_LIKED",
                    "User has already liked this event");
        }

        // Create new like
        EventLike eventLike = EventLike.builder()
                .user(user)
                .event(event)
                .build();

        EventLike savedLike = eventLikeRepository.save(eventLike);
        log.info("User {} successfully liked event {}", userId, eventId);
        
        return eventLikeMapper.toResponse(savedLike);
    }

    @Transactional
    public void unlikeEvent(String userId, String eventId) {
        log.info("User {} attempting to unlike event {}", userId, eventId);
        
        UUID userUuid = parseUUID(userId, "User ID");
        UUID eventUuid = parseUUID(eventId, "Event ID");

        Optional<EventLike> existingLike = eventLikeRepository.findByUserIdAndEventId(userUuid, eventUuid);
        if (existingLike.isEmpty()) {
            throw new EventException("Event not liked",
                    "EVENT_NOT_LIKED",
                    "User has not liked this event");
        }

        eventLikeRepository.delete(existingLike.get());
        log.info("User {} successfully unliked event {}", userId, eventId);
    }

    @Transactional(readOnly = true)
    public boolean isEventLikedByUser(String userId, String eventId) {
        UUID userUuid = parseUUID(userId, "User ID");
        UUID eventUuid = parseUUID(eventId, "Event ID");
        return eventLikeRepository.existsByUserIdAndEventId(userUuid, eventUuid);
    }

    @Transactional(readOnly = true)
    public long getEventLikeCount(String eventId) {
        UUID eventUuid = parseUUID(eventId, "Event ID");
        return eventLikeRepository.countByEventId(eventUuid);
    }

    @Transactional(readOnly = true)
    public List<EventLikeResponse> getEventLikes(String eventId) {
        UUID eventUuid = parseUUID(eventId, "Event ID");
        return eventLikeRepository.findByEventIdOrderByCreatedAtDesc(eventUuid)
                .stream()
                .map(eventLikeMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EventLikeResponse> getUserLikes(String userId) {
        UUID userUuid = parseUUID(userId, "User ID");
        return eventLikeRepository.findByUserIdOrderByCreatedAtDesc(userUuid)
                .stream()
                .map(eventLikeMapper::toResponse)
                .toList();
    }

    private User getUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EventException("User not found",
                        "USER_NOT_FOUND",
                        "User with ID " + userId + " does not exist"));
    }

    private Event getEventById(UUID eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new EventException("Event not found",
                        "EVENT_NOT_FOUND",
                        "Event with ID " + eventId + " does not exist"));
    }

    private UUID parseUUID(String id, String fieldName) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid " + fieldName + " format: " + id);
        }
    }
}
