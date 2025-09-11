package com.atlan.evently.mapper;

import com.atlan.evently.dto.BookingRequest;
import com.atlan.evently.dto.BookingResponse;
import com.atlan.evently.model.Booking;
import com.atlan.evently.model.Event;
import com.atlan.evently.model.User;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BookingMapperTest {

    private final BookingMapper mapper = BookingMapper.INSTANCE;

    @Test
    void testToResponseMapping() {
        Booking booking = new Booking();
        booking.setId("1");
        User user = new User();
        user.setId("1");
        Event event = new Event();
        event.setId("1");
        booking.setUser(user);
        booking.setEvent(event);
        booking.setQuantity(2);
        booking.setStatus("CONFIRMED");

        BookingResponse response = mapper.toResponse(booking);

        assertNotNull(response);
        assertEquals("1", response.getBookingId());
        assertEquals("1", response.getUserId());
        assertEquals("1", response.getEventId());
        assertEquals(2, response.getQuantity());
        assertEquals("CONFIRMED", response.getBookingStatus());
    }

    @Test
    void testToEntityMapping() {
        BookingRequest request = new BookingRequest();
        request.setUserId("1");
        request.setEventId("1");
        request.setQuantity(2);

        Booking entity = mapper.toEntity(request);

        assertNotNull(entity);
        assertEquals("1", entity.getUser().getId());
        assertEquals("1", entity.getEvent().getId());
        assertEquals(2, entity.getQuantity());
        assertEquals("CONFIRMED", entity.getStatus());
        assertNotNull(entity.getCreatedAt());
    }
}