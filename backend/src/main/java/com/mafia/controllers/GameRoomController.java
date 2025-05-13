package com.mafia.controllers;

import com.mafia.dto.CreateGameRoomRequest;
import com.mafia.dto.GameRoomResponse;
import com.mafia.services.GameRoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gamerooms")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class GameRoomController
{

    private final GameRoomService gameRoomService;

    @PostMapping("/create")
    public ResponseEntity<GameRoomResponse> createGameRoom(@Valid @RequestBody CreateGameRoomRequest request)
    {
        GameRoomResponse gameRoomResponse = gameRoomService.createRoom(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(gameRoomResponse);
    }

    @PostMapping("/{roomCode}/join") public ResponseEntity<GameRoomResponse> joinGameRoom(@PathVariable String roomCode)
    {
        GameRoomResponse gameRoomResponse = gameRoomService.joinRoom(roomCode);
        return ResponseEntity.ok(gameRoomResponse);
    }

    @GetMapping("/{roomCode}") public ResponseEntity<GameRoomResponse> getGameRoomDetails(@PathVariable String roomCode)
    {
        GameRoomResponse gameRoomResponse = gameRoomService.getRoomDetails(roomCode);
        return ResponseEntity.ok(gameRoomResponse);
    }
}