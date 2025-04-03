package com.mafia.controllers;

import com.mafia.dto.CreateGameRoomRequest;
import com.mafia.dto.UserResponse;
import com.mafia.exceptions.ResourceNotFoundException;
import com.mafia.models.GameRoom;
import com.mafia.services.UserService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/rooms") public class UserController
{

    private final UserService userService;

    public UserController(UserService userService) { this.userService = userService; }

    @GetMapping("/search") public ResponseEntity<List<UserResponse>> searchUsers(@RequestParam String query)
    {
        List<UserResponse> users = userService.searchUsers(query);
        return ResponseEntity.ok(users);
    }
}