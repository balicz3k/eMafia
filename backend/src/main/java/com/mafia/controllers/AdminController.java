package com.mafia.controllers;

import com.mafia.dto.UpdateUserRolesRequest;
import com.mafia.dto.UserResponse;
import com.mafia.services.UserService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
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
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Administration", description = "Admin-only endpoints for user management")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieves a list of all registered users in the system. Only accessible by administrators.", operationId = "getAllUsers")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved users list", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class, type = "array"), examples = @ExampleObject(name = "Users list", summary = "List of all users", value = """
                    [
                        {
                            "id": "123e4567-e89b-12d3-a456-426614174000",
                            "username": "john_doe",
                            "email": "john@example.com",
                            "roles": ["ROLE_USER"],
                            "createdAt": "2024-01-15T10:30:00Z",
                            "lastLoginAt": "2024-01-20T14:20:00Z"
                        },
                        {
                            "id": "987fcdeb-51a2-43d1-9c47-123456789abc",
                            "username": "jane_admin",
                            "email": "jane@example.com",
                            "roles": ["ROLE_USER", "ROLE_ADMIN"],
                            "createdAt": "2024-01-10T08:15:00Z",
                            "lastLoginAt": "2024-01-21T09:45:00Z"
                        }
                    ]
                    """))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token required", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Unauthorized", value = """
                    {
                        "error": "Unauthorized",
                        "message": "JWT token is required",
                        "timestamp": "2024-01-15T10:30:00Z"
                    }
                    """))),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Forbidden", value = """
                    {
                        "error": "Access denied",
                        "message": "Admin role is required for this operation",
                        "timestamp": "2024-01-15T10:30:00Z"
                    }
                    """)))
    })
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.adminGetAllUsers();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{userId}/roles")
    @Operation(summary = "Update user roles", description = "Updates the roles assigned to a specific user. Only administrators can modify user roles.", operationId = "updateUserRoles")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User roles updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class), examples = @ExampleObject(name = "Updated user", summary = "User with updated roles", value = """
                    {
                        "id": "123e4567-e89b-12d3-a456-426614174000",
                        "username": "john_doe",
                        "email": "john@example.com",
                        "roles": ["ROLE_USER", "ROLE_ADMIN"],
                        "createdAt": "2024-01-15T10:30:00Z",
                        "lastLoginAt": "2024-01-20T14:20:00Z"
                    }
                    """))),
            @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Invalid roles", value = """
                    {
                        "error": "Validation failed",
                        "message": "Invalid role specified",
                        "timestamp": "2024-01-15T10:30:00Z"
                    }
                    """))),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "User not found", value = """
                    {
                        "error": "User not found",
                        "message": "No user found with ID: 123e4567-e89b-12d3-a456-426614174000",
                        "timestamp": "2024-01-15T10:30:00Z"
                    }
                    """))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
    })
    public ResponseEntity<UserResponse> updateUserRoles(
            @Parameter(description = "ID of the user to update", required = true, example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID userId,

            @Parameter(description = "New roles to assign to the user", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = UpdateUserRolesRequest.class), examples = @ExampleObject(name = "Update roles request", summary = "Assign admin role to user", value = """
                    {
                        "roles": ["ROLE_USER", "ROLE_ADMIN"]
                    }
                    """))) @Valid @RequestBody UpdateUserRolesRequest request) {
        UserResponse updatedUser = userService.adminUpdateUserRoles(userId, request.getRoles());
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Delete user", description = "Permanently deletes a user from the system. This action cannot be undone. Only administrators can delete users.", operationId = "deleteUser")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User deleted successfully (no content returned)"),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "User not found", value = """
                    {
                        "error": "User not found",
                        "message": "No user found with ID: 123e4567-e89b-12d3-a456-426614174000",
                        "timestamp": "2024-01-15T10:30:00Z"
                    }
                    """))),
            @ApiResponse(responseCode = "400", description = "Cannot delete user", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Cannot delete", value = """
                    {
                        "error": "Operation not allowed",
                        "message": "Cannot delete user who is currently hosting active game rooms",
                        "timestamp": "2024-01-15T10:30:00Z"
                    }
                    """))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
    })
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID of the user to delete", required = true, example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID userId) {
        userService.adminDeleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}