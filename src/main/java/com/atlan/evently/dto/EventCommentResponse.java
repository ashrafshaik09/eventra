package com.atlan.evently.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * Response DTO for event comments with nested reply support.
 */
@Data
public class EventCommentResponse {

    private String commentId;
    private String userId;
    private String userName;
    private String eventId;
    private String commentText;
    private String parentCommentId;
    private Boolean isEdited;
    private Integer replyCount;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private ZonedDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private ZonedDateTime updatedAt;

    private List<EventCommentResponse> replies; // For nested display
}
