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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User authentication and registration endpoints")
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Creates a new user account with username, email and password. Email must be unique in the system.", operationId = "registerUser")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User successfully registered", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class), examples = @ExampleObject(name = "Successful registration", value = """
                    {
                        "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                        "type": "Bearer",
                        "user": {
                            "id": "123e4567-e89b-12d3-a456-426614174000",
                            "username": "john_doe",
                            "email": "john@example.com",
                            "roles": ["ROLE_USER"]
                        }
                    }
                    """))),
            @ApiResponse(responseCode = "400", description = "Invalid input data or validation errors"),
            @ApiResponse(responseCode = "422", description = "Email already exists in the system")
    })
    public ResponseEntity<AuthResponse> register(
            @Parameter(description = "User registration data including username, email and password", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = RegistrationRequest.class), examples = @ExampleObject(name = "Registration request", summary = "Example registration data", value = """
                    {
                        "username": "john_doe",
                        "email": "john@example.com",
                        "password": "SecurePassword123!"
                    }
                    """))) @Valid @RequestBody RegistrationRequest request) {
        AuthResponse response = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticates user with email and password, returns JWT token for subsequent API calls", operationId = "loginUser")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class), examples = @ExampleObject(name = "Successful login", summary = "User logged in successfully", value = """
                    {
                        "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                        "type": "Bearer",
                        "user": {
                            "id": "123e4567-e89b-12d3-a456-426614174000",
                            "username": "john_doe",
                            "email": "john@example.com",
                            "roles": ["ROLE_USER"]
                        }
                    }
                    """))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Invalid credentials", summary = "Wrong email or password", value = """
                    {
                        "error": "Authentication failed",
                        "message": "Invalid email or password",
                        "timestamp": "2024-01-15T10:30:00Z"
                    }
                    """))),
            @ApiResponse(responseCode = "400", description = "Invalid request format", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Invalid format", summary = "Malformed request", value = """
                    {
                        "error": "Bad request",
                        "message": "Email field is required",
                        "timestamp": "2024-01-15T10:30:00Z"
                    }
                    """)))
    })
    public ResponseEntity<AuthResponse> login(
            @Parameter(description = "User login credentials (email and password)", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoginRequest.class), examples = @ExampleObject(name = "Login request", summary = "Example login credentials", value = """
                    {
                        "email": "john@example.com",
                        "password": "SecurePassword123!"
                    }
                    """))) @Valid @RequestBody LoginRequest request) {
        AuthResponse response = userService.authenticateUser(request);
        return ResponseEntity.ok(response);
    }
}