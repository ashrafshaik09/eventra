package com.atlan.evently.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import static org.junit.jupiter.api.Assertions.*;

class BookingRequestTest {

    private Validator validator;
    private BookingRequest bookingRequest;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        bookingRequest = new BookingRequest();
    }

    @Test
    void testValidBookingRequest() {
        bookingRequest.setUserId("1");
        bookingRequest.setEventId("1");
        bookingRequest.setQuantity(2);
        bookingRequest.setIdempotencyKey("unique-key-123");

        var violations = validator.validate(bookingRequest);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testMissingUserIdThrowsValidationError() {
        bookingRequest.setEventId("1");
        bookingRequest.setQuantity(2);

        var violations = validator.validate(bookingRequest);
        assertFalse(violations.isEmpty());
        assertEquals("User ID is required", violations.iterator().next().getMessage());
    }

    @Test
    void testNegativeQuantityThrowsValidationError() {
        bookingRequest.setUserId("1");
        bookingRequest.setEventId("1");
        bookingRequest.setQuantity(-1);

        var violations = validator.validate(bookingRequest);
        assertFalse(violations.isEmpty());
        assertEquals("Quantity must be positive", violations.iterator().next().getMessage());
    }
}