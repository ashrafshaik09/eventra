package com.atlan.evently.dto;

public class AnalyticsResponse {

    private Long totalBookings;
    private Long totalCapacity;
    private String utilizationPercentage;

    public Long getTotalBookings() {
        return totalBookings;
    }

    public void setTotalBookings(Long totalBookings) {
        this.totalBookings = totalBookings;
    }

    public Long getTotalCapacity() {
        return totalCapacity;
    }

    public void setTotalCapacity(Long totalCapacity) {
        this.totalCapacity = totalCapacity;
    }

    public String getUtilizationPercentage() {
        return utilizationPercentage;
    }

    public void setUtilizationPercentage(String utilizationPercentage) {
        this.utilizationPercentage = utilizationPercentage;
    }
}