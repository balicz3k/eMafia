package com.mafia.repositiories;

import com.mafia.models.GameRoom;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;   // DODAJ IMPORT
import org.springframework.data.repository.query.Param; // DODAJ IMPORT
import org.springframework.stereotype.Repository;

@Repository
public interface GameRoomRepository extends JpaRepository<GameRoom, UUID> {
    boolean existsByRoomCode(String roomCode);
    Optional<GameRoom> findByRoomCode(String roomCode);

    @Query("SELECT gr FROM GameRoom gr LEFT JOIN FETCH gr.players WHERE gr.roomCode = :roomCode")
    Optional<GameRoom> findByRoomCodeWithPlayers(@Param("roomCode") String roomCode);
}