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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/gamerooms")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(name = "Game Rooms", description = "Game room management endpoints for creating, joining and managing Mafia game rooms")
@SecurityRequirement(name = "bearerAuth")
public class GameRoomController {

    private final GameRoomService gameRoomService;

    @PostMapping("/create")
    @Operation(summary = "Create new game room", description = "Creates a new game room with unique code. The authenticated user becomes the host of the room.", operationId = "createGameRoom")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Game room created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = GameRoomResponse.class), examples = @ExampleObject(name = "Created room", summary = "Successfully created game room", value = """
                    {
                        "id": "123e4567-e89b-12d3-a456-426614174000",
                        "roomCode": "ABC123",
                        "name": "John's Mafia Game",
                        "hostId": "456e7890-e89b-12d3-a456-426614174001",
                        "hostUsername": "john_doe",
                        "maxPlayers": 8,
                        "currentPlayers": 1,
                        "status": "WAITING",
                        "createdAt": "2024-01-15T10:30:00",
                        "joinLinkPath": "/join/ABC123",
                        "players": [
                            {
                                "playerId": "456e7890-e89b-12d3-a456-426614174001",
                                "username": "john_doe",
                                "isHost": true,
                                "isReady": false
                            }
                        ]
                    }
                    """))),
            @ApiResponse(responseCode = "400", description = "Invalid room data", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Invalid data", value = """
                    {
                        "error": "Validation failed",
                        "message": "Room name cannot be empty",
                        "timestamp": "2024-01-15T10:30:00Z"
                    }
                    """))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token required")
    })
    public ResponseEntity<GameRoomResponse> createGameRoom(
            @Parameter(description = "Game room creation data", required = true, content = @Content(schema = @Schema(implementation = CreateGameRoomRequest.class), examples = @ExampleObject(name = "Room creation request", summary = "Example room creation data", value = """
                    {
                        "name": "Friday Night Mafia",
                        "maxPlayers": 10,
                        "isPublic": true
                    }
                    """))) @Valid @RequestBody CreateGameRoomRequest request) {
        GameRoomResponse gameRoomResponse = gameRoomService.createRoom(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(gameRoomResponse);
    }

    @PostMapping("/{roomCode}/join")
    @Operation(summary = "Join game room", description = "Join an existing game room using its unique code", operationId = "joinGameRoom")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully joined room", content = @Content(mediaType = "application/json", schema = @Schema(implementation = GameRoomResponse.class), examples = @ExampleObject(name = "Joined room", value = """
                    {
                        "id": "123e4567-e89b-12d3-a456-426614174000",
                        "roomCode": "ABC123",
                        "name": "Friday Night Mafia",
                        "hostId": "456e7890-e89b-12d3-a456-426614174001",
                        "hostUsername": "john_doe",
                        "maxPlayers": 10,
                        "currentPlayers": 3,
                        "status": "WAITING",
                        "createdAt": "2024-01-15T10:30:00",
                        "joinLinkPath": "/join/ABC123",
                        "players": [
                            {
                                "playerId": "456e7890-e89b-12d3-a456-426614174001",
                                "username": "john_doe",
                                "isHost": true,
                                "isReady": true
                            },
                            {
                                "playerId": "789e0123-e89b-12d3-a456-426614174002",
                                "username": "jane_doe",
                                "isHost": false,
                                "isReady": false
                            },
                            {
                                "playerId": "012e3456-e89b-12d3-a456-426614174003",
                                "username": "current_user",
                                "isHost": false,
                                "isReady": false
                            }
                        ]
                    }
                    """))),
            @ApiResponse(responseCode = "404", description = "Room not found", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Room not found", value = """
                    {
                        "error": "Room not found",
                        "message": "No room found with code: INVALID",
                        "timestamp": "2024-01-15T10:30:00Z"
                    }
                    """))),
            @ApiResponse(responseCode = "400", description = "Cannot join room (full, already joined, game started, etc.)", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Room full", value = """
                    {
                        "error": "Cannot join room",
                        "message": "Room is full (10/10 players)",
                        "timestamp": "2024-01-15T10:30:00Z"
                    }
                    """))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token required")
    })
    public ResponseEntity<GameRoomResponse> joinGameRoom(
            @Parameter(description = "Unique room code", required = true, example = "ABC123") @PathVariable String roomCode) {
        GameRoomResponse gameRoomResponse = gameRoomService.joinRoom(roomCode);
        return ResponseEntity.ok(gameRoomResponse);
    }

    @GetMapping("/{roomCode}")
    @Operation(summary = "Get game room details", description = "Retrieve detailed information about a specific game room", operationId = "getGameRoomDetails")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Room details retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = GameRoomResponse.class), examples = @ExampleObject(name = "Room details", value = """
                    {
                        "id": "123e4567-e89b-12d3-a456-426614174000",
                        "roomCode": "ABC123",
                        "name": "Friday Night Mafia",
                        "hostId": "456e7890-e89b-12d3-a456-426614174001",
                        "hostUsername": "john_doe",
                        "maxPlayers": 10,
                        "currentPlayers": 5,
                        "status": "WAITING",
                        "createdAt": "2024-01-15T10:30:00",
                        "joinLinkPath": "/join/ABC123",
                        "players": [
                            {
                                "playerId": "456e7890-e89b-12d3-a456-426614174001",
                                "username": "john_doe",
                                "isHost": true,
                                "isReady": true
                            },
                            {
                                "playerId": "789e0123-e89b-12d3-a456-426614174002",
                                "username": "jane_doe",
                                "isHost": false,
                                "isReady": false
                            }
                        ]
                    }
                    """))),
            @ApiResponse(responseCode = "404", description = "Room not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token required")
    })
    public ResponseEntity<GameRoomResponse> getGameRoomDetails(
            @Parameter(description = "Unique room code", required = true, example = "ABC123") @PathVariable String roomCode) {
        GameRoomResponse gameRoomResponse = gameRoomService.getRoomDetails(roomCode);
        return ResponseEntity.ok(gameRoomResponse);
    }

    @GetMapping("/my-rooms")
    @Operation(summary = "Get my game rooms", description = "Retrieve all game rooms where the authenticated user is a member or host", operationId = "getMyGameRooms")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User's rooms retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = GameRoomResponse.class, type = "array"), examples = @ExampleObject(name = "My rooms", value = """
                    [
                        {
                            "id": "123e4567-e89b-12d3-a456-426614174000",
                            "roomCode": "ABC123",
                            "name": "My Hosted Game",
                            "hostId": "current-user-id",
                            "hostUsername": "current_user",
                            "maxPlayers": 8,
                            "currentPlayers": 4,
                            "status": "IN_PROGRESS",
                            "createdAt": "2024-01-15T10:30:00",
                            "joinLinkPath": "/join/ABC123",
                            "players": []
                        },
                        {
                            "id": "456e7890-e89b-12d3-a456-426614174001",
                            "roomCode": "XYZ789",
                            "name": "Friend's Game",
                            "hostId": "friend-user-id",
                            "hostUsername": "friend_user",
                            "maxPlayers": 6,
                            "currentPlayers": 5,
                            "status": "WAITING",
                            "createdAt": "2024-01-14T15:20:00",
                            "joinLinkPath": "/join/XYZ789",
                            "players": []
                        }
                    ]
                    """))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token required")
    })
    public ResponseEntity<List<GameRoomResponse>> getMyGameRooms() {
        List<GameRoomResponse> gameRooms = gameRoomService.getGameRoomsForCurrentUser();
        return ResponseEntity.ok(gameRooms);
    }

    @GetMapping("/search")
    @Operation(summary = "Search public game rooms", description = "Search for public game rooms by name. Only returns rooms that are public and accepting players.", operationId = "searchGameRooms")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search results retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = GameRoomResponse.class, type = "array"), examples = @ExampleObject(name = "Search results", value = """
                    [
                        {
                            "id": "123e4567-e89b-12d3-a456-426614174000",
                            "roomCode": "ABC123",
                            "name": "Friday Night Mafia",
                            "hostId": "456e7890-e89b-12d3-a456-426614174001",
                            "hostUsername": "john_doe",
                            "maxPlayers": 10,
                            "currentPlayers": 3,
                            "status": "WAITING",
                            "createdAt": "2024-01-15T10:30:00",
                            "joinLinkPath": "/join/ABC123",
                            "players": [
                                {
                                    "playerId": "456e7890-e89b-12d3-a456-426614174001",
                                    "username": "john_doe",
                                    "isHost": true,
                                    "isReady": true
                                },
                                {
                                    "playerId": "789e0123-e89b-12d3-a456-426614174002",
                                    "username": "jane_doe",
                                    "isHost": false,
                                    "isReady": false
                                }
                            ]
                        }
                    ]
                    """))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token required")
    })
    public ResponseEntity<List<GameRoomResponse>> searchGameRooms(
            @Parameter(description = "Room name to search for (partial match supported)", required = true, example = "Friday") @RequestParam String name) {
        List<GameRoomResponse> gameRooms = gameRoomService.searchGameRoomsByName(name);
        return ResponseEntity.ok(gameRooms);
    }

    @PostMapping("/{roomCode}/leave")
    @Operation(summary = "Leave game room", description = "Leave a game room. If the user is the host and there are other players, a new host will be assigned.", operationId = "leaveGameRoom")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successfully left the room"),
            @ApiResponse(responseCode = "404", description = "Room not found or user not in room", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Not in room", value = """
                    {
                        "error": "Not found",
                        "message": "User is not a member of this room",
                        "timestamp": "2024-01-15T10:30:00Z"
                    }
                    """))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token required")
    })
    public ResponseEntity<Void> leaveGameRoom(
            @Parameter(description = "Unique room code", required = true, example = "ABC123") @PathVariable String roomCode) {
        gameRoomService.leaveRoom(roomCode);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{roomCode}/end")
    @Operation(summary = "End game room", description = "End a game room permanently. Only the host can end the room. All players will be removed.", operationId = "endGameRoom")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Game room ended successfully"),
            @ApiResponse(responseCode = "403", description = "Only host can end the room", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Not host", value = """
                    {
                        "error": "Access denied",
                        "message": "Only the room host can end the game",
                        "timestamp": "2024-01-15T10:30:00Z"
                    }
                    """))),
            @ApiResponse(responseCode = "404", description = "Room not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token required")
    })
    public ResponseEntity<Void> endGameRoom(
            @Parameter(description = "Unique room code", required = true, example = "ABC123") @PathVariable String roomCode) {
        gameRoomService.endRoom(roomCode);
        return ResponseEntity.noContent().build();
    }
}