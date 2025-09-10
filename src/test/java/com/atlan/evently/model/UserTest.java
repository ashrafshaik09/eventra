package com.atlan.evently.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id("1")
                .email("user@example.com")
                .name("John Doe")
                .createdAt(ZonedDateTime.now())
                .build();
    }

    @Test
    void testValidUserCreation() {
        assertNotNull(user);
        assertEquals("user@example.com", user.getEmail());
        assertEquals("John Doe", user.getName());
    }

    @Test
    void testSettersUpdateFields() {
        user.setEmail("newuser@example.com");
        user.setName("Jane Doe");
        assertEquals("newuser@example.com", user.getEmail());
        assertEquals("Jane Doe", user.getName());
    }
}