package com.mafia.repositories;

import com.mafia.models.GameRoom;
import java.util.Optional;
import java.util.UUID;
import com.mafia.models.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GameRoomRepository extends JpaRepository<GameRoom, UUID> {
    boolean existsByRoomCode(String roomCode);

    Optional<GameRoom> findByRoomCode(String roomCode);

    @Query("SELECT gr FROM GameRoom gr LEFT JOIN FETCH gr.players WHERE gr.roomCode = :roomCode")
    Optional<GameRoom> findByRoomCodeWithPlayers(@Param("roomCode") String roomCode);

    @Query("SELECT gr FROM GameRoom gr LEFT JOIN FETCH gr.players p LEFT JOIN FETCH p.user WHERE LOWER(gr.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<GameRoom> findByNameContainingIgnoreCaseWithPlayers(@Param("name") String name);

    @Query("SELECT DISTINCT gr FROM GameRoom gr JOIN gr.players p WHERE p.user = :user ORDER BY gr.createdAt DESC")
    List<GameRoom> findGameRoomsByUser(@Param("user") User user);
}