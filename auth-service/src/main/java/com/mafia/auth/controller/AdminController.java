package com.mafia.auth.controller;

import com.mafia.auth.dto.UpdateAdminFlagRequest;
import com.mafia.auth.dto.UserInfoResponse;
import com.mafia.auth.service.AdminService;
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
import java.util.UUID;

/**
 * Controller administracyjny do zarządzania użytkownikami.
 * Dostęp tylko dla administratorów.
 */
@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Administration", description = "Admin-only endpoints for user management")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final AdminService adminService;

    @GetMapping
    @Operation(
        summary = "Get all users",
        description = "Retrieves a list of all registered users in the system. Only accessible by administrators.",
        operationId = "getAllUsers"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved users list",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserInfoResponse.class, type = "array"),
                examples = @ExampleObject(
                    name = "Users list",
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
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    })
    public ResponseEntity<List<UserInfoResponse>> getAllUsers() {
        log.info("Admin request: Get all users");
        List<UserInfoResponse> users = adminService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{userId}/admin-flag")
    @Operation(
        summary = "Update user admin flag",
        description = "Updates whether a user is an admin of the service.",
        operationId = "updateUserAdminFlag"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "User admin flag updated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserInfoResponse.class)
            )
        ),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<UserInfoResponse> updateUserAdminFlag(
            @Parameter(description = "ID of the user to update", required = true)
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateAdminFlagRequest request) {
        
        log.info("Admin request: Update admin flag for user {} to {}", userId, request.isAdmin());
        UserInfoResponse updatedUser = adminService.updateUserAdminFlag(userId, request.isAdmin());
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{userId}")
    @Operation(
        summary = "Delete user",
        description = "Permanently deletes a user from the system. This action cannot be undone.",
        operationId = "deleteUser"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "User deleted successfully"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID of the user to delete", required = true)
            @PathVariable UUID userId) {
        
        log.info("Admin request: Delete user {}", userId);
        adminService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
