package com.atlan.evently.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.ZonedDateTime;

/**
 * Response DTO for event category information.
 */
@Data
public class EventCategoryResponse {

    private String categoryId;
    private String name;
    private String description;
    private String colorCode;
    private String iconName;
    private Boolean isActive;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private ZonedDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private ZonedDateTime updatedAt;
}
