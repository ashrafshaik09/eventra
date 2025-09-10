package com.atlan.evently.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EventRequestTest {

    private Validator validator;
    private EventRequest eventRequest;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        eventRequest = new EventRequest();
    }

    @Test
    void testValidEventRequest() {
        eventRequest.setEventName("Concert 2025");
        eventRequest.setVenue("City Hall");
        eventRequest.setStartTime(ZonedDateTime.now().plusDays(1));
        eventRequest.setCapacity(100);

        var violations = validator.validate(eventRequest);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testInvalidEventNameThrowsValidationError() {
        eventRequest.setVenue("City Hall");
        eventRequest.setStartTime(ZonedDateTime.now().plusDays(1));
        eventRequest.setCapacity(100);

        var violations = validator.validate(eventRequest);
        assertFalse(violations.isEmpty());
        assertEquals("Event name is required", violations.iterator().next().getMessage());
    }

    @Test
    void testNegativeCapacityThrowsValidationError() {
        eventRequest.setEventName("Concert 2025");
        eventRequest.setVenue("City Hall");
        eventRequest.setStartTime(ZonedDateTime.now().plusDays(1));
        eventRequest.setCapacity(-1);

        var violations = validator.validate(eventRequest);
        assertFalse(violations.isEmpty());
        assertEquals("Capacity must be positive", violations.iterator().next().getMessage());
    }
}