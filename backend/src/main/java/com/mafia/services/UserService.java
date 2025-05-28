package com.mafia.services;

import com.mafia.components.JwtTokenProvider;
import com.mafia.dto.*;
import com.mafia.exceptions.*;
import com.mafia.models.Role;
import com.mafia.models.User;
import com.mafia.repositories.UserRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.mafia.models.RefreshToken;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public AuthResponse registerUser(RegistrationRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email is already registered");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UsernameAlreadyExistsException("Username is already taken");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        Set<Role> initialRoles = new HashSet<>();
        initialRoles.add(Role.ROLE_USER);
        user.setRoles(initialRoles);

        User savedUser = userRepository.save(user);

        String accessToken = jwtTokenProvider.generateToken(savedUser);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(savedUser, "Registration");

        return new AuthResponse(accessToken, refreshToken.getToken(), jwtTokenProvider.getExpirationTime());
    }

    public AuthResponse authenticateUser(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User does not exist"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidPasswordException("Invalid password");
        }

        String accessToken = jwtTokenProvider.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user, "Login");

        return new AuthResponse(accessToken, refreshToken.getToken(), jwtTokenProvider.getExpirationTime());
    }

    @Transactional
    public void logoutUser(String refreshTokenString) {
        if (refreshTokenString != null) {
            refreshTokenService.revokeRefreshToken(refreshTokenString);
        }
    }

    @Transactional
    public void logoutAllDevices() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new UserNotFoundException("User not authenticated for logoutAllDevices");
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User)) {
            throw new UserNotFoundException(
                    "Authenticated principal is not an instance of com.mafia.models.User. Found: "
                            + (principal != null ? principal.getClass().getName() : "null"));
        }
        User user = (User) principal;
        refreshTokenService.revokeAllUserTokens(user);
    }

    private UUID getCurrentAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() ||
                authentication.getPrincipal().equals("anonymousUser")) {
            throw new UserNotFoundException("User not authenticated");
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User)) {
            throw new UserNotFoundException(
                    "Authenticated principal is not an instance of com.mafia.models.User. Found: "
                            + (principal != null ? principal.getClass().getName() : "null"));
        }
        User principalUser = (User) principal;
        return principalUser.getId();
    }

    public List<UserResponse> searchUsers(String query) {
        List<User> users = userRepository.findByUsernameContainingIgnoreCase(query);
        return users.stream()
                .map(user -> new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getRoles()))
                .collect(Collectors.toList());
    }

    @Transactional
    public UserResponse updateUsername(String newUsername) {
        UUID userId = getCurrentAuthenticatedUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found"));

        if (userRepository.existsByUsername(newUsername) && !user.getUsername().equals(newUsername)) {
            throw new UsernameAlreadyExistsException("Username is already taken");
        }
        user.setUsername(newUsername);
        User updatedUser = userRepository.save(user);
        return new UserResponse(updatedUser.getId(), updatedUser.getUsername(), updatedUser.getEmail(),
                updatedUser.getRoles());
    }

    @Transactional
    public UserResponse updateEmail(String newEmail) {
        UUID userId = getCurrentAuthenticatedUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found"));

        if (userRepository.existsByEmail(newEmail) && !user.getEmail().equals(newEmail)) {
            throw new EmailAlreadyExistsException("Email is already registered");
        }
        user.setEmail(newEmail);
        User updatedUser = userRepository.save(user);
        return new UserResponse(updatedUser.getId(), updatedUser.getUsername(), updatedUser.getEmail(),
                updatedUser.getRoles());
    }

    @Transactional
    public void updatePassword(String oldPassword, String newPassword) {
        UUID userId = getCurrentAuthenticatedUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new InvalidPasswordException("Invalid old password");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public List<UserResponse> adminGetAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(user -> new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getRoles()))
                .collect(Collectors.toList());
    }

    @Transactional
    public UserResponse adminUpdateUserRoles(UUID userId, Set<Role> newRoles) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException("User not found with ID: " + userId));
        user.setRoles(new HashSet<>(newRoles));
        User updatedUser = userRepository.save(user);
        return new UserResponse(updatedUser.getId(), updatedUser.getUsername(), updatedUser.getEmail(),
                updatedUser.getRoles());
    }

    @Transactional
    public void adminDeleteUser(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found with ID: " + userId);
        }
        userRepository.deleteById(userId);
    }
}