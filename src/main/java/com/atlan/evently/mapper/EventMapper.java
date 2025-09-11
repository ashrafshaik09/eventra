package com.atlan.evently.mapper;

import com.atlan.evently.dto.EventRequest;
import com.atlan.evently.dto.EventResponse;
import com.atlan.evently.model.Event;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface EventMapper {

    EventMapper INSTANCE = Mappers.getMapper(EventMapper.class);

    @Mapping(target = "eventId", source = "id")
    @Mapping(target = "startTime", source = "startsAt")
    EventResponse toResponse(Event event);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", source = "eventName")
    @Mapping(target = "startsAt", source = "startTime")
    @Mapping(target = "availableSeats", expression = "java(eventRequest.getCapacity())")
    @Mapping(target = "createdAt", expression = "java(java.time.ZonedDateTime.now())")
    @Mapping(target = "version", constant = "1")
    Event toEntity(EventRequest eventRequest);

    // UUID to String conversion for DTOs
    default String map(UUID value) {
        return value == null ? null : value.toString();
    }

    // String to UUID conversion for entities  
    default UUID map(String value) {
        return value == null ? null : UUID.fromString(value);
    }
}