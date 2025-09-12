package com.atlan.evently.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request DTO for creating and updating event comments.
 */
@Data
public class EventCommentRequest {

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotBlank(message = "Event ID is required")
    private String eventId;

    @NotBlank(message = "Comment text is required")
    @Size(max = 1000, message = "Comment cannot exceed 1000 characters")
    private String commentText;

    private String parentCommentId; // For nested replies
}
