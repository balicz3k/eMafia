package com.mafia.auth.service;

import com.mafia.auth.dto.UserInfoResponse;
import com.mafia.auth.exception.UserNotFoundException;
import com.mafia.auth.model.User;
import com.mafia.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Serwis administracyjny do zarządzania użytkownikami.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final UserRepository userRepository;

    /**
     * Pobiera listę wszystkich użytkowników.
     */
    public List<UserInfoResponse> getAllUsers() {
        log.info("Fetching all users");
        return userRepository.findAll().stream()
            .map(this::mapToUserInfoResponse)
            .collect(Collectors.toList());
    }

    /**
     * Aktualizuje flagę admin użytkownika.
     */
    @Transactional
    public UserInfoResponse updateUserAdminFlag(UUID userId, boolean isAdmin) {
        log.info("Updating admin flag for user {} to {}", userId, isAdmin);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
        
        user.setAdmin(isAdmin);
        User saved = userRepository.save(user);
        
        log.info("Admin flag updated for user {}", userId);
        return mapToUserInfoResponse(saved);
    }

    /**
     * Usuwa użytkownika z systemu.
     */
    @Transactional
    public void deleteUser(UUID userId) {
        log.info("Deleting user {}", userId);
        
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found with ID: " + userId);
        }
        
        userRepository.deleteById(userId);
        log.info("User {} deleted", userId);
    }

    private UserInfoResponse mapToUserInfoResponse(User user) {
        return UserInfoResponse.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .isAdmin(user.isAdmin())
            .build();
    }
}
