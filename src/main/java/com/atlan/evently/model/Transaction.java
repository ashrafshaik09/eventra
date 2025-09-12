package com.atlan.evently.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Transaction entity for detailed payment and refund tracking.
 * Provides comprehensive financial analytics for admin dashboard.
 */
@Entity
@Table(name = "transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 20)
    private TransactionType transactionType;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    @Builder.Default
    private String currency = "USD";

    @Column(name = "payment_method", length = 50)
    private String paymentMethod; // CREDIT_CARD, PAYPAL, BANK_TRANSFER, etc.

    @Column(name = "payment_gateway", length = 50)
    private String paymentGateway; // STRIPE, PAYPAL, RAZORPAY, etc.

    @Column(name = "gateway_transaction_id")
    private String gatewayTransactionId; // External payment ID

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private TransactionStatus status = TransactionStatus.PENDING;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "processed_at")
    private ZonedDateTime processedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private ZonedDateTime createdAt = ZonedDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private ZonedDateTime updatedAt = ZonedDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = ZonedDateTime.now();
    }

    // Enums
    public enum TransactionType {
        PAYMENT, REFUND, PARTIAL_REFUND
    }

    public enum TransactionStatus {
        PENDING, COMPLETED, FAILED, CANCELLED
    }

    // Helper methods
    public boolean isSuccessful() {
        return status == TransactionStatus.COMPLETED;
    }

    public boolean isPending() {
        return status == TransactionStatus.PENDING;
    }

    public boolean hasFailed() {
        return status == TransactionStatus.FAILED;
    }

    public void markAsCompleted() {
        this.status = TransactionStatus.COMPLETED;
        this.processedAt = ZonedDateTime.now();
    }

    public void markAsFailed(String reason) {
        this.status = TransactionStatus.FAILED;
        this.failureReason = reason;
        this.processedAt = ZonedDateTime.now();
    }
}
