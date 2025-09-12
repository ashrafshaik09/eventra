package com.atlan.evently.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Request DTO for creating transactions.
 */
@Data
public class TransactionRequest {

    @NotBlank(message = "Booking ID is required")
    private String bookingId;

    @NotNull(message = "Transaction type is required")
    private String transactionType; // PAYMENT, REFUND, PARTIAL_REFUND

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    private String currency = "USD";
    private String paymentMethod;
    private String paymentGateway;
    private String gatewayTransactionId;
}
