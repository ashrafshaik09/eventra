package com.atlan.evently.mapper;

import com.atlan.evently.dto.UserRegistrationRequest;
import com.atlan.evently.dto.UserResponse;
import com.atlan.evently.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(target = "userId", source = "id")
    UserResponse toResponse(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", expression = "java(java.time.ZonedDateTime.now())")
    @Mapping(target = "bookings", ignore = true)
    User toEntity(UserRegistrationRequest request);

    // UUID to String conversion for DTOs
    default String map(UUID value) {
        return value == null ? null : value.toString();
    }

    // String to UUID conversion for entities
    default UUID map(String value) {
        return value == null ? null : UUID.fromString(value);
    }
}
