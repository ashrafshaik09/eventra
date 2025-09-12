package com.atlan.evently.controller;

import com.atlan.evently.dto.EventLikeResponse;
import com.atlan.evently.service.EventLikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for event like functionality.
 * Provides user engagement tracking through likes.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/events")
@Tag(name = "Event Likes", description = "Event like/unlike functionality and engagement tracking")
public class EventLikeController {

    private final EventLikeService eventLikeService;

    @PostMapping("/{eventId}/like")
    @Operation(summary = "Like an event", description = "Add a like to an event")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Event liked successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid user or event ID"),
        @ApiResponse(responseCode = "404", description = "User or event not found"),
        @ApiResponse(responseCode = "409", description = "Event already liked by user")
    })
    public ResponseEntity<EventLikeResponse> likeEvent(
            @Parameter(description = "Event UUID") @PathVariable String eventId,
            @Parameter(description = "User UUID") @RequestParam String userId) {
        EventLikeResponse response = eventLikeService.likeEvent(userId, eventId);
        return ResponseEntity.status(201).body(response);
    }

    @DeleteMapping("/{eventId}/like")
    @Operation(summary = "Unlike an event", description = "Remove like from an event")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Event unliked successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid user or event ID"),
        @ApiResponse(responseCode = "404", description = "User or event not found"),
        @ApiResponse(responseCode = "409", description = "Event not liked by user")
    })
    public ResponseEntity<Void> unlikeEvent(
            @Parameter(description = "Event UUID") @PathVariable String eventId,
            @Parameter(description = "User UUID") @RequestParam String userId) {
        eventLikeService.unlikeEvent(userId, eventId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{eventId}/likes")
    @Operation(summary = "Get event likes", description = "Retrieve all likes for an event")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved event likes")
    public ResponseEntity<List<EventLikeResponse>> getEventLikes(
            @Parameter(description = "Event UUID") @PathVariable String eventId) {
        List<EventLikeResponse> likes = eventLikeService.getEventLikes(eventId);
        return ResponseEntity.ok(likes);
    }

    @GetMapping("/{eventId}/likes/count")
    @Operation(summary = "Get event like count", description = "Get total number of likes for an event")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved like count")
    public ResponseEntity<Map<String, Long>> getEventLikeCount(
            @Parameter(description = "Event UUID") @PathVariable String eventId) {
        long count = eventLikeService.getEventLikeCount(eventId);
        return ResponseEntity.ok(Map.of("likeCount", count));
    }

    @GetMapping("/{eventId}/likes/check")
    @Operation(summary = "Check if user liked event", description = "Check if a specific user has liked an event")
    @ApiResponse(responseCode = "200", description = "Successfully checked like status")
    public ResponseEntity<Map<String, Boolean>> checkUserLiked(
            @Parameter(description = "Event UUID") @PathVariable String eventId,
            @Parameter(description = "User UUID") @RequestParam String userId) {
        boolean isLiked = eventLikeService.isEventLikedByUser(userId, eventId);
        return ResponseEntity.ok(Map.of("isLiked", isLiked));
    }
}
