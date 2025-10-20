package com.mafia.repositories;

import com.mafia.models.PlayerGameState;
import com.mafia.models.GameRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PlayerGameStateRepository extends JpaRepository<PlayerGameState, UUID> {
    PlayerGameState findByRoomCodeAndUserId(String roomCode, UUID userId);

    long countByRoomCodeAndAliveTrue(String roomCode);

    List<PlayerGameState> findByRoomCodeAndAliveTrue(String roomCode);

    List<PlayerGameState> findByRoomCodeAndAliveTrueAndRole(String roomCode, GameRole role);
}