package com.atlan.evently.controller;

import com.atlan.evently.dto.EventCommentRequest;
import com.atlan.evently.dto.EventCommentResponse;
import com.atlan.evently.service.EventCommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for event comment management.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/events")
@Tag(name = "Event Comments", description = "Event comment and discussion management API")
public class EventCommentController {

    private final EventCommentService eventCommentService;

    @PostMapping("/{eventId}/comments")
    @Operation(summary = "Create comment", description = "Add a new comment to an event")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Comment created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid comment data"),
        @ApiResponse(responseCode = "404", description = "Event or parent comment not found")
    })
    public ResponseEntity<EventCommentResponse> createComment(
            @Parameter(description = "Event UUID") @PathVariable String eventId,
            @Valid @RequestBody EventCommentRequest request) {
        
        // Ensure eventId in path matches request
        request.setEventId(eventId);
        EventCommentResponse response = eventCommentService.createComment(request);
        return ResponseEntity.status(201).body(response);
    }

    @PutMapping("/comments/{commentId}")
    @Operation(summary = "Update comment", description = "Update an existing comment (user must own comment)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Comment updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid comment data"),
        @ApiResponse(responseCode = "403", description = "User does not own this comment"),
        @ApiResponse(responseCode = "404", description = "Comment not found")
    })
    public ResponseEntity<EventCommentResponse> updateComment(
            @Parameter(description = "Comment UUID") @PathVariable String commentId,
            @Valid @RequestBody EventCommentRequest request) {
        
        EventCommentResponse response = eventCommentService.updateComment(commentId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/comments/{commentId}")
    @Operation(summary = "Delete comment", description = "Delete a comment (user must own comment)")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Comment deleted successfully"),
        @ApiResponse(responseCode = "403", description = "User does not own this comment"),
        @ApiResponse(responseCode = "404", description = "Comment not found")
    })
    public ResponseEntity<Void> deleteComment(
            @Parameter(description = "Comment UUID") @PathVariable String commentId,
            @Parameter(description = "User UUID") @RequestParam String userId) {
        
        eventCommentService.deleteComment(commentId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{eventId}/comments")
    @Operation(summary = "Get event comments", description = "Retrieve top-level comments for an event")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved comments")
    public ResponseEntity<Page<EventCommentResponse>> getEventComments(
            @Parameter(description = "Event UUID") @PathVariable String eventId,
            Pageable pageable) {
        
        Page<EventCommentResponse> comments = eventCommentService.getEventComments(eventId, pageable);
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/comments/{commentId}/replies")
    @Operation(summary = "Get comment replies", description = "Retrieve replies to a specific comment")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved replies")
    public ResponseEntity<List<EventCommentResponse>> getCommentReplies(
            @Parameter(description = "Comment UUID") @PathVariable String commentId) {
        
        List<EventCommentResponse> replies = eventCommentService.getCommentReplies(commentId);
        return ResponseEntity.ok(replies);
    }

    @GetMapping("/{eventId}/comments/count")
    @Operation(summary = "Get comment count", description = "Get total number of comments for an event")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved comment count")
    public ResponseEntity<Map<String, Long>> getCommentCount(
            @Parameter(description = "Event UUID") @PathVariable String eventId) {
        
        long count = eventCommentService.getEventCommentCount(eventId);
        return ResponseEntity.ok(Map.of("commentCount", count));
    }

    @GetMapping("/users/{userId}/comments")
    @Operation(summary = "Get user comments", description = "Retrieve all comments by a specific user")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved user comments")
    public ResponseEntity<Page<EventCommentResponse>> getUserComments(
            @Parameter(description = "User UUID") @PathVariable String userId,
            Pageable pageable) {
        
        Page<EventCommentResponse> comments = eventCommentService.getUserComments(userId, pageable);
        return ResponseEntity.ok(comments);
    }
}
