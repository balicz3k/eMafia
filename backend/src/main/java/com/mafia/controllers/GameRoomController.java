package com.mafia.controller;

import com.mafia.model.GameRoom;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/rooms") public class GameRoomController
{

    private List<GameRoom> rooms = new ArrayList<>();

    @GetMapping public List<GameRoom> getAllRooms() { return rooms; }

    @PostMapping public GameRoom createRoom(@RequestBody GameRoom newRoom)
    {
        newRoom.setId(UUID.randomUUID());
        rooms.add(newRoom);
        return newRoom;
    }
}