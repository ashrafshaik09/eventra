package com.atlan.evently.mapper;

import com.atlan.evently.dto.EventLikeResponse;
import com.atlan.evently.model.EventLike;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface EventLikeMapper {

    EventLikeMapper INSTANCE = Mappers.getMapper(EventLikeMapper.class);

    @Mapping(target = "likeId", source = "id")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "eventId", source = "event.id")
    @Mapping(target = "eventName", source = "event.name")
    @Mapping(target = "userName", source = "user.name")
    EventLikeResponse toResponse(EventLike eventLike);

    // UUID to String conversion for DTOs
    default String map(UUID value) {
        return value == null ? null : value.toString();
    }
}
