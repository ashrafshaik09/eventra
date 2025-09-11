package com.atlan.evently.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        // Using default bootstrap servers from application.yml
        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic bookingEventsTopic() {
        return TopicBuilder.name("booking-events")
                .partitions(3)
                .replicas(1)
                .build();
    }
}