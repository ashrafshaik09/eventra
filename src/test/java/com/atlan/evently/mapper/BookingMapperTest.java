package com.atlan.evently.mapper;

import com.atlan.evently.dto.BookingRequest;
import com.atlan.evently.dto.BookingResponse;
import com.atlan.evently.model.Booking;
import com.atlan.evently.model.Event;
import com.atlan.evently.model.User;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class BookingMapperTest {

    private final BookingMapper mapper = BookingMapper.INSTANCE;

    @Test
    void testToResponseMapping() {
        Booking booking = new Booking();
        booking.setId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));

        User user = new User();
        user.setId(UUID.fromString("223e4567-e89b-12d3-a456-426614174000"));

        Event event = new Event();
        event.setId(UUID.fromString("323e4567-e89b-12d3-a456-426614174000"));

        booking.setUser(user);
        booking.setEvent(event);
        booking.setQuantity(2);
        booking.setStatus("CONFIRMED");

        BookingResponse response = mapper.toResponse(booking);

        assertNotNull(response);
        assertEquals("123e4567-e89b-12d3-a456-426614174000", response.getBookingId());
        assertEquals("223e4567-e89b-12d3-a456-426614174000", response.getUserId());
        assertEquals("323e4567-e89b-12d3-a456-426614174000", response.getEventId());
        assertEquals(2, response.getQuantity());
        assertEquals("CONFIRMED", response.getBookingStatus());
    }

    @Test
    void testToEntityMapping() {
        BookingRequest request = new BookingRequest();
        request.setUserId("223e4567-e89b-12d3-a456-426614174000");
        request.setEventId("323e4567-e89b-12d3-a456-426614174000");
        request.setQuantity(2);

        Booking entity = mapper.toEntity(request);

        assertNotNull(entity);
        assertEquals(UUID.fromString("223e4567-e89b-12d3-a456-426614174000"), entity.getUser().getId());
        assertEquals(UUID.fromString("323e4567-e89b-12d3-a456-426614174000"), entity.getEvent().getId());
        assertEquals(2, entity.getQuantity());
        assertEquals("CONFIRMED", entity.getStatus());
        assertNotNull(entity.getCreatedAt());
    }
}