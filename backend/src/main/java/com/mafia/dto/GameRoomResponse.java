package com.mafia.dto;

import com.mafia.models.GameRoomStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "Game room information response")
public class GameRoomResponse {

    @Schema(description = "Room's unique identifier", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "Room's unique join code", example = "ABC123")
    private String roomCode;

    @Schema(description = "Room name", example = "Friday Night Mafia")
    private String name;

    @Schema(description = "Host user's unique identifier", example = "456e7890-e89b-12d3-a456-426614174001")
    private UUID hostId;

    @Schema(description = "Host user's username", example = "john_doe")
    private String hostUsername;

    @Schema(description = "Maximum players allowed in the room", example = "8")
    private int maxPlayers;

    @Schema(description = "Current number of players in the room", example = "3")
    private int currentPlayers;

    @Schema(description = "Current room status", example = "WAITING")
    private GameRoomStatus status;

    @Schema(description = "Room creation timestamp", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Join link path for the room", example = "/join/ABC123")
    private String joinLinkPath;

    @Schema(description = "List of all players currently in the room")
    private List<PlayerInRoomResponse> players;

    public GameRoomResponse(UUID id, String roomCode, String name, UUID hostId, String hostUsername, int maxPlayers,
            int currentPlayers, GameRoomStatus status, LocalDateTime createdAt, String joinLinkPath,
            List<PlayerInRoomResponse> players) {
        this.id = id;
        this.roomCode = roomCode;
        this.name = name;
        this.hostId = hostId;
        this.hostUsername = hostUsername;
        this.maxPlayers = maxPlayers;
        this.currentPlayers = currentPlayers;
        this.status = status;
        this.createdAt = createdAt;
        this.joinLinkPath = joinLinkPath;
        this.players = players;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getHostId() {
        return hostId;
    }

    public void setHostId(UUID hostId) {
        this.hostId = hostId;
    }

    public String getHostUsername() {
        return hostUsername;
    }

    public void setHostUsername(String hostUsername) {
        this.hostUsername = hostUsername;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public int getCurrentPlayers() {
        return currentPlayers;
    }

    public void setCurrentPlayers(int currentPlayers) {
        this.currentPlayers = currentPlayers;
    }

    public GameRoomStatus getStatus() {
        return status;
    }

    public void setStatus(GameRoomStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getJoinLinkPath() {
        return joinLinkPath;
    }

    public void setJoinLinkPath(String joinLinkPath) {
        this.joinLinkPath = joinLinkPath;
    }

    public List<PlayerInRoomResponse> getPlayers() {
        return players;
    }

    public void setPlayers(List<PlayerInRoomResponse> players) {
        this.players = players;
    }
}