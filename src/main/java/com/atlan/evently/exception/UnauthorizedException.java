package com.atlan.evently.exception;

/**
 * Exception thrown when authentication is required but missing or invalid.
 * 
 * Mapped to HTTP 401 Unauthorized status code.
 */
public class UnauthorizedException extends RuntimeException {

    private final String details;

    public UnauthorizedException(String message) {
        super(message);
        this.details = "Authentication required";
    }

    public UnauthorizedException(String message, String details) {
        super(message);
        this.details = details;
    }

    public String getDetails() {
        return details;
    }
}
