package com.mafia.controllers;

import com.mafia.dto.UpdateEmailRequest;
import com.mafia.dto.UpdatePasswordRequest;
import com.mafia.dto.UpdateUsernameRequest;
import com.mafia.dto.UserResponse;
import com.mafia.services.UserService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "User profile management and search endpoints")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Search users", description = "Search for users by username or email. Returns a list of users matching the search query.", operationId = "searchUsers")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search results retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class, type = "array"), examples = @ExampleObject(name = "Search results", summary = "List of users matching search query", value = """
                    [
                        {
                            "id": "123e4567-e89b-12d3-a456-426614174000",
                            "username": "john_doe",
                            "email": "john@example.com",
                            "roles": ["ROLE_USER"]
                        },
                        {
                            "id": "456e7890-e89b-12d3-a456-426614174001",
                            "username": "jane_smith",
                            "email": "jane@example.com",
                            "roles": ["ROLE_USER"]
                        }
                    ]
                    """))),
            @ApiResponse(responseCode = "400", description = "Invalid search query", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Invalid query", value = """
                    {
                        "error": "Bad request",
                        "message": "Search query must be at least 2 characters long",
                        "timestamp": "2024-01-15T10:30:00Z"
                    }
                    """))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token required")
    })
    public ResponseEntity<List<UserResponse>> searchUsers(
            @Parameter(description = "Search query (username or email)", required = true, example = "john") @RequestParam String query) {
        List<UserResponse> users = userService.searchUsers(query);
        return ResponseEntity.ok(users);
    }

    @PutMapping("/profile/username")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update username", description = "Update the current user's username. Username must be unique across the system.", operationId = "updateUsername")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Username updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class), examples = @ExampleObject(name = "Updated user", summary = "User with new username", value = """
                    {
                        "id": "123e4567-e89b-12d3-a456-426614174000",
                        "username": "new_username",
                        "email": "john@example.com",
                        "roles": ["ROLE_USER"]
                    }
                    """))),
            @ApiResponse(responseCode = "400", description = "Invalid username or validation error", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Validation error", value = """
                    {
                        "error": "Validation failed",
                        "message": "Username must be between 3 and 20 characters",
                        "timestamp": "2024-01-15T10:30:00Z"
                    }
                    """))),
            @ApiResponse(responseCode = "409", description = "Username already exists", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Username taken", value = """
                    {
                        "error": "Conflict",
                        "message": "Username 'new_username' is already taken",
                        "timestamp": "2024-01-15T10:30:00Z"
                    }
                    """))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token required")
    })
    public ResponseEntity<UserResponse> updateUsername(
            @Parameter(description = "New username data", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = UpdateUsernameRequest.class), examples = @ExampleObject(name = "Update username request", summary = "Example username update", value = """
                    {
                        "newUsername": "new_cool_username"
                    }
                    """))) @Valid @RequestBody UpdateUsernameRequest request) {
        UserResponse updatedUser = userService.updateUsername(request.getNewUsername());
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/profile/email")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update email address", description = "Update the current user's email address. Email must be unique and valid.", operationId = "updateEmail")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class), examples = @ExampleObject(name = "Updated user", summary = "User with new email", value = """
                    {
                        "id": "123e4567-e89b-12d3-a456-426614174000",
                        "username": "john_doe",
                        "email": "newemail@example.com",
                        "roles": ["ROLE_USER"]
                    }
                    """))),
            @ApiResponse(responseCode = "400", description = "Invalid email format", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Invalid email", value = """
                    {
                        "error": "Validation failed",
                        "message": "Email format is invalid",
                        "timestamp": "2024-01-15T10:30:00Z"
                    }
                    """))),
            @ApiResponse(responseCode = "409", description = "Email already exists", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Email taken", value = """
                    {
                        "error": "Conflict",
                        "message": "Email 'newemail@example.com' is already registered",
                        "timestamp": "2024-01-15T10:30:00Z"
                    }
                    """))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token required")
    })
    public ResponseEntity<UserResponse> updateEmail(
            @Parameter(description = "New email data", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = UpdateEmailRequest.class), examples = @ExampleObject(name = "Update email request", summary = "Example email update", value = """
                    {
                        "newEmail": "newemail@example.com"
                    }
                    """))) @Valid @RequestBody UpdateEmailRequest request) {
        UserResponse updatedUser = userService.updateEmail(request.getNewEmail());
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/profile/password")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update password", description = "Update the current user's password. Requires current password for verification.", operationId = "updatePassword")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid password format or validation error", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Weak password", value = """
                    {
                        "error": "Validation failed",
                        "message": "New password must be at least 8 characters long",
                        "timestamp": "2024-01-15T10:30:00Z"
                    }
                    """))),
            @ApiResponse(responseCode = "401", description = "Current password is incorrect", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Wrong password", value = """
                    {
                        "error": "Authentication failed",
                        "message": "Current password is incorrect",
                        "timestamp": "2024-01-15T10:30:00Z"
                    }
                    """)))
    })
    public ResponseEntity<Void> updatePassword(
            @Parameter(description = "Password update data", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = UpdatePasswordRequest.class), examples = @ExampleObject(name = "Update password request", summary = "Example password update", value = """
                    {
                        "oldPassword": "OldSecurePassword123!",
                        "newPassword": "NewSecurePassword456!"
                    }
                    """))) @Valid @RequestBody UpdatePasswordRequest request) {
        userService.updatePassword(request.getOldPassword(), request.getNewPassword());
        return ResponseEntity.ok().build();
    }
}