package com.mafia.repositories;

import com.mafia.models.GamePhase;
import com.mafia.models.GameVote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GameVoteRepository extends JpaRepository<GameVote, UUID> {
    long countByRoomCodeAndPhase(String roomCode, GamePhase phase);

    Optional<GameVote> findByRoomCodeAndPhaseAndVoterId(String roomCode, GamePhase phase, UUID voterId);

    List<GameVote> findByRoomCodeAndPhase(String roomCode, GamePhase phase);

    void deleteByRoomCodeAndPhase(String roomCode, GamePhase phase);
}