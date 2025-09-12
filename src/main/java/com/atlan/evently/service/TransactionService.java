package com.atlan.evently.service;

import com.atlan.evently.dto.TransactionRequest;
import com.atlan.evently.dto.TransactionResponse;
import com.atlan.evently.exception.EventException;
import com.atlan.evently.mapper.TransactionMapper;
import com.atlan.evently.model.Booking;
import com.atlan.evently.model.Transaction;
import com.atlan.evently.repository.BookingRepository;
import com.atlan.evently.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing financial transactions and payment analytics.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final BookingRepository bookingRepository;
    private final TransactionMapper transactionMapper;

    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request) {
        log.info("Creating transaction for booking {} with amount {}", 
                request.getBookingId(), request.getAmount());

        UUID bookingUuid = parseUUID(request.getBookingId(), "Booking ID");
        Booking booking = getBookingById(bookingUuid);

        Transaction transaction = transactionMapper.toEntity(request);
        transaction.setBooking(booking);
        transaction.setUser(booking.getUser());
        transaction.setEvent(booking.getEvent());

        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("Successfully created transaction {} for booking {}", 
                savedTransaction.getId(), request.getBookingId());

        return transactionMapper.toResponse(savedTransaction);
    }

    @Transactional
    public TransactionResponse updateTransactionStatus(String transactionId, 
                                                      Transaction.TransactionStatus status, 
                                                      String failureReason) {
        log.info("Updating transaction {} status to {}", transactionId, status);

        UUID transactionUuid = parseUUID(transactionId, "Transaction ID");
        Transaction transaction = transactionRepository.findById(transactionUuid)
                .orElseThrow(() -> new EventException("Transaction not found",
                        "TRANSACTION_NOT_FOUND",
                        "Transaction with ID " + transactionId + " does not exist"));

        switch (status) {
            case COMPLETED -> transaction.markAsCompleted();
            case FAILED -> transaction.markAsFailed(failureReason);
            case CANCELLED -> {
                transaction.setStatus(Transaction.TransactionStatus.CANCELLED);
                transaction.setProcessedAt(ZonedDateTime.now());
            }
            default -> transaction.setStatus(status);
        }

        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("Successfully updated transaction {} status to {}", transactionId, status);

        return transactionMapper.toResponse(savedTransaction);
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getBookingTransactions(String bookingId) {
        UUID bookingUuid = parseUUID(bookingId, "Booking ID");
        return transactionRepository.findByBookingIdOrderByCreatedAtDesc(bookingUuid)
                .stream()
                .map(transactionMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> getUserTransactions(String userId, Pageable pageable) {
        UUID userUuid = parseUUID(userId, "User ID");
        return transactionRepository.findByUserIdOrderByCreatedAtDesc(userUuid, pageable)
                .map(transactionMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> getEventTransactions(String eventId, Pageable pageable) {
        UUID eventUuid = parseUUID(eventId, "Event ID");
        return transactionRepository.findByEventIdOrderByCreatedAtDesc(eventUuid, pageable)
                .map(transactionMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public TransactionAnalyticsResponse getTransactionAnalytics(ZonedDateTime startDate, 
                                                               ZonedDateTime endDate) {
        log.info("Generating transaction analytics for period {} to {}", startDate, endDate);

        // Revenue analytics
        BigDecimal totalRevenue = transactionRepository.getTotalRevenueByDateRange(
                Transaction.TransactionStatus.COMPLETED,
                Transaction.TransactionType.PAYMENT,
                startDate, endDate).orElse(BigDecimal.ZERO);

        // Transaction counts by status
        List<Object[]> statusCounts = transactionRepository.getTransactionCountByStatus();
        Map<String, Long> statusCountMap = statusCounts.stream()
                .collect(Collectors.toMap(
                    row -> row[0].toString(),
                    row -> (Long) row[1]
                ));

        // Payment method statistics
        List<Object[]> paymentMethodStats = transactionRepository.getPaymentMethodStatistics();
        List<PaymentMethodStatResponse> paymentMethods = paymentMethodStats.stream()
                .map(row -> new PaymentMethodStatResponse(
                    (String) row[0],
                    (Long) row[1],
                    (BigDecimal) row[2]
                ))
                .toList();

        // Daily revenue report
        List<Object[]> dailyRevenue = transactionRepository.getDailyRevenueReport(startDate);
        List<DailyRevenueResponse> dailyRevenueList = dailyRevenue.stream()
                .map(row -> new DailyRevenueResponse(
                    row[0].toString(), // Date
                    (BigDecimal) row[1],
                    (Long) row[2]
                ))
                .toList();

        return TransactionAnalyticsResponse.builder()
                .totalRevenue(totalRevenue)
                .transactionCountsByStatus(statusCountMap)
                .paymentMethodStats(paymentMethods)
                .dailyRevenue(dailyRevenueList)
                .reportPeriod(new ReportPeriod(startDate, endDate))
                .build();
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getFailedTransactions(Pageable pageable) {
        return transactionRepository.findFailedTransactions(pageable)
                .stream()
                .map(transactionMapper::toResponse)
                .toList();
    }

    private Booking getBookingById(UUID bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EventException("Booking not found",
                        "BOOKING_NOT_FOUND",
                        "Booking with ID " + bookingId + " does not exist"));
    }

    private UUID parseUUID(String id, String fieldName) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid " + fieldName + " format: " + id);
        }
    }

    // Response DTOs
    @lombok.Data
    @lombok.Builder
    public static class TransactionAnalyticsResponse {
        private BigDecimal totalRevenue;
        private Map<String, Long> transactionCountsByStatus;
        private List<PaymentMethodStatResponse> paymentMethodStats;
        private List<DailyRevenueResponse> dailyRevenue;
        private ReportPeriod reportPeriod;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class PaymentMethodStatResponse {
        private String paymentMethod;
        private Long transactionCount;
        private BigDecimal totalAmount;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class DailyRevenueResponse {
        private String date;
        private BigDecimal revenue;
        private Long transactionCount;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class ReportPeriod {
        private ZonedDateTime startDate;
        private ZonedDateTime endDate;
    }
}
