package com.atlan.evently.controller;

import com.atlan.evently.dto.TransactionRequest;
import com.atlan.evently.dto.TransactionResponse;
import com.atlan.evently.model.Transaction;
import com.atlan.evently.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * REST controller for transaction management and payment analytics.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/admin/transactions")
@Tag(name = "Transaction Management", description = "Payment processing and financial analytics API")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create transaction", description = "Create a new payment transaction")
    @ApiResponse(responseCode = "201", description = "Transaction created successfully")
    public ResponseEntity<TransactionResponse> createTransaction(@Valid @RequestBody TransactionRequest request) {
        TransactionResponse response = transactionService.createTransaction(request);
        return ResponseEntity.status(201).body(response);
    }

    @PutMapping("/{transactionId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update transaction status", description = "Update the status of a transaction")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Transaction status updated successfully"),
        @ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    public ResponseEntity<TransactionResponse> updateTransactionStatus(
            @Parameter(description = "Transaction UUID") @PathVariable String transactionId,
            @Parameter(description = "New status") @RequestParam Transaction.TransactionStatus status,
            @Parameter(description = "Failure reason (if status is FAILED)") @RequestParam(required = false) String failureReason) {
        
        TransactionResponse response = transactionService.updateTransactionStatus(transactionId, status, failureReason);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/booking/{bookingId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get transactions by booking", description = "Retrieve all transactions for a specific booking")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved booking transactions")
    public ResponseEntity<List<TransactionResponse>> getBookingTransactions(
            @Parameter(description = "Booking UUID") @PathVariable String bookingId) {
        
        List<TransactionResponse> transactions = transactionService.getBookingTransactions(bookingId);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user transactions", description = "Retrieve all transactions for a specific user")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved user transactions")
    public ResponseEntity<Page<TransactionResponse>> getUserTransactions(
            @Parameter(description = "User UUID") @PathVariable String userId,
            Pageable pageable) {
        
        Page<TransactionResponse> transactions = transactionService.getUserTransactions(userId, pageable);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/event/{eventId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get event transactions", description = "Retrieve all transactions for a specific event")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved event transactions")
    public ResponseEntity<Page<TransactionResponse>> getEventTransactions(
            @Parameter(description = "Event UUID") @PathVariable String eventId,
            Pageable pageable) {
        
        Page<TransactionResponse> transactions = transactionService.getEventTransactions(eventId, pageable);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/analytics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get transaction analytics", description = "Comprehensive financial analytics and reporting")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved transaction analytics")
    public ResponseEntity<TransactionService.TransactionAnalyticsResponse> getTransactionAnalytics(
            @Parameter(description = "Start date for analytics period") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime startDate,
            @Parameter(description = "End date for analytics period")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime endDate) {
        
        TransactionService.TransactionAnalyticsResponse analytics = 
                transactionService.getTransactionAnalytics(startDate, endDate);
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/failed")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get failed transactions", description = "Retrieve failed transactions for admin review")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved failed transactions")
    public ResponseEntity<List<TransactionResponse>> getFailedTransactions(Pageable pageable) {
        List<TransactionResponse> failedTransactions = transactionService.getFailedTransactions(pageable);
        return ResponseEntity.ok(failedTransactions);
    }
}
