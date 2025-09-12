package com.atlan.evently.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.ZonedDateTime;

/**
 * Response DTO for event like information.
 */
@Data
public class EventLikeResponse {

    private String likeId;
    private String userId;
    private String eventId;
    private String eventName;
    private String userName;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private ZonedDateTime createdAt;
}
