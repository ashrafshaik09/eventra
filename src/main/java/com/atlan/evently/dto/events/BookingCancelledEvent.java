package com.atlan.evently.dto.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingCancelledEvent {
    private String bookingId;
    private String userId;
    private String eventId;
    private Integer quantity;
    private ZonedDateTime cancelledAt;
    private String reason; // "USER_CANCELLED", "ADMIN_CANCELLED", "EXPIRED"
}
