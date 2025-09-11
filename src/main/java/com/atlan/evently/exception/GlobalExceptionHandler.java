package com.atlan.evently.exception;

import com.atlan.evently.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.persistence.OptimisticLockException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * Comprehensive global exception handler for the Evently application.
 * Provides standardized error responses with appropriate HTTP status codes and detailed information.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // ========== BUSINESS LOGIC EXCEPTIONS ==========

    @ExceptionHandler(EventException.class)
    public ResponseEntity<ErrorResponse> handleEventException(EventException ex, WebRequest request) {
        log.error("EventException: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = createErrorResponse(
                HttpStatus.CONFLICT,
                ex.getMessage(),
                ex.getErrorCode(),
                ex.getDetails(),
                request
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(BookingConflictException.class)
    public ResponseEntity<ErrorResponse> handleBookingConflict(BookingConflictException ex, WebRequest request) {
        log.warn("Booking conflict: {}", ex.getMessage());

        ErrorResponse errorResponse = createErrorResponse(
                HttpStatus.CONFLICT,
                ex.getMessage(),
                "BOOKING_CONFLICT",
                ex.getDetails(),
                request
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(DuplicateBookingException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateBooking(DuplicateBookingException ex, WebRequest request) {
        log.info("Duplicate booking attempt: {}", ex.getMessage());

        ErrorResponse errorResponse = createErrorResponse(
                HttpStatus.CONFLICT,
                ex.getMessage(),
                "DUPLICATE_BOOKING",
                ex.getDetails(),
                request
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex, WebRequest request) {
        log.info("User not found: {}", ex.getMessage());

        ErrorResponse errorResponse = createErrorResponse(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                "USER_NOT_FOUND",
                "User ID: " + ex.getUserId(),
                request
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    // ========== AUTHENTICATION & AUTHORIZATION ==========

    @ExceptionHandler({UnauthorizedException.class, AuthenticationCredentialsNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleUnauthorized(Exception ex, WebRequest request) {
        log.warn("Unauthorized access attempt: {}", ex.getMessage());

        String details = ex instanceof UnauthorizedException ?
                ((UnauthorizedException) ex).getDetails() :
                "Valid authentication credentials are required";

        ErrorResponse errorResponse = createErrorResponse(
                HttpStatus.UNAUTHORIZED,
                "Authentication required",
                "UNAUTHORIZED",
                details,
                request
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler({ForbiddenException.class, AccessDeniedException.class})
    public ResponseEntity<ErrorResponse> handleForbidden(Exception ex, WebRequest request) {
        log.warn("Forbidden access attempt: {}", ex.getMessage());

        String details = ex instanceof ForbiddenException ?
                String.format("Required role: %s, User role: %s",
                        ((ForbiddenException) ex).getRequiredRole(),
                        ((ForbiddenException) ex).getUserRole()) :
                "Insufficient permissions to access this resource";

        ErrorResponse errorResponse = createErrorResponse(
                HttpStatus.FORBIDDEN,
                "Access denied",
                "FORBIDDEN",
                details,
                request
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    // ========== CONCURRENCY & LOCKING ==========

    @ExceptionHandler({OptimisticLockingFailureException.class,
            ObjectOptimisticLockingFailureException.class,
            OptimisticLockException.class})
    public ResponseEntity<ErrorResponse> handleOptimisticLockingFailure(Exception ex, WebRequest request) {
        log.warn("Optimistic locking failure: {}", ex.getMessage());

        ErrorResponse errorResponse = createErrorResponse(
                HttpStatus.CONFLICT,
                "Resource was modified by another request",
                "OPTIMISTIC_LOCK_FAILURE",
                "The resource you're trying to modify was updated by another user. Please refresh and try again.",
                request
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(ResourceLockedException.class)
    public ResponseEntity<ErrorResponse> handleResourceLocked(ResourceLockedException ex, WebRequest request) {
        log.warn("Resource locked: {}", ex.getMessage());

        ErrorResponse errorResponse = createErrorResponse(
                HttpStatus.LOCKED,
                ex.getMessage(),
                "RESOURCE_LOCKED",
                String.format("Resource: %s, Lock Type: %s", ex.getResourceId(), ex.getLockType()),
                request
        );

        return ResponseEntity.status(HttpStatus.LOCKED).body(errorResponse);
    }

    // ========== RATE LIMITING & TIMEOUTS ==========

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleRateLimitExceeded(RateLimitExceededException ex, WebRequest request) {
        log.warn("Rate limit exceeded for client: {}", ex.getClientId());

        ErrorResponse errorResponse = createErrorResponse(
                HttpStatus.TOO_MANY_REQUESTS,
                ex.getMessage(),
                "RATE_LIMIT_EXCEEDED",
                "Client: " + ex.getClientId(),
                request
        );

        // Add retry-after header
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", String.valueOf(ex.getRetryAfter().getSeconds()))
                .body(errorResponse);
    }

    @ExceptionHandler(TimeoutException.class)
    public ResponseEntity<ErrorResponse> handleTimeout(TimeoutException ex, WebRequest request) {
        log.error("Operation timeout: {}", ex.getMessage());

        ErrorResponse errorResponse = createErrorResponse(
                HttpStatus.REQUEST_TIMEOUT,
                "Operation took too long to complete",
                "OPERATION_TIMEOUT",
                "The booking request timed out. Please try again.",
                request
        );

        return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(errorResponse);
    }

    // ========== VALIDATION ERRORS ==========

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
        log.error("Validation failed: {}", ex.getMessage());

        List<ErrorResponse.ValidationError> validationErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new ErrorResponse.ValidationError(
                        error.getField(),
                        error.getRejectedValue(),
                        error.getDefaultMessage(),
                        error.getCode()
                ))
                .collect(Collectors.toList());

        ErrorResponse errorResponse = createErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Validation failed",
                "VALIDATION_ERROR",
                "Request contains invalid data",
                request
        );
        errorResponse.setValidationErrors(validationErrors);

        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception ex, WebRequest request) {
        log.error("Bad request: {}", ex.getMessage());

        String message = ex instanceof MethodArgumentTypeMismatchException ?
                "Invalid parameter type: " + ((MethodArgumentTypeMismatchException) ex).getName() :
                ex.getMessage();

        ErrorResponse errorResponse = createErrorResponse(
                HttpStatus.BAD_REQUEST,
                message,
                "BAD_REQUEST",
                "Please check your request parameters and try again",
                request
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler({MissingServletRequestParameterException.class, MissingRequestHeaderException.class})
    public ResponseEntity<ErrorResponse> handleMissingParameters(Exception ex, WebRequest request) {
        log.error("Missing required parameter: {}", ex.getMessage());

        ErrorResponse errorResponse = createErrorResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                "MISSING_PARAMETER",
                "A required request parameter or header is missing",
                request
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMalformedJson(HttpMessageNotReadableException ex, WebRequest request) {
        log.error("Malformed JSON request: {}", ex.getMessage());

        ErrorResponse errorResponse = createErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Invalid JSON format",
                "MALFORMED_JSON",
                "Request body contains invalid JSON syntax",
                request
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    // ========== HTTP METHOD & ROUTING ERRORS ==========

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex, WebRequest request) {
        log.error("Method not allowed: {}", ex.getMessage());

        ErrorResponse errorResponse = createErrorResponse(
                HttpStatus.METHOD_NOT_ALLOWED,
                String.format("Method %s not supported", ex.getMethod()),
                "METHOD_NOT_ALLOWED",
                "Supported methods: " + String.join(", ", ex.getSupportedMethods()),
                request
        );

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(errorResponse);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NoHandlerFoundException ex, WebRequest request) {
        log.error("Endpoint not found: {}", ex.getMessage());

        ErrorResponse errorResponse = createErrorResponse(
                HttpStatus.NOT_FOUND,
                "Endpoint not found",
                "ENDPOINT_NOT_FOUND",
                String.format("No handler found for %s %s", ex.getHttpMethod(), ex.getRequestURL()),
                request
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    // ========== DATABASE ERRORS ==========

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex, WebRequest request) {
        log.error("Data integrity violation: {}", ex.getMessage());

        String userMessage = "Data constraint violation";
        String details = "The operation violates data integrity constraints";

        // Try to provide more specific error messages
        if (ex.getMessage() != null) {
            if (ex.getMessage().contains("unique constraint") || ex.getMessage().contains("duplicate key")) {
                userMessage = "Duplicate data detected";
                details = "The provided data already exists in the system";
            } else if (ex.getMessage().contains("foreign key constraint")) {
                userMessage = "Invalid reference";
                details = "Referenced data does not exist";
            }
        }

        ErrorResponse errorResponse = createErrorResponse(
                HttpStatus.CONFLICT,
                userMessage,
                "DATA_INTEGRITY_VIOLATION",
                details,
                request
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> handleDataAccessException(DataAccessException ex, WebRequest request) {
        log.error("Database access error: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = createErrorResponse(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Database service temporarily unavailable",
                "DATABASE_ERROR",
                "Please try again later. If the problem persists, contact support.",
                request
        );

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
    }

    // ========== GENERIC FALLBACK ==========

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, WebRequest request) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = createErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred",
                "INTERNAL_SERVER_ERROR",
                "Please try again later or contact support if the issue persists",
                request
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    // ========== UTILITY METHODS ==========

    private ErrorResponse createErrorResponse(HttpStatus status, String message, String errorCode,
                                            String details, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setTimestamp(ZonedDateTime.now());
        errorResponse.setStatus(status.value());
        errorResponse.setError(status.getReasonPhrase());
        errorResponse.setMessage(message);
        errorResponse.setErrorCode(errorCode);
        errorResponse.setDetails(details);
        errorResponse.setPath(getPath(request));

        // Add trace ID from request headers (if present)
        if (request instanceof ServletWebRequest) {
            HttpServletRequest httpRequest = ((ServletWebRequest) request).getRequest();
            String traceId = httpRequest.getHeader("X-Trace-ID");
            if (traceId != null) {
                errorResponse.setTraceId(traceId);
            }
        }

        return errorResponse;
    }

    private String getPath(WebRequest request) {
        if (request instanceof ServletWebRequest) {
            return ((ServletWebRequest) request).getRequest().getRequestURI();
        }
        return "unknown";
    }
}