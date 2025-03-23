package com.mafia.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController public class BackendController
{

    @GetMapping("/api/test") public String testConnection()
    {
        return "Backend is working! 🚀 Current time: " + java.time.LocalDateTime.now();
    }
}