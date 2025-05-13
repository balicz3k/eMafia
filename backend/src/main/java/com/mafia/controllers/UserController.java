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

@RestController @RequestMapping("/api/users") public class UserController
{

    private final UserService userService;

    public UserController(UserService userService) { this.userService = userService; }

    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<UserResponse>> searchUsers(@RequestParam String query)
    {
        List<UserResponse> users = userService.searchUsers(query);
        return ResponseEntity.ok(users);
    }

    @PutMapping("/profile/username")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> updateUsername(@Valid @RequestBody UpdateUsernameRequest request)
    {
        UserResponse updatedUser = userService.updateUsername(request.getNewUsername());
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/profile/email")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> updateEmail(@Valid @RequestBody UpdateEmailRequest request)
    {
        UserResponse updatedUser = userService.updateEmail(request.getNewEmail());
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/profile/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> updatePassword(@Valid @RequestBody UpdatePasswordRequest request)
    {
        userService.updatePassword(request.getOldPassword(), request.getNewPassword());
        return ResponseEntity.ok().build();
    }
}