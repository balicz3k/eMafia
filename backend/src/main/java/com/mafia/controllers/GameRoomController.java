package com.mafia.controllers;

import com.mafia.dto.CreateGameRoomRequest;
import com.mafia.exceptions.ResourceNotFoundException;
import com.mafia.models.GameRoom;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/rooms") public class GameRoomController
{

    private List<GameRoom> rooms = new ArrayList<>();

    @GetMapping public List<GameRoom> getAllRooms() { return rooms; }

    @GetMapping("/{roomId}") public ResponseEntity<GameRoom> getRoomById(@PathVariable UUID roomId)
    {
        return rooms.stream()
            .filter(room -> room.getId().equals(roomId))
            .findFirst()
            .map(ResponseEntity::ok)
            .orElseThrow(() -> new ResourceNotFoundException("Room with ID: " + roomId + " does not exist!"));
    }

    @PostMapping public GameRoom createGameRoom(@RequestBody CreateGameRoomRequest request)
    {
        GameRoom newRoom = new GameRoom(request.getName(), request.getHost(), request.getMaxPlayers());
        rooms.add(newRoom);
        return newRoom;
    }
}