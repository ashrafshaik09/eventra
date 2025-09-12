package com.atlan.evently.service;

import com.atlan.evently.dto.EventCommentRequest;
import com.atlan.evently.dto.EventCommentResponse;
import com.atlan.evently.exception.EventException;
import com.atlan.evently.mapper.EventCommentMapper;
import com.atlan.evently.model.Event;
import com.atlan.evently.model.EventComment;
import com.atlan.evently.model.User;
import com.atlan.evently.repository.EventCommentRepository;
import com.atlan.evently.repository.EventRepository;
import com.atlan.evently.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service for managing event comments with support for nested replies.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventCommentService {

    private final EventCommentRepository eventCommentRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EventCommentMapper eventCommentMapper;

    @Transactional
    public EventCommentResponse createComment(EventCommentRequest request) {
        log.info("Creating comment for event {} by user {}", request.getEventId(), request.getUserId());
        
        UUID userUuid = parseUUID(request.getUserId(), "User ID");
        UUID eventUuid = parseUUID(request.getEventId(), "Event ID");

        User user = getUserById(userUuid);
        Event event = getEventById(eventUuid);

        EventComment comment = EventComment.builder()
                .user(user)
                .event(event)
                .commentText(request.getCommentText().trim())
                .build();

        // Handle parent comment for replies
        if (request.getParentCommentId() != null) {
            UUID parentUuid = parseUUID(request.getParentCommentId(), "Parent Comment ID");
            EventComment parentComment = eventCommentRepository.findById(parentUuid)
                    .orElseThrow(() -> new EventException("Parent comment not found",
                            "PARENT_COMMENT_NOT_FOUND",
                            "Parent comment with ID " + request.getParentCommentId() + " does not exist"));
            
            // Validate parent comment belongs to same event
            if (!parentComment.getEvent().getId().equals(eventUuid)) {
                throw new EventException("Invalid parent comment",
                        "INVALID_PARENT_COMMENT",
                        "Parent comment does not belong to the same event");
            }
            
            comment.setParentComment(parentComment);
        }

        EventComment savedComment = eventCommentRepository.save(comment);
        log.info("Successfully created comment {} for event {}", savedComment.getId(), request.getEventId());
        
        return eventCommentMapper.toResponse(savedComment);
    }

    @Transactional
    public EventCommentResponse updateComment(String commentId, EventCommentRequest request) {
        log.info("Updating comment {} by user {}", commentId, request.getUserId());
        
        UUID commentUuid = parseUUID(commentId, "Comment ID");
        UUID userUuid = parseUUID(request.getUserId(), "User ID");

        EventComment comment = eventCommentRepository.findById(commentUuid)
                .orElseThrow(() -> new EventException("Comment not found",
                        "COMMENT_NOT_FOUND",
                        "Comment with ID " + commentId + " does not exist"));

        // Validate user owns the comment
        if (!comment.getUser().getId().equals(userUuid)) {
            throw new EventException("Unauthorized comment update",
                    "UNAUTHORIZED_COMMENT_UPDATE",
                    "User can only update their own comments");
        }

        comment.setCommentText(request.getCommentText().trim());
        EventComment savedComment = eventCommentRepository.save(comment);
        
        log.info("Successfully updated comment {}", commentId);
        return eventCommentMapper.toResponse(savedComment);
    }

    @Transactional
    public void deleteComment(String commentId, String userId) {
        log.info("Deleting comment {} by user {}", commentId, userId);
        
        UUID commentUuid = parseUUID(commentId, "Comment ID");
        UUID userUuid = parseUUID(userId, "User ID");

        EventComment comment = eventCommentRepository.findById(commentUuid)
                .orElseThrow(() -> new EventException("Comment not found",
                        "COMMENT_NOT_FOUND",
                        "Comment with ID " + commentId + " does not exist"));

        // Validate user owns the comment
        if (!comment.getUser().getId().equals(userUuid)) {
            throw new EventException("Unauthorized comment deletion",
                    "UNAUTHORIZED_COMMENT_DELETE",
                    "User can only delete their own comments");
        }

        eventCommentRepository.delete(comment);
        log.info("Successfully deleted comment {}", commentId);
    }

    @Transactional(readOnly = true)
    public Page<EventCommentResponse> getEventComments(String eventId, Pageable pageable) {
        UUID eventUuid = parseUUID(eventId, "Event ID");
        return eventCommentRepository.findByEventIdAndParentCommentIsNullOrderByCreatedAtDesc(eventUuid, pageable)
                .map(eventCommentMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<EventCommentResponse> getCommentReplies(String commentId) {
        UUID commentUuid = parseUUID(commentId, "Comment ID");
        return eventCommentRepository.findByParentCommentIdOrderByCreatedAtAsc(commentUuid)
                .stream()
                .map(eventCommentMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public long getEventCommentCount(String eventId) {
        UUID eventUuid = parseUUID(eventId, "Event ID");
        return eventCommentRepository.countByEventId(eventUuid);
    }

    @Transactional(readOnly = true)
    public Page<EventCommentResponse> getUserComments(String userId, Pageable pageable) {
        UUID userUuid = parseUUID(userId, "User ID");
        return eventCommentRepository.findByUserIdOrderByCreatedAtDesc(userUuid, pageable)
                .map(eventCommentMapper::toResponse);
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
