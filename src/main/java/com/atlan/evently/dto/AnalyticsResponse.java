package com.atlan.evently.dto;

import lombok.Data;

@Data
public class AnalyticsResponse {
    private Long totalBookings;
    private Long totalCapacity;
    private String utilizationPercentage;
}