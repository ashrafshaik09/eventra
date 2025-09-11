package com.atlan.evently.controller;

import com.atlan.evently.dto.UserRegistrationRequest;
import com.atlan.evently.dto.UserResponse;
import com.atlan.evently.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.ZonedDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserControllerTest {

    private MockMvc mockMvc;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        UserController userController = new UserController(userService);
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    @Test
    void testRegisterUserReturnsCreated() throws Exception {
        UserResponse mockResponse = new UserResponse();
        mockResponse.setUserId("123e4567-e89b-12d3-a456-426614174000");
        mockResponse.setName("John Doe");
        mockResponse.setEmail("john@example.com");
        mockResponse.setRole("USER");
        mockResponse.setIsActive(true);
        mockResponse.setCreatedAt(ZonedDateTime.now());

        when(userService.registerUser(any(UserRegistrationRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"John Doe\",\"email\":\"john@example.com\",\"password\":\"password123\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value("123e4567-e89b-12d3-a456-426614174000"))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.role").value("USER"));

        verify(userService, times(1)).registerUser(any(UserRegistrationRequest.class));
    }

    @Test
    void testGetUserByIdReturnsOk() throws Exception {
        String userId = "123e4567-e89b-12d3-a456-426614174000";
        UserResponse mockResponse = new UserResponse();
        mockResponse.setUserId(userId);
        mockResponse.setName("John Doe");
        mockResponse.setEmail("john@example.com");

        when(userService.getUserById(userId)).thenReturn(mockResponse);

        mockMvc.perform(get("/api/v1/users/" + userId))
                .andExpected(status().isOk())
                .andExpected(jsonPath("$.userId").value(userId))
                .andExpected(jsonPath("$.name").value("John Doe"));

        verify(userService, times(1)).getUserById(userId);
    }

    @Test
    void testIsEmailRegisteredReturnsBoolean() throws Exception {
        String email = "john@example.com";
        when(userService.isEmailRegistered(email)).thenReturn(true);

        mockMvc.perform(get("/api/v1/users/check-email/" + email))
                .andExpected(status().isOk())
                .andExpected(content().string("true"));

        verify(userService, times(1)).isEmailRegistered(email);
    }
}
