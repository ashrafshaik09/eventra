package com.atlan.evently.mapper;

import com.atlan.evently.dto.EventRequest;
import com.atlan.evently.dto.EventResponse;
import com.atlan.evently.model.Event;
import com.atlan.evently.model.EventCategory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface EventMapper {

    EventMapper INSTANCE = Mappers.getMapper(EventMapper.class);

    @Mapping(target = "eventId", source = "id")
    @Mapping(target = "startTime", source = "startsAt")
    @Mapping(target = "endTime", source = "endsAt")
    @Mapping(target = "bookedSeats", expression = "java(event.getBookedSeats())")
    @Mapping(target = "tags", expression = "java(event.getTagsList())")
    @Mapping(target = "likeCount", expression = "java(event.getLikeCount())")
    @Mapping(target = "commentCount", expression = "java(event.getCommentCount())")
    @Mapping(target = "utilizationPercentage", expression = "java(event.getUtilizationPercentage())")
    @Mapping(target = "isActive", expression = "java(event.isActive())")
    @Mapping(target = "isSoldOut", expression = "java(event.isSoldOut())")
    @Mapping(target = "isLive", expression = "java(event.isLive())")
    @Mapping(target = "hasEnded", expression = "java(event.hasEnded())")
    @Mapping(target = "currency", constant = "USD")
    @Mapping(target = "category", source = "category")
    EventResponse toResponse(Event event);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", source = "eventName")
    @Mapping(target = "startsAt", source = "startTime")
    @Mapping(target = "endsAt", source = "endTime")
    @Mapping(target = "availableSeats", expression = "java(eventRequest.getCapacity())")
    @Mapping(target = "tags", expression = "java(eventRequest.getTags() != null ? String.join(\",\", eventRequest.getTags()) : null)")
    @Mapping(target = "category", expression = "java(mapCategory(eventRequest.getCategoryId()))")
    @Mapping(target = "createdAt", expression = "java(java.time.ZonedDateTime.now())")
    @Mapping(target = "version", constant = "1")
    @Mapping(target = "bookings", ignore = true)
    @Mapping(target = "likes", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "transactions", ignore = true)
    Event toEntity(EventRequest eventRequest);

    @Mapping(target = "id", source = "category.id")
    @Mapping(target = "name", source = "category.name")
    @Mapping(target = "description", source = "category.description")
    @Mapping(target = "colorCode", source = "category.colorCode")
    @Mapping(target = "iconName", source = "category.iconName")
    EventResponse.EventCategoryResponse mapCategoryResponse(EventCategory category);

    // Helper method for category mapping
    default EventCategory mapCategory(String categoryId) {
        if (categoryId == null || categoryId.trim().isEmpty()) {
            return null;
        }
        EventCategory category = new EventCategory();
        category.setId(UUID.fromString(categoryId));
        return category;
    }

    // UUID to String conversion for DTOs
    default String map(UUID value) {
        return value == null ? null : value.toString();
    }

    // String to UUID conversion for entities  
    default UUID map(String value) {
        return value == null ? null : UUID.fromString(value);
    }

    // List<String> to String conversion for tags
    default String mapTagsToString(List<String> tags) {
        return tags != null && !tags.isEmpty() ? String.join(",", tags) : null;
    }

    // String to List<String> conversion for tags
    default List<String> mapStringToTags(String tagsString) {
        return tagsString != null && !tagsString.trim().isEmpty() ? 
               List.of(tagsString.split(",")) : List.of();
    }
}