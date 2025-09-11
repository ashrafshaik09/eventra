package com.atlan.evently.exception;

public class EventException extends RuntimeException {

    private final String errorCode;
    private final String details;

    public EventException(String message, String errorCode, String details) {
        super(message);
        this.errorCode = errorCode;
        this.details = details;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getDetails() {
        return details;
    }
}