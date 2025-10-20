package com.mafia.models;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "player_game_state", indexes = @Index(name = "idx_room_user_unique", columnList = "roomCode,userId", unique = true))
public class PlayerGameState {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String roomCode;

    @Column(nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GameRole role;

    @Column(nullable = false)
    private boolean alive = true;

    @Column(nullable = false)
    private boolean suspended = false;

    public UUID getId() {
        return id;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public GameRole getRole() {
        return role;
    }

    public void setGameRole(GameRole role) {
        this.role = role;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public boolean isSuspended() {
        return suspended;
    }

    public void setSuspended(boolean suspended) {
        this.suspended = suspended;
    }
}