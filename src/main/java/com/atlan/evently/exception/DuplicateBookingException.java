package com.atlan.evently.exception;

/**
 * Exception thrown when attempting to create a duplicate booking.
 * This occurs when idempotency keys are reused or users try to book the same event twice.
 * 
 * Mapped to HTTP 409 Conflict status code.
 */
public class DuplicateBookingException extends RuntimeException {

    private final String details;

    public DuplicateBookingException(String message, String details) {
        super(message);
        this.details = details;
    }

    public DuplicateBookingException(String message, String details, Throwable cause) {
        super(message, cause);
        this.details = details;
    }

    public String getDetails() {
        return details;
    }
}
