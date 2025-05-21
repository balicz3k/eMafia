package com.mafia.controllers;

import com.mafia.dto.CreateGameRoomRequest;
import com.mafia.dto.GameRoomResponse;
import com.mafia.services.GameRoomService;
import java.util.List;
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

    @GetMapping("/my-rooms") // Zmieniono endpoint dla jasności
    public ResponseEntity<List<GameRoomResponse>> getMyGameRooms() {
        List<GameRoomResponse> gameRooms = gameRoomService.getGameRoomsForCurrentUser();
        return ResponseEntity.ok(gameRooms);
    }

    // Nowy endpoint do wyszukiwania pokoi po nazwie
    @GetMapping("/search")
    public ResponseEntity<List<GameRoomResponse>> searchGameRooms(@RequestParam String name) {
        List<GameRoomResponse> gameRooms = gameRoomService.searchGameRoomsByName(name);
        return ResponseEntity.ok(gameRooms);
    }

    @PostMapping("/{roomCode}/leave")
    public ResponseEntity<Void> leaveGameRoom(@PathVariable String roomCode) {
        gameRoomService.leaveRoom(roomCode);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{roomCode}/end")
    public ResponseEntity<Void> endGameRoom(@PathVariable String roomCode) {
        gameRoomService.endRoom(roomCode);
        return ResponseEntity.ok().build();
    }
}