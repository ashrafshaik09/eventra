package com.atlan.evently.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Enhanced response DTO for event data with comprehensive event information.
 */
@Data
public class EventResponse {

    private String eventId;
    private String name;
    private String description;
    private String venue;
    private List<String> tags;
    private Boolean isOnline;
    private String imageUrl;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private ZonedDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private ZonedDateTime endTime;

    private Integer capacity;
    private Integer availableSeats;
    private Integer bookedSeats;
    private BigDecimal ticketPrice;
    private String currency = "USD";

    // Category information
    private EventCategoryResponse category;

    // Engagement metrics
    private Long likeCount;
    private Long commentCount;
    private Double utilizationPercentage;

    // Status flags
    private Boolean isActive;
    private Boolean isSoldOut;
    private Boolean isLive;
    private Boolean hasEnded;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private ZonedDateTime createdAt;

    @Data
    public static class EventCategoryResponse {
        private String id;
        private String name;
        private String description;
        private String colorCode;
        private String iconName;
    }
}