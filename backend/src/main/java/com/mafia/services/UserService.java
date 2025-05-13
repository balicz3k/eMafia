package com.mafia.services;

import com.mafia.components.JwtTokenProvider;
import com.mafia.dto.AuthResponse;
import com.mafia.dto.LoginRequest;
import com.mafia.dto.RegistrationRequest;
import com.mafia.dto.UserResponse;
import com.mafia.exceptions.EmailAlreadyExistsException;
import com.mafia.exceptions.InvalidPasswordException;
import com.mafia.exceptions.UserNotFoundException;
import com.mafia.exceptions.UsernameAlreadyExistsException;
import com.mafia.models.Role;
import com.mafia.models.User;
import com.mafia.repositiories.UserRepository;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service @RequiredArgsConstructor public class UserService
{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthResponse registerUser(RegistrationRequest request)
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

    public List<UserResponse> searchUsers(String query)
    {
        List<User> users = userRepository.findByUsernameContainingIgnoreCase(query);
        return users.stream()
            .map(user -> new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getRoles()))
            .collect(Collectors.toList());
    }
}