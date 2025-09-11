package com.atlan.evently.exception;

import com.atlan.evently.dto.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.mock.web.MockHttpServletRequest;

import jakarta.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = new MockHttpServletRequest();
    }

    @Test
    void testHandleEventException() {
        EventException ex = new EventException("Event sold out", "EVENT_001", "No seats available");
        ServletWebRequest webRequest = new ServletWebRequest(request);

        ResponseEntity<ErrorResponse> response = handler.handleEventException(ex, webRequest);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        ErrorResponse error = response.getBody();
        assertNotNull(error);
        assertEquals("Conflict", error.getError());
        assertEquals("Event sold out", error.getMessage());
        assertEquals("EVENT_001", error.getErrorCode());
        assertEquals("No seats available", error.getDetails());
        assertNotNull(error.getTimestamp());
    }

    @Test
    void testHandleValidationExceptions() {
        BindException bindException = new BindException("target", "objectName");
        bindException.addError(new FieldError("object", "eventName", "Event name is required"));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindException);
        ServletWebRequest webRequest = new ServletWebRequest(request);

        ResponseEntity<ErrorResponse> response = handler.handleValidationExceptions(ex, webRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorResponse error = response.getBody();
        assertNotNull(error);
        assertEquals("Bad Request", error.getError());
        assertEquals("Validation failed", error.getMessage());
        assertEquals("VALIDATION_001", error.getErrorCode());
        assertTrue(error.getDetails().contains("eventName: Event name is required"));
    }

    @Test
    void testHandleGlobalException() {
        Exception ex = new RuntimeException("Unexpected error");
        ServletWebRequest webRequest = new ServletWebRequest(request);

        ResponseEntity<ErrorResponse> response = handler.handleGlobalException(ex, webRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ErrorResponse error = response.getBody();
        assertNotNull(error);
        assertEquals("Internal Server Error", error.getError());
        assertEquals("Unexpected error", error.getMessage());
        assertEquals("INTERNAL_001", error.getErrorCode());
        assertEquals("An unexpected error occurred", error.getDetails());
    }
}