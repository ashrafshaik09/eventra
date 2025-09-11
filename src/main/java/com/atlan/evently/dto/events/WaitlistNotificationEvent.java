package com.atlan.evently.dto.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WaitlistNotificationEvent {
    private String waitlistId;
    private String userId;
    private String userEmail;
    private String userName;
    private String eventId;
    private String eventName;
    private String eventVenue;
    private ZonedDateTime eventStartTime;
    private Integer availableSeats;
    private ZonedDateTime expiresAt;
    private String bookingUrl; // Direct link to book the seat
}
