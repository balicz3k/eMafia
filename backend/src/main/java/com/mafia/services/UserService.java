package com.mafia.services;

import com.mafia.components.JwtTokenProvider;
import com.mafia.dto.*;
import com.mafia.exceptions.*;
import com.mafia.models.Role;
import com.mafia.models.User;
import com.mafia.repositiories.UserRepository;
import java.util.Collections;
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

@Service @RequiredArgsConstructor public class UserService
{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional public AuthResponse registerUser(RegistrationRequest request)
    {
        if (userRepository.existsByEmail(request.getEmail()))
        {
            throw new EmailAlreadyExistsException("Email is already registered");
        }

        if (userRepository.existsByUsername(request.getUsername()))
        {
            throw new UsernameAlreadyExistsException("Username is already taken");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(Collections.singleton(Role.ROLE_USER));

        User savedUser = userRepository.save(user);
        String token = jwtTokenProvider.generateToken(savedUser);

        return new AuthResponse(token);
    }

    public AuthResponse authenticateUser(LoginRequest request)
    {
        User user = userRepository.findByEmail(request.getEmail())
                        .orElseThrow(() -> new UserNotFoundException("User does not exist"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword()))
        {
            throw new InvalidPasswordException("Invalid password");
        }

        String token = jwtTokenProvider.generateToken(user);
        return new AuthResponse(token);
    }

    private UUID getCurrentAuthenticatedUserId()
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() ||
            authentication.getPrincipal().equals("anonymousUser"))
        {
            throw new UserNotFoundException("User not authenticated");
        }

        User principalUser = (User)authentication.getPrincipal();
        return principalUser.getId();
    }

    public List<UserResponse> searchUsers(String query)
    {
        List<User> users = userRepository.findByUsernameContainingIgnoreCase(query);
        return users.stream()
            .map(user -> new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getRoles()))
            .collect(Collectors.toList());
    }

    @Transactional public UserResponse updateUsername(String newUsername)
    {
        UUID userId = getCurrentAuthenticatedUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found"));

        if (userRepository.existsByUsername(newUsername) && !user.getUsername().equals(newUsername))
        {
            throw new UsernameAlreadyExistsException("Username is already taken");
        }
        user.setUsername(newUsername);
        User updatedUser = userRepository.save(user);
        return new UserResponse(updatedUser.getId(), updatedUser.getUsername(), updatedUser.getEmail(),
                                updatedUser.getRoles());
    }

    @Transactional public UserResponse updateEmail(String newEmail)
    {
        UUID userId = getCurrentAuthenticatedUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found"));

        if (userRepository.existsByEmail(newEmail) && !user.getEmail().equals(newEmail))
        {
            throw new EmailAlreadyExistsException("Email is already registered");
        }
        user.setEmail(newEmail);
        User updatedUser = userRepository.save(user);
        return new UserResponse(updatedUser.getId(), updatedUser.getUsername(), updatedUser.getEmail(),
                                updatedUser.getRoles());
    }

    @Transactional public void updatePassword(String oldPassword, String newPassword)
    {
        UUID userId = getCurrentAuthenticatedUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword()))
        {
            throw new InvalidPasswordException("Invalid old password");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}