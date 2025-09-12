package com.atlan.evently.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

/**
 * Standardized error response structure for all API endpoints.
 * Provides comprehensive error information for client debugging and user feedback.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private ZonedDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String errorCode;
    private String details;
    private String path;
    
    // Additional fields for specific error types
    private String traceId;
    private List<ValidationError> validationErrors;
    private Map<String, Object> metadata;

    public ErrorResponse() {
        this.timestamp = ZonedDateTime.now();
    }

    public ErrorResponse(ZonedDateTime timestamp, int status, String error, String message, 
                        String errorCode, String details, String path) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.errorCode = errorCode;
        this.details = details;
        this.path = path;
    }

    // Getters and Setters
    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(ZonedDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public List<ValidationError> getValidationErrors() {
        return validationErrors;
    }

    public void setValidationErrors(List<ValidationError> validationErrors) {
        this.validationErrors = validationErrors;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    /**
     * Nested class for validation error details
     */
    public static class ValidationError {
        private String field;
        private Object rejectedValue;
        private String message;
        private String errorCode;

        public ValidationError() {}

        public ValidationError(String field, Object rejectedValue, String message, String errorCode) {
            this.field = field;
            this.rejectedValue = rejectedValue;
            this.message = message;
            this.errorCode = errorCode;
        }

        // Getters and Setters
        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public Object getRejectedValue() {
            return rejectedValue;
        }

        public void setRejectedValue(Object rejectedValue) {
            this.rejectedValue = rejectedValue;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getErrorCode() {
            return errorCode;
        }

        public void setErrorCode(String errorCode) {
            this.errorCode = errorCode;
        }
    }
}