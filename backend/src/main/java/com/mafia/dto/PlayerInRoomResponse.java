package com.mafia.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Player information within a game room")
public class PlayerInRoomResponse {

    @Schema(description = "Player's unique identifier", example = "456e7890-e89b-12d3-a456-426614174001")
    private UUID playerId;

    @Schema(description = "User's unique identifier", example = "456e7890-e89b-12d3-a456-426614174001")
    private UUID userId;

    @Schema(description = "Player's username", example = "john_doe")
    private String username;

    @Schema(description = "Player's display name in room", example = "john_doe")
    private String displayName;

    @Schema(description = "Whether this player is the room host", example = "true")
    private boolean isHost;

    @Schema(description = "Whether the player is ready to start the game", example = "false")
    private boolean isReady;

    @Schema(description = "When player joined the room", example = "2024-01-15T10:30:00")
    private LocalDateTime joinedAt;

    public PlayerInRoomResponse() {
    }

    public PlayerInRoomResponse(UUID playerId, String username, boolean isHost, boolean isReady) {
        this.playerId = playerId;
        this.userId = playerId;
        this.username = username;
        this.displayName = username;
        this.isHost = isHost;
        this.isReady = isReady;
        this.joinedAt = LocalDateTime.now();
    }

    public PlayerInRoomResponse(UUID playerId, UUID userId, String username, String displayName, boolean isHost,
            LocalDateTime joinedAt) {
        this.playerId = playerId;
        this.userId = userId;
        this.username = username;
        this.displayName = displayName;
        this.isHost = isHost;
        this.isReady = false;
        this.joinedAt = joinedAt;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public void setPlayerId(UUID playerId) {
        this.playerId = playerId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public boolean isHost() {
        return isHost;
    }

    public void setHost(boolean host) {
        isHost = host;
    }

    public boolean isReady() {
        return isReady;
    }

    public void setReady(boolean ready) {
        isReady = ready;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }
}