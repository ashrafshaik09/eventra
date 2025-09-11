package com.atlan.evently.controller;

import com.atlan.evently.dto.UserRegistrationRequest;
import com.atlan.evently.dto.UserResponse;
import com.atlan.evently.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "User management endpoints")
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    @Operation(
        summary = "Register new user",
        description = "Create a new user account for booking tickets"
    )
    @ApiResponse(responseCode = "201", description = "User successfully registered")
    @ApiResponse(responseCode = "400", description = "Invalid registration data")
    @ApiResponse(responseCode = "409", description = "Email already registered")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody UserRegistrationRequest request) {
        UserResponse response = userService.registerUser(request);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/{userId}")
    @Operation(
        summary = "Get user profile",
        description = "Retrieve user profile information"
    )
    @ApiResponse(responseCode = "200", description = "Successfully retrieved user profile")
    @ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<UserResponse> getUserById(
            @Parameter(description = "User ID") @PathVariable String userId) {
        UserResponse user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/check-email/{email}")
    @Operation(
        summary = "Check email availability",
        description = "Check if an email address is already registered"
    )
    @ApiResponse(responseCode = "200", description = "Email availability check completed")
    public ResponseEntity<Boolean> isEmailRegistered(
            @Parameter(description = "Email address to check") @PathVariable String email) {
        boolean isRegistered = userService.isEmailRegistered(email);
        return ResponseEntity.ok(isRegistered);
    }
}
