package com.atlan.evently.util;

public class Constants {

    private Constants() {
        throw new IllegalStateException("Utility class");
    }

    // Error Codes
    public static final String ERROR_CODE_EVENT_SOLD_OUT = "EVENT_001";
    public static final String ERROR_CODE_VALIDATION_FAILED = "VALIDATION_001";
    public static final String ERROR_CODE_INTERNAL_ERROR = "INTERNAL_001";
    public static final String ERROR_CODE_INVALID_UUID = "UUID_001";

    // Booking Statuses
    public static final String STATUS_CONFIRMED = "CONFIRMED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    // API Paths
    public static final String API_BASE_PATH = "/api/v1";
    public static final String EVENTS_PATH = API_BASE_PATH + "/events";
    public static final String BOOKINGS_PATH = API_BASE_PATH + "/bookings";
    public static final String ADMIN_EVENTS_PATH = API_BASE_PATH + "/admin/events";

    // Validation Messages
    public static final String MSG_EVENT_NAME_REQUIRED = "Event name is required";
    public static final String MSG_CAPACITY_POSITIVE = "Capacity must be positive";
    public static final String MSG_INVALID_UUID_FORMAT = "Invalid UUID format";
}