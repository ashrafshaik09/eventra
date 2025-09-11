package com.atlan.evently.exception;

/**
 * Exception thrown when a resource is temporarily locked due to concurrent operations.
 * Indicates client should retry after a brief delay.
 * 
 * Mapped to HTTP 423 Locked status code.
 */
public class ResourceLockedException extends RuntimeException {

    private final String resourceId;
    private final String lockType;

    public ResourceLockedException(String resourceId, String lockType) {
        super(String.format("Resource %s is temporarily locked (%s). Please retry.", resourceId, lockType));
        this.resourceId = resourceId;
        this.lockType = lockType;
    }

    public String getResourceId() {
        return resourceId;
    }

    public String getLockType() {
        return lockType;
    }
}
