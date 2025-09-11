package com.atlan.evently.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidationUtilsTest {

    @Test
    void testIsNullOrEmpty() {
        assertTrue(ValidationUtils.isNullOrEmpty(null));
        assertTrue(ValidationUtils.isNullOrEmpty(""));
        assertTrue(ValidationUtils.isNullOrEmpty(" "));
        assertFalse(ValidationUtils.isNullOrEmpty("test"));
    }

    @Test
    void testValidateNotNullOrEmpty() {
        assertDoesNotThrow(() -> ValidationUtils.validateNotNullOrEmpty("test", "field"));
        assertThrows(IllegalArgumentException.class, () -> ValidationUtils.validateNotNullOrEmpty(null, "field"));
        assertThrows(IllegalArgumentException.class, () -> ValidationUtils.validateNotNullOrEmpty("", "field"));
        assertThrows(IllegalArgumentException.class, () -> ValidationUtils.validateNotNullOrEmpty(" ", "field"));
    }

    @Test
    void testValidatePositive() {
        assertDoesNotThrow(() -> ValidationUtils.validatePositive(1, "quantity"));
        assertDoesNotThrow(() -> ValidationUtils.validatePositive(100, "quantity"));
        assertThrows(IllegalArgumentException.class, () -> ValidationUtils.validatePositive(null, "quantity"));
        assertThrows(IllegalArgumentException.class, () -> ValidationUtils.validatePositive(0, "quantity"));
        assertThrows(IllegalArgumentException.class, () -> ValidationUtils.validatePositive(-1, "quantity"));
    }
}