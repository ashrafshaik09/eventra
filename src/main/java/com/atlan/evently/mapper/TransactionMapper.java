package com.atlan.evently.mapper;

import com.atlan.evently.dto.TransactionRequest;
import com.atlan.evently.dto.TransactionResponse;
import com.atlan.evently.model.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    TransactionMapper INSTANCE = Mappers.getMapper(TransactionMapper.class);

    @Mapping(target = "transactionId", source = "id")
    @Mapping(target = "bookingId", source = "booking.id")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "eventId", source = "event.id")
    @Mapping(target = "transactionType", source = "transactionType")
    @Mapping(target = "status", source = "status")
    TransactionResponse toResponse(Transaction transaction);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "booking", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "event", ignore = true)
    @Mapping(target = "transactionType", expression = "java(parseTransactionType(request.getTransactionType()))")
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "createdAt", expression = "java(java.time.ZonedDateTime.now())")
    @Mapping(target = "updatedAt", expression = "java(java.time.ZonedDateTime.now())")
    @Mapping(target = "processedAt", ignore = true)
    Transaction toEntity(TransactionRequest request);

    // Helper method for transaction type parsing
    default Transaction.TransactionType parseTransactionType(String type) {
        return type != null ? Transaction.TransactionType.valueOf(type.toUpperCase()) : Transaction.TransactionType.PAYMENT;
    }

    // UUID to String conversion for DTOs
    default String map(UUID value) {
        return value == null ? null : value.toString();
    }

    // Enum to String conversion
    default String map(Transaction.TransactionType type) {
        return type == null ? null : type.name();
    }

    default String map(Transaction.TransactionStatus status) {
        return status == null ? null : status.name();
    }
}
