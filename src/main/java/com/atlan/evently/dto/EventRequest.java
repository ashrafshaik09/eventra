package com.atlan.evently.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Enhanced request DTO for event creation and updates.
 * Supports all new event features including pricing, categorization, and media.
 */
@Data
public class EventRequest {

    @NotBlank(message = "Event name is required")
    @Size(max = 255, message = "Event name cannot exceed 255 characters")
    private String eventName;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;

    @NotBlank(message = "Venue is required")
    @Size(max = 255, message = "Venue cannot exceed 255 characters")
    private String venue;

    @NotNull(message = "Start time is required")
    @Future(message = "Start time must be in the future")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private ZonedDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private ZonedDateTime endTime;

    @NotNull(message = "Capacity is required")
    @Positive(message = "Capacity must be positive")
    @Max(value = 100000, message = "Capacity cannot exceed 100,000")
    private Integer capacity;

    @NotNull(message = "Ticket price is required")
    @DecimalMin(value = "0.00", message = "Ticket price cannot be negative")
    @DecimalMax(value = "99999.99", message = "Ticket price cannot exceed 99,999.99")
    private BigDecimal ticketPrice;

    private Boolean isOnline = false;

    @Pattern(regexp = "^(https?://).*\\.(jpg|jpeg|png|gif|webp)$", 
             message = "Image URL must be a valid HTTP(S) URL ending with jpg, jpeg, png, gif, or webp")
    private String imageUrl;

    private List<String> tags;

    private String categoryId; // UUID string

    // Validation method
    @AssertTrue(message = "End time must be after start time")
    private boolean isEndTimeValid() {
        return endTime == null || startTime == null || endTime.isAfter(startTime);
    }
}