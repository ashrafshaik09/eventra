package com.atlan.evently.exception;

public class DuplicateBookingException extends RuntimeException {
    
    private final String details;
    
    public DuplicateBookingException(String message, String details) {
        super(message);
        this.details = details;
    }
    
    public String getDetails() {
        return details;
    }
}
