package com.atlan.evently.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Enhanced booking request with payment information.
 */
@Data
public class BookingRequest {

    @NotBlank(message = "User ID is required")
    private String userId; // String for API compatibility

    @NotBlank(message = "Event ID is required")
    private String eventId; // String for API compatibility

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private Integer quantity;

    private String idempotencyKey;

    // Payment information
    private String paymentMethod; // CREDIT_CARD, PAYPAL, etc.
    private String paymentGateway; // STRIPE, PAYPAL, etc.
    private BigDecimal expectedAmount; // For validation
    private String currency = "USD";
    
    // Payment gateway specific data (flexible JSON)
    private String paymentData; // JSON string with gateway-specific fields
}