package com.mafia.auth.controller;

import com.mafia.auth.dto.AuthResponse;
import com.mafia.auth.dto.LoginRequest;
import com.mafia.auth.dto.RefreshTokenRequest;
import com.mafia.auth.dto.RegistrationRequest;
import com.mafia.auth.exception.TokenExpiredException;
import com.mafia.auth.exception.TokenNotFoundException;
import com.mafia.auth.service.RefreshTokenService;
import com.mafia.auth.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Controller obsługujący endpointy uwierzytelniania.
 * Odpowiada za rejestrację, logowanie, odświeżanie tokenów i wylogowanie.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "User authentication and registration endpoints")
public class AuthController {

    private final UserService userService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/register")
    @Operation(
        summary = "Register new user",
        description = "Creates a new user account with username, email and password. Email must be unique in the system.",
        operationId = "registerUser"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "User successfully registered",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponse.class),
                examples = @ExampleObject(
                    name = "Successful registration",
                    value = """
                        {
                            "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                            "refreshToken": "refresh_token_here",
                            "tokenType": "Bearer",
                            "expiresIn": 3600,
                            "user": {
                                "id": "123e4567-e89b-12d3-a456-426614174000",
                                "username": "john_doe",
                                "email": "john@example.com",
                                "roles": ["ROLE_USER"]
                            }
                        }
                        """
                )
            )
        ),
        @ApiResponse(responseCode = "400", description = "Invalid input data or validation errors"),
        @ApiResponse(responseCode = "409", description = "Email or username already exists")
    })
    public ResponseEntity<AuthResponse> register(
            @Parameter(
                description = "User registration data including username, email and password",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = RegistrationRequest.class),
                    examples = @ExampleObject(
                        name = "Registration request",
                        value = """
                            {
                                "username": "john_doe",
                                "email": "john@example.com",
                                "password": "SecurePassword123!"
                            }
                            """
                    )
                )
            )
            @Valid @RequestBody RegistrationRequest request) {
        
        log.info("Registration attempt for email: {}", request.getEmail());
        AuthResponse response = userService.registerUser(request);
        log.info("User registered successfully: {}", request.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(
        summary = "User login",
        description = "Authenticates user with email and password, returns JWT token for subsequent API calls",
        operationId = "loginUser"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Login successful",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponse.class),
                examples = @ExampleObject(
                    name = "Successful login",
                    value = """
                        {
                            "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                            "refreshToken": "refresh_token_here",
                            "tokenType": "Bearer",
                            "expiresIn": 3600,
                            "user": {
                                "id": "123e4567-e89b-12d3-a456-426614174000",
                                "username": "john_doe",
                                "email": "john@example.com",
                                "roles": ["ROLE_USER"]
                            }
                        }
                        """
                )
            )
        ),
        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
        @ApiResponse(responseCode = "400", description = "Invalid request format")
    })
    public ResponseEntity<AuthResponse> login(
            @Parameter(
                description = "User login credentials (email and password)",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = LoginRequest.class),
                    examples = @ExampleObject(
                        name = "Login request",
                        value = """
                            {
                                "email": "john@example.com",
                                "password": "SecurePassword123!"
                            }
                            """
                    )
                )
            )
            @Valid @RequestBody LoginRequest request) {
        
        log.info("Login attempt for email: {}", request.getEmail());
        AuthResponse response = userService.authenticateUser(request);
        log.info("User logged in successfully: {}", request.getEmail());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    @Operation(
        summary = "Refresh access token",
        description = "Generate new access token using refresh token"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "New access token generated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponse.class),
                examples = @ExampleObject(
                    name = "Successful refresh",
                    value = """
                        {
                            "token": "eyJhbGciOiJIUzI1NiJ9...",
                            "refreshToken": "new_refresh_token_here",
                            "tokenType": "Bearer",
                            "expiresIn": 3600
                        }
                        """
                )
            )
        ),
        @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
    })
    public ResponseEntity<AuthResponse> refreshToken(
            @Parameter(description = "Refresh token request", required = true)
            @RequestBody RefreshTokenRequest request) {
        
        log.debug("Token refresh attempt");
        try {
            AuthResponse response = refreshTokenService.refreshAccessToken(request.getRefreshToken());
            log.debug("Token refreshed successfully");
            return ResponseEntity.ok(response);
        } catch (TokenExpiredException | TokenNotFoundException e) {
            log.warn("Token refresh failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Logout user",
        description = "Revoke refresh token and logout current session"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Successfully logged out"),
        @ApiResponse(responseCode = "400", description = "Bad Request - Refresh token missing"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Valid access token required")
    })
    public ResponseEntity<Void> logout(
            @Parameter(description = "Refresh token to revoke")
            @RequestBody RefreshTokenRequest request) {
        
        if (request == null || request.getRefreshToken() == null || request.getRefreshToken().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        
        log.info("Logout attempt");
        userService.logoutUser(request.getRefreshToken());
        log.info("User logged out successfully");
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout-all")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Logout from all devices",
        description = "Revoke all refresh tokens for current user"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully logged out from all devices"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Map<String, String>> logoutAllDevices() {
        log.info("Logout all devices attempt");
        userService.logoutAllDevices();
        
        Map<String, String> response = Map.of(
            "message", "Successfully logged out from all devices",
            "timestamp", LocalDateTime.now().toString()
        );
        log.info("User logged out from all devices");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/validate")
    @Operation(
        summary = "Validate token",
        description = "Validates the JWT token and returns user info if valid"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token is valid"),
        @ApiResponse(responseCode = "401", description = "Token is invalid or expired")
    })
    public ResponseEntity<Map<String, Object>> validateToken() {
        // Ten endpoint jest chroniony przez JWT filter, więc jeśli dotarliśmy tutaj,
        // to token jest poprawny
        return ResponseEntity.ok(Map.of(
            "valid", true,
            "timestamp", LocalDateTime.now().toString()
        ));
    }
}
