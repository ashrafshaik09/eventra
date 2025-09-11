package com.atlan.evently.dto;

import lombok.Data;
import java.util.List;

@Data
public class AnalyticsResponse {
    private Long totalBookings;
    private Long totalCapacity;
    private String utilizationPercentage;
    
    // Enhanced analytics
    private List<PopularEventResponse> mostPopularEvents;
    private Long totalEvents;
    private Long soldOutEvents;

    @Data
    public static class PopularEventResponse {
        private String eventId;
        private String eventName;
        private String venue;
        private Long totalBookings;
        private Integer capacity;
        private String utilizationPercentage;
    }
}