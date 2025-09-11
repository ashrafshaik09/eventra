package com.atlan.evently.exception;

/**
 * Exception thrown when booking conflicts occur during concurrent booking attempts.
 * This includes scenarios like insufficient seats, event sold out, or timing conflicts.
 * 
 * Mapped to HTTP 409 Conflict status code.
 */
public class BookingConflictException extends RuntimeException {

    private final String details;

    public BookingConflictException(String message, String details) {
        super(message);
        this.details = details;
    }

    public BookingConflictException(String message, String details, Throwable cause) {
        super(message, cause);
        this.details = details;
    }

    public String getDetails() {
        return details;
    }
}
