package com.mafia.controllers;

import com.mafia.dto.AuthResponse;
import com.mafia.dto.LoginRequest;
import com.mafia.dto.RegistrationRequest;
import com.mafia.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController @RequestMapping("/api/auth") @RequiredArgsConstructor public class AuthController
{

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegistrationRequest request)
    {
        AuthResponse response = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login") public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request)
    {
        AuthResponse response = userService.authenticateUser(request);
        return ResponseEntity.ok(response);
    }
}