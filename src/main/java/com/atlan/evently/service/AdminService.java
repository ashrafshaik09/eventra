package com.atlan.evently.service;

import com.atlan.evently.dto.AnalyticsResponse;
import com.atlan.evently.dto.BookingResponse;
import com.atlan.evently.dto.EventRequest;
import com.atlan.evently.dto.EventResponse;
import com.atlan.evently.dto.UserRegistrationRequest;
import com.atlan.evently.dto.UserResponse;
import com.atlan.evently.dto.UserUpdateRequest;
import com.atlan.evently.exception.EventException;
import com.atlan.evently.mapper.BookingMapper;
import com.atlan.evently.mapper.EventMapper;
import com.atlan.evently.mapper.UserMapper;
import com.atlan.evently.model.Booking;
import com.atlan.evently.model.Event;
import com.atlan.evently.model.User;
import com.atlan.evently.repository.BookingRepository;
import com.atlan.evently.repository.EventRepository;
import com.atlan.evently.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class AdminService {

    private final EventRepository eventRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final EventMapper eventMapper;
    private final BookingMapper bookingMapper;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final EventService eventService; // Add reference to EventService

    @Transactional
    public EventResponse createEvent(EventRequest request) {
        validateEventRequest(request);
        Event event = eventMapper.toEntity(request);
        Event savedEvent = eventRepository.save(event);
        
        // Invalidate events cache when new event is created
        eventService.evictEventCaches();
        
        return eventMapper.toResponse(savedEvent);
    }

    @Transactional
    public EventResponse updateEvent(String eventId, EventRequest request) {
        validateEventRequest(request);
        UUID uuid = parseUUID(eventId, "Event ID");
        Event existingEvent = eventRepository.findById(uuid)
                .orElseThrow(() -> new EventException("Event not found",
                        "EVENT_NOT_FOUND",
                        "Event with ID " + eventId + " does not exist"));

        // Update fields while preserving metadata
        existingEvent.setName(request.getEventName().trim());
        existingEvent.setVenue(request.getVenue().trim());
        existingEvent.setStartsAt(request.getStartTime());
        existingEvent.setCapacity(request.getCapacity());
        existingEvent.setAvailableSeats(request.getCapacity()); // Reset available seats for simplicity

        Event savedEvent = eventRepository.save(existingEvent);
        
        // Invalidate specific event cache and events list
        eventService.evictEventCache(eventId);
        eventService.evictEventCaches();
        
        return eventMapper.toResponse(savedEvent);
    }

    @Transactional(readOnly = true)
    public AnalyticsResponse getAnalytics() {
        long totalBookings = bookingRepository.count();
        long totalCapacity = eventRepository.count() > 0 ?
                eventRepository.findAll().stream()
                        .mapToLong(Event::getCapacity)
                        .sum() : 0;

        double utilization = totalBookings > 0 && totalCapacity > 0 ?
                (double) totalBookings / totalCapacity * 100 : 0.0;

        AnalyticsResponse response = new AnalyticsResponse();
        response.setTotalBookings(totalBookings);
        response.setTotalCapacity(totalCapacity);
        response.setUtilizationPercentage(String.format("%.2f", utilization));
        return response;
    }

    // Admin Booking Management Methods (new)
    
    @Transactional(readOnly = true)
    public List<BookingResponse> getAllBookings(String status, String eventId) {
        List<Booking> bookings;
        
        if (status != null && eventId != null) {
            // Filter by both status and eventId
            UUID eventUuid = parseUUID(eventId, "Event ID");
            bookings = bookingRepository.findByStatusAndEventId(status, eventUuid);
        } else if (status != null) {
            // Filter by status only
            bookings = bookingRepository.findByStatus(status);
        } else if (eventId != null) {
            // Filter by eventId only
            UUID eventUuid = parseUUID(eventId, "Event ID");
            bookings = bookingRepository.findByEventId(eventUuid);
        } else {
            // Get all bookings
            bookings = bookingRepository.findAll();
        }
        
        return bookings.stream()
                .map(bookingMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BookingResponse getBookingById(String bookingId) {
        UUID bookingUuid = parseUUID(bookingId, "Booking ID");
        Booking booking = bookingRepository.findById(bookingUuid)
                .orElseThrow(() -> new EventException("Booking not found",
                        "BOOKING_NOT_FOUND",
                        "Booking with ID " + bookingId + " does not exist"));
        
        return bookingMapper.toResponse(booking);
    }

    // ============= USER MANAGEMENT METHODS (NEW) =============
    
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable, String role, Boolean isActive) {
        Page<User> users;
        
        if (role != null && isActive != null) {
            users = userRepository.findByRoleAndIsActive(User.UserRole.valueOf(role.toUpperCase()), isActive, pageable);
        } else if (role != null) {
            users = userRepository.findByRole(User.UserRole.valueOf(role.toUpperCase()), pageable);
        } else if (isActive != null) {
            users = userRepository.findByIsActive(isActive, pageable);
        } else {
            users = userRepository.findAll(pageable);
        }
        
        return users.map(userMapper::toResponse);
    }

    @Transactional
    public UserResponse createUser(UserRegistrationRequest request) {
        validateUserRegistrationRequest(request);
        
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EventException("Email already registered", 
                    "USER_EMAIL_EXISTS", 
                    "A user with email " + request.getEmail() + " already exists");
        }

        // Create user entity and hash password
        User user = userMapper.toEntity(request);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        
        User savedUser = userRepository.save(user);
        return userMapper.toResponse(savedUser);
    }

    @Transactional
    public UserResponse updateUser(String userId, UserUpdateRequest request) {
        UUID uuid = parseUUID(userId, "User ID");
        User existingUser = userRepository.findById(uuid)
                .orElseThrow(() -> new EventException("User not found",
                        "USER_NOT_FOUND",
                        "User with ID " + userId + " does not exist"));

        // Update fields if provided
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            existingUser.setName(request.getName().trim());
        }
        
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            // Check if new email is already taken by another user
            if (!existingUser.getEmail().equals(request.getEmail()) && 
                userRepository.existsByEmail(request.getEmail())) {
                throw new EventException("Email already registered", 
                        "USER_EMAIL_EXISTS", 
                        "A user with email " + request.getEmail() + " already exists");
            }
            existingUser.setEmail(request.getEmail().trim());
        }
        
        if (request.getRole() != null) {
            try {
                User.UserRole newRole = User.UserRole.valueOf(request.getRole().toUpperCase());
                existingUser.setRole(newRole);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid role: " + request.getRole());
            }
        }
        
        if (request.getIsActive() != null) {
            existingUser.setIsActive(request.getIsActive());
        }
        
        if (request.getNewPassword() != null && !request.getNewPassword().trim().isEmpty()) {
            if (request.getNewPassword().length() < 8) {
                throw new IllegalArgumentException("Password must be at least 8 characters long");
            }
            existingUser.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        }

        User savedUser = userRepository.save(existingUser);
        return userMapper.toResponse(savedUser);
    }

    @Transactional
    public void deactivateUser(String userId) {
        UUID uuid = parseUUID(userId, "User ID");
        User user = userRepository.findById(uuid)
                .orElseThrow(() -> new EventException("User not found",
                        "USER_NOT_FOUND",
                        "User with ID " + userId + " does not exist"));
        
        user.setIsActive(false);
        userRepository.save(user);
    }

    @Transactional
    public void activateUser(String userId) {
        UUID uuid = parseUUID(userId, "User ID");
        User user = userRepository.findById(uuid)
                .orElseThrow(() -> new EventException("User not found",
                        "USER_NOT_FOUND",
                        "User with ID " + userId + " does not exist"));
        
        user.setIsActive(true);
        userRepository.save(user);
    }

    @Transactional
    public void promoteUserToAdmin(String userId) {
        UUID uuid = parseUUID(userId, "User ID");
        User user = userRepository.findById(uuid)
                .orElseThrow(() -> new EventException("User not found",
                        "USER_NOT_FOUND",
                        "User with ID " + userId + " does not exist"));
        
        user.setRole(User.UserRole.ADMIN);
        userRepository.save(user);
    }

    @Transactional
    public void demoteUserToRegular(String userId) {
        UUID uuid = parseUUID(userId, "User ID");
        User user = userRepository.findById(uuid)
                .orElseThrow(() -> new EventException("User not found",
                        "USER_NOT_FOUND",
                        "User with ID " + userId + " does not exist"));
        
        user.setRole(User.UserRole.USER);
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(String userId) {
        UUID uuid = parseUUID(userId, "User ID");
        User user = userRepository.findById(uuid)
                .orElseThrow(() -> new EventException("User not found",
                        "USER_NOT_FOUND",
                        "User with ID " + userId + " does not exist"));

        // Check if user has active bookings
        List<Booking> activeBookings = bookingRepository.findByUserIdAndStatus(uuid, "CONFIRMED");
        if (!activeBookings.isEmpty()) {
            throw new EventException("Cannot delete user with active bookings", 
                    "USER_HAS_ACTIVE_BOOKINGS", 
                    "User has " + activeBookings.size() + " active bookings. Cancel them first.");
        }

        userRepository.delete(user);
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> searchUsers(String query, Pageable pageable) {
        Page<User> users = userRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                query, query, pageable);
        return users.map(userMapper::toResponse);
    }

    private void validateEventRequest(EventRequest request) {
        if (request.getEventName() == null || request.getEventName().trim().isEmpty()) {
            throw new IllegalArgumentException("Event name is required");
        }
        if (request.getVenue() == null || request.getVenue().trim().isEmpty()) {
            throw new IllegalArgumentException("Venue is required");
        }
        if (request.getStartTime() == null) {
            throw new IllegalArgumentException("Start time is required");
        }
        if (request.getCapacity() == null || request.getCapacity() <= 0) {
            throw new IllegalArgumentException("Capacity must be positive");
        }
    }

    private void validateUserRegistrationRequest(UserRegistrationRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }
        if (request.getPassword().length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }
    }

    private UUID parseUUID(String id, String fieldName) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid " + fieldName + " format: " + id);
        }
    }

    // Legacy method for backward compatibility (will be removed in Phase D)
    @Transactional
    @Deprecated
    public Event createEvent(String name, String venue, ZonedDateTime startsAt, Integer capacity) {
        EventRequest request = new EventRequest();
        request.setEventName(name);
        request.setVenue(venue);
        request.setStartTime(startsAt);
        request.setCapacity(capacity);

        EventResponse response = createEvent(request);
        UUID uuid = parseUUID(response.getEventId(), "Event ID");
        return eventRepository.findById(uuid).orElse(null);
    }
}