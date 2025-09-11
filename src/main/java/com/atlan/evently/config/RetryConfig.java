package com.atlan.evently.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

@Configuration
@EnableRetry
public class RetryConfig {
    // Spring Retry will automatically configure retry functionality
    // This enables @Retryable and @Recover annotations across the application
}
