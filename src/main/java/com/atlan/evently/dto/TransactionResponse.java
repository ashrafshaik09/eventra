package com.atlan.evently.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

/**
 * Response DTO for transaction information.
 */
@Data
public class TransactionResponse {

    private String transactionId;
    private String bookingId;
    private String userId;
    private String eventId;
    private String transactionType;
    private BigDecimal amount;
    private String currency;
    private String paymentMethod;
    private String paymentGateway;
    private String gatewayTransactionId;
    private String status;
    private String failureReason;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private ZonedDateTime processedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private ZonedDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private ZonedDateTime updatedAt;
}
