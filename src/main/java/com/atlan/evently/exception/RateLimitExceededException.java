package com.atlan.evently.exception;

import java.time.Duration;

/**
 * Exception thrown when API rate limits are exceeded.
 * 
 * Mapped to HTTP 429 Too Many Requests status code.
 */
public class RateLimitExceededException extends RuntimeException {

    private final String clientId;
    private final Duration retryAfter;

    public RateLimitExceededException(String clientId, Duration retryAfter) {
        super("Rate limit exceeded. Retry after " + retryAfter.getSeconds() + " seconds.");
        this.clientId = clientId;
        this.retryAfter = retryAfter;
    }

    public String getClientId() {
        return clientId;
    }

    public Duration getRetryAfter() {
        return retryAfter;
    }
}
