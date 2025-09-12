package com.atlan.evently.config;

import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HealthConfig {

    // Optional: Add custom health indicators if needed
    // For now, rely on auto-configured indicators for DB, Redis, Kafka
}
