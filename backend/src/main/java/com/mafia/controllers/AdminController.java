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

@RestController @RequestMapping("/api/admin/users") @PreAuthorize("hasRole('ADMIN')") public class AdminController
{

    private final UserService userService;

    public AdminController(UserService userService) { this.userService = userService; }

    @GetMapping public ResponseEntity<List<UserResponse>> getAllUsers()
    {
        List<UserResponse> users = userService.adminGetAllUsers();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{userId}/roles")
    public ResponseEntity<UserResponse> updateUserRoles(@PathVariable UUID userId,
                                                        @Valid @RequestBody UpdateUserRolesRequest request)
    {
        UserResponse updatedUser = userService.adminUpdateUserRoles(userId, request.getRoles());
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{userId}") public ResponseEntity<Void> deleteUser(@PathVariable UUID userId)
    {
        userService.adminDeleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}