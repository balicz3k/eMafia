package com.mafia.services;

import com.mafia.components.JwtTokenProvider;
import com.mafia.databaseModels.RefreshToken;
import com.mafia.databaseModels.User;
import com.mafia.dto.auth.AuthResp;
import com.mafia.dto.auth.LoginReq;
import com.mafia.dto.auth.RegistrationReq;
import com.mafia.dto.auth.UserInfoResp;
import com.mafia.exceptions.*;
import com.mafia.repositories.UserRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider jwtTokenProvider;
  private final RefreshTokenService refreshTokenService;

  @Transactional
  public AuthResp registerUser(RegistrationReq request) {
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

    String accessToken = jwtTokenProvider.generateToken(savedUser);
    RefreshToken refreshToken = refreshTokenService.createRefreshToken(savedUser, "Registration");

    return new AuthResp(
        accessToken, refreshToken.getToken(), "Bearer", jwtTokenProvider.getExpirationTime());
  }

  public AuthResp authenticateUser(LoginReq request) {
    User user =
        userRepository
            .findByEmail(request.getEmail())
            .orElseThrow(() -> new UserNotFoundException("User does not exist"));

    if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
      throw new InvalidPasswordException("Invalid password");
    }

    String accessToken = jwtTokenProvider.generateToken(user);
    RefreshToken refreshToken = refreshTokenService.createRefreshToken(user, "Login");

    return new AuthResp(
        accessToken, refreshToken.getToken(), "Bearer", jwtTokenProvider.getExpirationTime());
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
    if (authentication == null
        || !authentication.isAuthenticated()
        || "anonymousUser".equals(authentication.getPrincipal())) {
      throw new UserNotFoundException("User not authenticated for logoutAllDevices");
    }
    Object principal = authentication.getPrincipal();
    if (!(principal instanceof User user)) {
      throw new UserNotFoundException(
          "Authenticated principal is not an instance of com.mafia.databaseModels.User. Found: "
              + (principal != null ? principal.getClass().getName() : "null"));
    }
    refreshTokenService.revokeAllUserTokens(user);
  }

  private UUID getCurrentAuthenticatedUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null
        || !authentication.isAuthenticated()
        || authentication.getPrincipal().equals("anonymousUser")) {
      throw new UserNotFoundException("User not authenticated");
    }
    Object principal = authentication.getPrincipal();
    if (!(principal instanceof User principalUser)) {
      throw new UserNotFoundException(
          "Authenticated principal is not an instance of com.mafia.databaseModels.User. Found: "
              + (principal != null ? principal.getClass().getName() : "null"));
    }
    return principalUser.getId();
  }

  public List<UserInfoResp> searchUsers(String query) {
    List<User> users = userRepository.findByUsernameContainingIgnoreCase(query);
    return users.stream()
        .map(
            user ->
                new UserInfoResp(user.getId(), user.getUsername(), user.getEmail(), user.isAdmin()))
        .collect(Collectors.toList());
  }

  @Transactional
  public UserInfoResp updateUsername(String newUsername) {
    UUID userId = getCurrentAuthenticatedUserId();
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found"));

    if (userRepository.existsByUsername(newUsername) && !user.getUsername().equals(newUsername)) {
      throw new UsernameAlreadyExistsException("Username is already taken");
    }
    user.setUsername(newUsername);
    User updatedUser = userRepository.save(user);
    return new UserInfoResp(
        updatedUser.getId(),
        updatedUser.getUsername(),
        updatedUser.getEmail(),
        updatedUser.isAdmin());
  }

  @Transactional
  public UserInfoResp updateEmail(String newEmail) {
    UUID userId = getCurrentAuthenticatedUserId();
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found"));

    if (userRepository.existsByEmail(newEmail) && !user.getEmail().equals(newEmail)) {
      throw new EmailAlreadyExistsException("Email is already registered");
    }
    user.setEmail(newEmail);
    User updatedUser = userRepository.save(user);
    return new UserInfoResp(
        updatedUser.getId(),
        updatedUser.getUsername(),
        updatedUser.getEmail(),
        updatedUser.isAdmin());
  }

  @Transactional
  public void updatePassword(String oldPassword, String newPassword) {
    UUID userId = getCurrentAuthenticatedUserId();
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found"));

    if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
      throw new InvalidPasswordException("Invalid old password");
    }
    user.setPasswordHash(passwordEncoder.encode(newPassword));
    userRepository.save(user);
  }
}
