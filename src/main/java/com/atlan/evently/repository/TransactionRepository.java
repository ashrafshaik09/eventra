package com.atlan.evently.repository;

import com.atlan.evently.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Transaction entity operations.
 * Provides detailed payment tracking and analytics for admin dashboard.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    /**
     * Find transactions by booking ID
     */
    List<Transaction> findByBookingIdOrderByCreatedAtDesc(UUID bookingId);

    /**
     * Find transactions by user
     */
    Page<Transaction> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    /**
     * Find transactions by event
     */
    Page<Transaction> findByEventIdOrderByCreatedAtDesc(UUID eventId, Pageable pageable);

    /**
     * Find transactions by status
     */
    Page<Transaction> findByStatusOrderByCreatedAtDesc(Transaction.TransactionStatus status, Pageable pageable);

    /**
     * Find transactions by type
     */
    Page<Transaction> findByTransactionTypeOrderByCreatedAtDesc(Transaction.TransactionType type, Pageable pageable);

    /**
     * Find completed transactions for analytics
     */
    List<Transaction> findByStatusAndCreatedAtBetween(
            Transaction.TransactionStatus status, 
            ZonedDateTime startDate, 
            ZonedDateTime endDate);

    /**
     * Get revenue analytics by date range
     */
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.status = :status AND t.transactionType = :type AND t.createdAt BETWEEN :startDate AND :endDate")
    Optional<BigDecimal> getTotalRevenueByDateRange(
            @Param("status") Transaction.TransactionStatus status,
            @Param("type") Transaction.TransactionType type,
            @Param("startDate") ZonedDateTime startDate,
            @Param("endDate") ZonedDateTime endDate);

    /**
     * Get transaction count by status
     */
    @Query("SELECT t.status, COUNT(t) FROM Transaction t GROUP BY t.status")
    List<Object[]> getTransactionCountByStatus();

    /**
     * Get payment method statistics
     */
    @Query("SELECT t.paymentMethod, COUNT(t), SUM(t.amount) FROM Transaction t WHERE t.status = 'COMPLETED' GROUP BY t.paymentMethod ORDER BY COUNT(t) DESC")
    List<Object[]> getPaymentMethodStatistics();

    /**
     * Find failed transactions for admin review
     */
    @Query("SELECT t FROM Transaction t WHERE t.status = 'FAILED' ORDER BY t.createdAt DESC")
    List<Transaction> findFailedTransactions(Pageable pageable);

    /**
     * Get daily revenue report
     */
    @Query("SELECT DATE(t.createdAt), SUM(t.amount), COUNT(t) FROM Transaction t WHERE t.status = 'COMPLETED' AND t.transactionType = 'PAYMENT' AND t.createdAt >= :startDate GROUP BY DATE(t.createdAt) ORDER BY DATE(t.createdAt)")
    List<Object[]> getDailyRevenueReport(@Param("startDate") ZonedDateTime startDate);

    /**
     * Get transactions by gateway for reconciliation
     */
    List<Transaction> findByPaymentGatewayAndStatusOrderByCreatedAtDesc(String paymentGateway, Transaction.TransactionStatus status);
}
