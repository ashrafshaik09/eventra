package com.atlan.evently.exception;

/**
 * Exception thrown when user is authenticated but lacks required permissions.
 * 
 * Mapped to HTTP 403 Forbidden status code.
 */
public class ForbiddenException extends RuntimeException {

    private final String requiredRole;
    private final String userRole;

    public ForbiddenException(String message, String requiredRole, String userRole) {
        super(message);
        this.requiredRole = requiredRole;
        this.userRole = userRole;
    }

    public String getRequiredRole() {
        return requiredRole;
    }

    public String getUserRole() {
        return userRole;
    }
}
