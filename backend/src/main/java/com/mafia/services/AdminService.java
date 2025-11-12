package com.mafia.services;

import com.mafia.databaseModels.User;
import com.mafia.dto.auth.UserInfoResp;
import com.mafia.exceptions.UserNotFoundException;
import com.mafia.repositories.UserRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminService {
  UserRepository userRepository;

  public List<UserInfoResp> getAllUsers() {
    return userRepository.findAll().stream()
        .map(
            user ->
                new UserInfoResp(user.getId(), user.getUsername(), user.getEmail(), user.isAdmin()))
        .collect(Collectors.toList());
  }

  @Transactional
  public UserInfoResp updateUserAdminFlag(UUID userId, boolean isAdmin) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
    user.setAdmin(isAdmin);
    User saved = userRepository.save(user);
    return new UserInfoResp(saved.getId(), saved.getUsername(), saved.getEmail(), saved.isAdmin());
  }

  @Transactional
  public void deleteUser(UUID userId) {
    if (!userRepository.existsById(userId)) {
      throw new UserNotFoundException("User not found with ID: " + userId);
    }
    userRepository.deleteById(userId);
  }
}
