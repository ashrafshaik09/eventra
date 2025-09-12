package com.atlan.evently.mapper;

import com.atlan.evently.dto.EventCommentResponse;
import com.atlan.evently.model.EventComment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface EventCommentMapper {

    EventCommentMapper INSTANCE = Mappers.getMapper(EventCommentMapper.class);

    @Mapping(target = "commentId", source = "id")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userName", source = "user.name")
    @Mapping(target = "eventId", source = "event.id")
    @Mapping(target = "parentCommentId", source = "parentComment.id")
    @Mapping(target = "replyCount", expression = "java(comment.getReplyCount())")
    @Mapping(target = "replies", ignore = true) // Loaded separately to avoid deep nesting
    EventCommentResponse toResponse(EventComment comment);

    // UUID to String conversion for DTOs
    default String map(UUID value) {
        return value == null ? null : value.toString();
    }
}
