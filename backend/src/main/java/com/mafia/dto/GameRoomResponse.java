// filepath: backend/src/main/java/com/mafia/dto/GameRoomResponse.java
package com.mafia.dto;

import com.mafia.models.GameRoomStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public class GameRoomResponse
{
    private UUID id;
    private String roomCode;
    private String name;
    private String hostUsername;
    private int maxPlayers;
    private GameRoomStatus status;
    private LocalDateTime createdAt;
    private String joinLinkPath; // Np. "/join/" - frontend doda roomCode

    public GameRoomResponse(UUID id, String roomCode, String name, String hostUsername, int maxPlayers,
                            GameRoomStatus status, LocalDateTime createdAt, String joinLinkPath)
    {
        this.id = id;
        this.roomCode = roomCode;
        this.name = name;
        this.hostUsername = hostUsername;
        this.maxPlayers = maxPlayers;
        this.status = status;
        this.createdAt = createdAt;
        this.joinLinkPath = joinLinkPath;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getRoomCode() { return roomCode; }
    public void setRoomCode(String roomCode) { this.roomCode = roomCode; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getHostUsername() { return hostUsername; }
    public void setHostUsername(String hostUsername) { this.hostUsername = hostUsername; }
    public int getMaxPlayers() { return maxPlayers; }
    public void setMaxPlayers(int maxPlayers) { this.maxPlayers = maxPlayers; }
    public GameRoomStatus getStatus() { return status; }
    public void setStatus(GameRoomStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getJoinLinkPath() { return joinLinkPath; }
    public void setJoinLinkPath(String joinLinkPath) { this.joinLinkPath = joinLinkPath; }
}