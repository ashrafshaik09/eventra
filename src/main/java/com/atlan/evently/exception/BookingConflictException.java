package com.atlan.evently.exception;

public class BookingConflictException extends RuntimeException {
    
    private final String details;
    
    public BookingConflictException(String message, String details) {
        super(message);
        this.details = details;
    }
    
    public String getDetails() {
        return details;
    }
}
