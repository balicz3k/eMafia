package com.mafia.auth.controller;

import com.mafia.auth.dto.*;
import com.mafia.auth.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller obsługujący endpointy zarządzania użytkownikami.
 * Odpowiada za wyszukiwanie i aktualizację danych użytkowników.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Management", description = "User profile management and search endpoints")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Search users",
        description = "Search for users by username or email",
        operationId = "searchUsers"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Search results retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserInfoResponse.class),
                examples = @ExampleObject(
                    name = "Search results",
                    value = """
                        [
                            {
                                "id": "123e4567-e89b-12d3-a456-426614174000",
                                "username": "john_doe",
                                "email": "john@example.com",
                                "isAdmin": false
                            }
                        ]
                        """
                )
            )
        ),
        @ApiResponse(responseCode = "400", description = "Invalid search query"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token required")
    })
    public ResponseEntity<List<UserInfoResponse>> searchUsers(
            @Parameter(description = "Search query", required = true)
            @RequestParam String query) {
        
        log.debug("Searching users with query: {}", query);
        List<UserInfoResponse> users = userService.searchUsers(query);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get current user info",
        description = "Get information about the currently authenticated user",
        operationId = "getCurrentUser"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "User info retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserInfoResponse.class)
            )
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token required")
    })
    public ResponseEntity<UserInfoResponse> getCurrentUser() {
        log.debug("Getting current user info");
        UserInfoResponse user = userService.getCurrentUserInfo();
        return ResponseEntity.ok(user);
    }

    @PutMapping("/profile/username")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Update username",
        description = "Update the current user's username",
        operationId = "updateUsername"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Username updated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserInfoResponse.class)
            )
        ),
        @ApiResponse(responseCode = "400", description = "Invalid username"),
        @ApiResponse(responseCode = "409", description = "Username already exists"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<UserInfoResponse> updateUsername(
            @Parameter(description = "New username data", required = true)
            @Valid @RequestBody UpdateUsernameRequest request) {
        
        log.info("Updating username for current user");
        UserInfoResponse updatedUser = userService.updateUsername(request.getNewUsername());
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/profile/email")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Update email address",
        description = "Update the current user's email address",
        operationId = "updateEmail"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Email updated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserInfoResponse.class)
            )
        ),
        @ApiResponse(responseCode = "400", description = "Invalid email format"),
        @ApiResponse(responseCode = "409", description = "Email already exists"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<UserInfoResponse> updateEmail(
            @Parameter(description = "New email data", required = true)
            @Valid @RequestBody UpdateEmailRequest request) {
        
        log.info("Updating email for current user");
        UserInfoResponse updatedUser = userService.updateEmail(request.getNewEmail());
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/profile/password")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Update password",
        description = "Update the current user's password",
        operationId = "updatePassword"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Password updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid password format"),
        @ApiResponse(responseCode = "401", description = "Current password is incorrect")
    })
    public ResponseEntity<Void> updatePassword(
            @Parameter(description = "Password update data", required = true)
            @Valid @RequestBody UpdatePasswordRequest request) {
        
        log.info("Updating password for current user");
        userService.updatePassword(request.getOldPassword(), request.getNewPassword());
        return ResponseEntity.ok().build();
    }
}
