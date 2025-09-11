package com.atlan.evently.service;

import com.atlan.evently.dto.UserRegistrationRequest;
import com.atlan.evently.dto.UserResponse;
import com.atlan.evently.exception.EventException;
import com.atlan.evently.mapper.UserMapper;
import com.atlan.evently.model.User;
import com.atlan.evently.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional
    public UserResponse registerUser(UserRegistrationRequest request) {
        validateRegistrationRequest(request);
        
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EventException("Email already registered", 
                    "USER_EMAIL_EXISTS", 
                    "A user with email " + request.getEmail() + " already exists");
        }

        // Create and save user
        User user = userMapper.toEntity(request);
        User savedUser = userRepository.save(user);
        
        return userMapper.toResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(String userId) {
        UUID uuid = parseUUID(userId, "User ID");
        User user = userRepository.findById(uuid)
                .orElseThrow(() -> new EventException("User not found", 
                        "USER_NOT_FOUND", 
                        "User with ID " + userId + " does not exist"));
        
        return userMapper.toResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EventException("User not found", 
                        "USER_NOT_FOUND", 
                        "User with email " + email + " does not exist"));
        
        return userMapper.toResponse(user);
    }

    @Transactional(readOnly = true)
    public boolean isEmailRegistered(String email) {
        return userRepository.existsByEmail(email);
    }

    private void validateRegistrationRequest(UserRegistrationRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (!isValidEmail(request.getEmail())) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    private UUID parseUUID(String id, String fieldName) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid " + fieldName + " format: " + id);
        }
    }
}
