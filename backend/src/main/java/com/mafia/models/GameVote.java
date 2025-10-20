package com.mafia.models;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "game_vote", indexes = @Index(name = "idx_room_phase_voter_unique", columnList = "roomCode,phase,voterId", unique = true))
public class GameVote {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String roomCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GamePhase phase;

    @Column(nullable = false)
    private UUID voterId;

    @Column(nullable = false)
    private UUID targetUserId;

    public UUID getId() {
        return id;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public GamePhase getPhase() {
        return phase;
    }

    public void setPhase(GamePhase phase) {
        this.phase = phase;
    }

    public UUID getVoterId() {
        return voterId;
    }

    public void setVoterId(UUID voterId) {
        this.voterId = voterId;
    }

    public UUID getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(UUID targetUserId) {
        this.targetUserId = targetUserId;
    }
}