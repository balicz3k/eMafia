package com.mafia.controllers;

import com.mafia.dto.auth.UpdateUserAdminFlagReq;
import com.mafia.dto.auth.UserInfoResp;
import com.mafia.services.AdminService;
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
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Administration", description = "Admin-only endpoints for user management")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class AdminController {

  private final AdminService adminService;

  @GetMapping
  @Operation(
      summary = "Get all users",
      description =
          "Retrieves a list of all registered users in the system. Only accessible by administrators.",
      operationId = "getAllUsers")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved users list",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserInfoResp.class, type = "array"),
                    examples =
                        @ExampleObject(
                            name = "Users list",
                            summary = "List of all users",
                            value =
                                """
                    [
                        {
                            "id": "123e4567-e89b-12d3-a456-426614174000",
                            "username": "john_doe",
                            "email": "john@example.com",
                            "isAdmin": false
                        },
                        {
                            "id": "123e4567-e89b-12d3-a456-426214174000",
                            "username": "jane_admin",
                            "email": "jane@example.com",
                            "isAdmin": true
                        }
                    ]
                    """)))
      })
  public ResponseEntity<List<UserInfoResp>> getAllUsers() {
    List<UserInfoResp> users = adminService.getAllUsers();
    return ResponseEntity.ok(users);
  }

  @PutMapping("/{userId}/admin-flag")
  @Operation(
      summary = "Update user admin flag",
      description = "Updates whether a user is an admin of the service.",
      operationId = "updateUserAdminFlag")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "User admin flag updated successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserInfoResp.class),
                    examples =
                        @ExampleObject(
                            name = "Updated user",
                            summary = "User with updated admin flag",
                            value =
                                """
                    {
                        "id": "123e4567-e89b-12d3-a456-426614174000",
                        "username": "john_doe",
                        "email": "john@example.com",
                        "isAdmin": true
                    }
                    """))),
        @ApiResponse(responseCode = "404", description = "User not found")
      })
  public ResponseEntity<UserInfoResp> updateUserAdminFlag(
      @Parameter(description = "ID of the user to update", required = true) @PathVariable
          UUID userId,
      @Valid @RequestBody UpdateUserAdminFlagReq request) {
    UserInfoResp updatedUser = adminService.updateUserAdminFlag(userId, request.isAdmin());
    return ResponseEntity.ok(updatedUser);
  }

  @DeleteMapping("/{userId}")
  @Operation(
      summary = "Delete user",
      description =
          "Permanently deletes a user from the system. This action cannot be undone. Only administrators can delete users.",
      operationId = "deleteUser")
  public ResponseEntity<Void> deleteUser(
      @Parameter(description = "ID of the user to delete", required = true) @PathVariable
          UUID userId) {
    adminService.deleteUser(userId);
    return ResponseEntity.noContent().build();
  }
}
