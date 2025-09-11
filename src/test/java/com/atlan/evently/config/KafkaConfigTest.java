package com.atlan.evently.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class KafkaConfigTest {

    @Autowired
    private KafkaConfig kafkaConfig;

    @Test
    void testKafkaAdminBeanCreation() {
        assertNotNull(kafkaConfig.kafkaAdmin());
    }

    @Test
    void testBookingEventsTopicCreation() {
        var topic = kafkaConfig.bookingEventsTopic();
        assertNotNull(topic);
        assertEquals("booking-events", topic.name());
        assertEquals(3, topic.numPartitions());
        assertEquals(1, topic.replicationFactor());
    }
}