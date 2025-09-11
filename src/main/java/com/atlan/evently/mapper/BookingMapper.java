package com.atlan.evently.mapper;

import com.atlan.evently.dto.BookingRequest;
import com.atlan.evently.dto.BookingResponse;
import com.atlan.evently.model.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    BookingMapper INSTANCE = Mappers.getMapper(BookingMapper.class);

    @Mapping(target = "bookingId", source = "id")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "event.id", target = "eventId")
    @Mapping(target = "bookingStatus", source = "status")
    BookingResponse toResponse(Booking booking);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user.id", source = "userId")
    @Mapping(target = "event.id", source = "eventId")
    @Mapping(target = "status", constant = "CONFIRMED")
    @Mapping(target = "createdAt", expression = "java(java.time.ZonedDateTime.now())")
    Booking toEntity(BookingRequest bookingRequest);

    // UUID to String conversion for DTOs
    default String map(UUID value) {
        return value == null ? null : value.toString();
    }

    // String to UUID conversion for entities
    default UUID map(String value) {
        return value == null ? null : UUID.fromString(value);
    }
}