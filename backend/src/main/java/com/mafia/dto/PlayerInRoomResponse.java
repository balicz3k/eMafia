package com.mafia.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class PlayerInRoomResponse
{
    private UUID playerId;
    private UUID userId;
    private String username;
    private String nicknameInRoom;
    private boolean isAlive;
    private LocalDateTime joinedAt;

    public PlayerInRoomResponse(UUID playerId, UUID userId, String username, String nicknameInRoom, boolean isAlive,
                                LocalDateTime joinedAt)
    {
        this.playerId = playerId;
        this.userId = userId;
        this.username = username;
        this.nicknameInRoom = nicknameInRoom;
        this.isAlive = isAlive;
        this.joinedAt = joinedAt;
    }

    public UUID getPlayerId() { return playerId; }
    public void setPlayerId(UUID playerId) { this.playerId = playerId; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getNicknameInRoom() { return nicknameInRoom; }
    public void setNicknameInRoom(String nicknameInRoom) { this.nicknameInRoom = nicknameInRoom; }
    public boolean isAlive() { return isAlive; }
    public void setAlive(boolean alive) { isAlive = alive; }
    public LocalDateTime getJoinedAt() { return joinedAt; }
    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }
}