package com.atlan.evently.exception;

/**
 * Exception thrown when a requested user cannot be found.
 * 
 * Mapped to HTTP 404 Not Found status code.
 */
public class UserNotFoundException extends RuntimeException {

    private final String userId;

    public UserNotFoundException(String userId) {
        super("User not found with ID: " + userId);
        this.userId = userId;
    }

    public UserNotFoundException(String userId, Throwable cause) {
        super("User not found with ID: " + userId, cause);
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }
}
