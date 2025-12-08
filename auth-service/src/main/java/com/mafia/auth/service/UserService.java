package com.mafia.auth.service;

import com.mafia.auth.dto.AuthResponse;
import com.mafia.auth.dto.LoginRequest;
import com.mafia.auth.dto.RegistrationRequest;
import com.mafia.auth.dto.UserInfoResponse;
import com.mafia.auth.dto.UserResponse;
import com.mafia.auth.exception.*;
import com.mafia.auth.model.RefreshToken;
import com.mafia.auth.model.User;
import com.mafia.auth.repository.UserRepository;
import com.mafia.auth.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Serwis zarządzający użytkownikami i autentykacją.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final RabbitTemplate rabbitTemplate;

    // RabbitMQ exchange i routing keys do komunikacji z game-service
    private static final String USER_EVENTS_EXCHANGE = "user.events.exchange";
    private static final String USER_CREATED_ROUTING_KEY = "user.created";
    private static final String USER_UPDATED_ROUTING_KEY = "user.updated";

    @Transactional
    public AuthResponse registerUser(RegistrationRequest request) {
        log.info("Registering new user: {}", request.getUsername());
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email is already registered");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UsernameAlreadyExistsException("Username is already taken");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        User savedUser = userRepository.save(user);
        log.info("User registered: {} (ID: {})", savedUser.getUsername(), savedUser.getId());

        // Publikuj zdarzenie do RabbitMQ dla game-service
        publishUserCreatedEvent(savedUser);

        String accessToken = jwtTokenProvider.generateToken(savedUser);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(savedUser, "Registration");

        return new AuthResponse(
            accessToken, 
            refreshToken.getToken(), 
            "Bearer", 
            jwtTokenProvider.getExpirationTime()
        );
    }

    public AuthResponse authenticateUser(LoginRequest request) {
        log.info("Authenticating user: {}", request.getEmail());
        
        User user = userRepository
            .findByEmail(request.getEmail())
            .orElseThrow(() -> new UserNotFoundException("User does not exist"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidPasswordException("Invalid password");
        }

        String accessToken = jwtTokenProvider.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user, "Login");

        log.info("User authenticated: {}", user.getUsername());
        
        return new AuthResponse(
            accessToken, 
            refreshToken.getToken(), 
            "Bearer", 
            jwtTokenProvider.getExpirationTime()
        );
    }

    @Transactional
    public void logoutUser(String refreshTokenString) {
        if (refreshTokenString != null) {
            refreshTokenService.revokeRefreshToken(refreshTokenString);
            log.info("User logged out");
        }
    }

    @Transactional
    public void logoutAllDevices() {
        User user = getCurrentAuthenticatedUser();
        refreshTokenService.revokeAllUserTokens(user);
        log.info("User {} logged out from all devices", user.getUsername());
    }

    public UserResponse getCurrentUserProfile() {
        User user = getCurrentAuthenticatedUser();
        User fullUser = userRepository.findById(user.getId())
            .orElseThrow(() -> new UserNotFoundException("User not found"));
        return mapToUserResponse(fullUser);
    }

    public UserInfoResponse getCurrentUserInfo() {
        User user = getCurrentAuthenticatedUser();
        User fullUser = userRepository.findById(user.getId())
            .orElseThrow(() -> new UserNotFoundException("User not found"));
        return mapToUserInfoResponse(fullUser);
    }

    public User getUserById(UUID userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    public List<UserInfoResponse> searchUsers(String query) {
        return userRepository.findByUsernameContainingIgnoreCase(query)
            .stream()
            .map(this::mapToUserInfoResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public UserInfoResponse updateUsername(String newUsername) {
        User user = getCurrentAuthenticatedUser();
        User fullUser = userRepository.findById(user.getId())
            .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (userRepository.existsByUsername(newUsername) && !fullUser.getUsername().equals(newUsername)) {
            throw new UsernameAlreadyExistsException("Username is already taken");
        }
        
        fullUser.setUsername(newUsername);
        User updatedUser = userRepository.save(fullUser);
        
        // Publikuj zdarzenie aktualizacji
        publishUserUpdatedEvent(updatedUser);
        
        log.info("Username updated for user {}", updatedUser.getId());
        return mapToUserInfoResponse(updatedUser);
    }

    @Transactional
    public UserInfoResponse updateEmail(String newEmail) {
        User user = getCurrentAuthenticatedUser();
        User fullUser = userRepository.findById(user.getId())
            .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (userRepository.existsByEmail(newEmail) && !fullUser.getEmail().equals(newEmail)) {
            throw new EmailAlreadyExistsException("Email is already registered");
        }
        
        fullUser.setEmail(newEmail);
        User updatedUser = userRepository.save(fullUser);
        
        publishUserUpdatedEvent(updatedUser);
        
        log.info("Email updated for user {}", updatedUser.getId());
        return mapToUserInfoResponse(updatedUser);
    }

    @Transactional
    public void updatePassword(String oldPassword, String newPassword) {
        User user = getCurrentAuthenticatedUser();
        User fullUser = userRepository.findById(user.getId())
            .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!passwordEncoder.matches(oldPassword, fullUser.getPasswordHash())) {
            throw new InvalidPasswordException("Invalid old password");
        }
        
        fullUser.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(fullUser);
        
        log.info("Password updated for user {}", fullUser.getId());
    }

    private User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() 
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new UserNotFoundException("User not authenticated");
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User user)) {
            throw new UserNotFoundException("Authenticated principal is not a User");
        }
        return user;
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .isAdmin(user.isAdmin())
            .createdAt(user.getCreatedAt())
            .build();
    }

    private UserInfoResponse mapToUserInfoResponse(User user) {
        return UserInfoResponse.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .isAdmin(user.isAdmin())
            .build();
    }

    private void publishUserCreatedEvent(User user) {
        try {
            UserEventMessage event = new UserEventMessage(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.isAdmin(),
                "CREATED"
            );
            rabbitTemplate.convertAndSend(USER_EVENTS_EXCHANGE, USER_CREATED_ROUTING_KEY, event);
            log.info("[RabbitMQ] Published USER_CREATED event for user: {}", user.getUsername());
        } catch (Exception e) {
            log.error("[RabbitMQ] Failed to publish USER_CREATED event", e);
        }
    }

    private void publishUserUpdatedEvent(User user) {
        try {
            UserEventMessage event = new UserEventMessage(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.isAdmin(),
                "UPDATED"
            );
            rabbitTemplate.convertAndSend(USER_EVENTS_EXCHANGE, USER_UPDATED_ROUTING_KEY, event);
            log.info("[RabbitMQ] Published USER_UPDATED event for user: {}", user.getUsername());
        } catch (Exception e) {
            log.error("[RabbitMQ] Failed to publish USER_UPDATED event", e);
        }
    }

    /**
     * Wewnętrzna klasa reprezentująca zdarzenie użytkownika wysyłane przez RabbitMQ.
     */
    public record UserEventMessage(
        UUID userId,
        String username,
        String email,
        boolean isAdmin,
        String eventType
    ) {}
}
