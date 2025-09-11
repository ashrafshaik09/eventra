package com.atlan.evently.exception;

import com.atlan.evently.dto.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.mock.web.MockHttpServletRequest;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;
    private ServletWebRequest webRequest;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = new MockHttpServletRequest("POST", "/api/v1/bookings");
        webRequest = new ServletWebRequest(request);
    }

    @Test
    void testHandleEventException() {
        EventException ex = new EventException("Event sold out", "EVENT_SOLD_OUT", "No seats available");

        ResponseEntity<ErrorResponse> response = handler.handleEventException(ex, webRequest);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        ErrorResponse error = response.getBody();
        assertNotNull(error);
        assertEquals(409, error.getStatus());
        assertEquals("Conflict", error.getError());
        assertEquals("Event sold out", error.getMessage());
        assertEquals("EVENT_SOLD_OUT", error.getErrorCode());
        assertEquals("No seats available", error.getDetails());
        assertNotNull(error.getTimestamp());
        assertEquals("/api/v1/bookings", error.getPath());
    }

    @Test
    void testHandleBookingConflictException() {
        BookingConflictException ex = new BookingConflictException(
                "Insufficient seats", "Requested: 5, Available: 2");

        ResponseEntity<ErrorResponse> response = handler.handleBookingConflict(ex, webRequest);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        ErrorResponse error = response.getBody();
        assertNotNull(error);
        assertEquals("BOOKING_CONFLICT", error.getErrorCode());
        assertEquals("Insufficient seats", error.getMessage());
        assertEquals("Requested: 5, Available: 2", error.getDetails());
    }

    @Test
    void testHandleDuplicateBookingException() {
        DuplicateBookingException ex = new DuplicateBookingException(
                "Duplicate booking detected", "IdempotencyKey: abc123");

        ResponseEntity<ErrorResponse> response = handler.handleDuplicateBooking(ex, webRequest);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        ErrorResponse error = response.getBody();
        assertNotNull(error);
        assertEquals("DUPLICATE_BOOKING", error.getErrorCode());
    }

    @Test
    void testHandleUserNotFoundException() {
        UserNotFoundException ex = new UserNotFoundException("user123");

        ResponseEntity<ErrorResponse> response = handler.handleUserNotFound(ex, webRequest);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ErrorResponse error = response.getBody();
        assertNotNull(error);
        assertEquals("USER_NOT_FOUND", error.getErrorCode());
        assertTrue(error.getDetails().contains("user123"));
    }

    @Test
    void testHandleUnauthorizedException() {
        UnauthorizedException ex = new UnauthorizedException("Invalid token", "Token expired");

        ResponseEntity<ErrorResponse> response = handler.handleUnauthorized(ex, webRequest);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        ErrorResponse error = response.getBody();
        assertNotNull(error);
        assertEquals("UNAUTHORIZED", error.getErrorCode());
        assertEquals("Token expired", error.getDetails());
    }

    @Test
    void testHandleForbiddenException() {
        ForbiddenException ex = new ForbiddenException("Access denied", "ADMIN", "USER");

        ResponseEntity<ErrorResponse> response = handler.handleForbidden(ex, webRequest);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        ErrorResponse error = response.getBody();
        assertNotNull(error);
        assertEquals("FORBIDDEN", error.getErrorCode());
        assertTrue(error.getDetails().contains("ADMIN"));
        assertTrue(error.getDetails().contains("USER"));
    }

    @Test
    void testHandleAccessDeniedException() {
        AccessDeniedException ex = new AccessDeniedException("Access is denied");

        ResponseEntity<ErrorResponse> response = handler.handleForbidden(ex, webRequest);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        ErrorResponse error = response.getBody();
        assertNotNull(error);
        assertEquals("FORBIDDEN", error.getErrorCode());
    }

    @Test
    void testHandleOptimisticLockingFailure() {
        OptimisticLockingFailureException ex = new OptimisticLockingFailureException("Version conflict");

        ResponseEntity<ErrorResponse> response = handler.handleOptimisticLockingFailure(ex, webRequest);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        ErrorResponse error = response.getBody();
        assertNotNull(error);
        assertEquals("OPTIMISTIC_LOCK_FAILURE", error.getErrorCode());
        assertTrue(error.getDetails().contains("refresh and try again"));
    }

    @Test
    void testHandleResourceLockedException() {
        ResourceLockedException ex = new ResourceLockedException("event123", "SEAT_RESERVATION");

        ResponseEntity<ErrorResponse> response = handler.handleResourceLocked(ex, webRequest);

        assertEquals(HttpStatus.LOCKED, response.getStatusCode());
        ErrorResponse error = response.getBody();
        assertNotNull(error);
        assertEquals("RESOURCE_LOCKED", error.getErrorCode());
        assertTrue(error.getDetails().contains("event123"));
        assertTrue(error.getDetails().contains("SEAT_RESERVATION"));
    }

    @Test
    void testHandleRateLimitExceededException() {
        RateLimitExceededException ex = new RateLimitExceededException("client123", Duration.ofSeconds(60));

        ResponseEntity<ErrorResponse> response = handler.handleRateLimitExceeded(ex, webRequest);

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        ErrorResponse error = response.getBody();
        assertNotNull(error);
        assertEquals("RATE_LIMIT_EXCEEDED", error.getErrorCode());
        assertTrue(error.getDetails().contains("client123"));
        assertEquals("60", response.getHeaders().getFirst("Retry-After"));
    }

    @Test
    void testHandleTimeoutException() {
        TimeoutException ex = new TimeoutException("Operation timed out");

        ResponseEntity<ErrorResponse> response = handler.handleTimeout(ex, webRequest);

        assertEquals(HttpStatus.REQUEST_TIMEOUT, response.getStatusCode());
        ErrorResponse error = response.getBody();
        assertNotNull(error);
        assertEquals("OPERATION_TIMEOUT", error.getErrorCode());
    }

    @Test
    void testHandleValidationExceptions() {
        BindException bindException = new BindException("target", "objectName");
        bindException.addError(new FieldError("booking", "quantity", 0, false, 
                new String[]{"Positive"}, null, "must be positive"));
        bindException.addError(new FieldError("booking", "eventId", "", false, 
                new String[]{"NotBlank"}, null, "must not be blank"));
        
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindException);

        ResponseEntity<ErrorResponse> response = handler.handleValidationExceptions(ex, webRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorResponse error = response.getBody();
        assertNotNull(error);
        assertEquals("VALIDATION_ERROR", error.getErrorCode());
        assertNotNull(error.getValidationErrors());
        assertEquals(2, error.getValidationErrors().size());
        
        ErrorResponse.ValidationError quantityError = error.getValidationErrors().stream()
                .filter(ve -> "quantity".equals(ve.getField()))
                .findFirst().orElse(null);
        assertNotNull(quantityError);
        assertEquals(0, quantityError.getRejectedValue());
        assertEquals("must be positive", quantityError.getMessage());
    }

    @Test
    void testHandleIllegalArgumentException() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid UUID format");

        ResponseEntity<ErrorResponse> response = handler.handleBadRequest(ex, webRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorResponse error = response.getBody();
        assertNotNull(error);
        assertEquals("BAD_REQUEST", error.getErrorCode());
        assertEquals("Invalid UUID format", error.getMessage());
    }

    @Test
    void testHandleMissingServletRequestParameterException() {
        MissingServletRequestParameterException ex = new MissingServletRequestParameterException(
                "eventId", "String");

        ResponseEntity<ErrorResponse> response = handler.handleMissingParameters(ex, webRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorResponse error = response.getBody();
        assertNotNull(error);
        assertEquals("MISSING_PARAMETER", error.getErrorCode());
        assertTrue(error.getMessage().contains("eventId"));
    }
    
    @Test
    void testHandleDataIntegrityViolationException() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException(
                "duplicate key value violates unique constraint");

        ResponseEntity<ErrorResponse> response = handler.handleDataIntegrityViolation(ex, webRequest);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        ErrorResponse error = response.getBody();
        assertNotNull(error);
        assertEquals("DATA_INTEGRITY_VIOLATION", error.getErrorCode());
        assertEquals("Duplicate data detected", error.getMessage());
    }

    @Test
    void testHandleGlobalException() {
        RuntimeException ex = new RuntimeException("Unexpected error");

        ResponseEntity<ErrorResponse> response = handler.handleGlobalException(ex, webRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ErrorResponse error = response.getBody();
        assertNotNull(error);
        assertEquals("INTERNAL_SERVER_ERROR", error.getErrorCode());
        assertTrue(error.getDetails().contains("contact support"));
    }

    @Test
    void testTraceIdHandling() {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest("POST", "/api/v1/bookings");
        mockRequest.addHeader("X-Trace-ID", "trace-123");
        ServletWebRequest mockWebRequest = new ServletWebRequest(mockRequest);

        EventException ex = new EventException("Test", "TEST_CODE", "Test details");

        ResponseEntity<ErrorResponse> response = handler.handleEventException(ex, mockWebRequest);

        ErrorResponse error = response.getBody();
        assertNotNull(error);
        assertEquals("trace-123", error.getTraceId());
    }
}