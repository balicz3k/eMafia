package com.mafia.controllers;

import com.mafia.dto.UserResponse;
import com.mafia.services.UserService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/users") public class UserController
{

    private final UserService userService;

    public UserController(UserService userService) { this.userService = userService; }

    @GetMapping("/search") public ResponseEntity<List<UserResponse>> searchUsers(@RequestParam String query)
    {
        System.out.println("Zapytanie wyszukiwania: " + query);
        List<UserResponse> users = userService.searchUsers(query);
        System.out.println("Znalezieni użytkownicy: " + users);
        return ResponseEntity.ok(users);
    }
}