package com.mafia.repositiories;

import com.mafia.models.GameRoom;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameRoomRepository extends JpaRepository<GameRoom, UUID> {
    Optional<GameRoom> findByRoomCode(String roomCode);
    boolean existsByRoomCode(String roomCode);
}