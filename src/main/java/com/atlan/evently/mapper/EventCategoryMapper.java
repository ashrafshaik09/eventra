package com.atlan.evently.mapper;

import com.atlan.evently.dto.EventCategoryRequest;
import com.atlan.evently.dto.EventCategoryResponse;
import com.atlan.evently.model.EventCategory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface EventCategoryMapper {

    EventCategoryMapper INSTANCE = Mappers.getMapper(EventCategoryMapper.class);

    @Mapping(target = "categoryId", source = "id")
    EventCategoryResponse toResponse(EventCategory category);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "events", ignore = true)
    @Mapping(target = "createdAt", expression = "java(java.time.ZonedDateTime.now())")
    @Mapping(target = "updatedAt", expression = "java(java.time.ZonedDateTime.now())")
    EventCategory toEntity(EventCategoryRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "events", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", expression = "java(java.time.ZonedDateTime.now())")
    void updateEntity(@MappingTarget EventCategory category, EventCategoryRequest request);

    // UUID to String conversion for DTOs
    default String map(UUID value) {
        return value == null ? null : value.toString();
    }

    // String to UUID conversion for entities
    default UUID map(String value) {
        return value == null ? null : UUID.fromString(value);
    }
}
