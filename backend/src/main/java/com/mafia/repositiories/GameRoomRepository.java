package com.mafia.repositiories;

import com.mafia.models.GameRoom;
import com.mafia.models.User;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRoomRepository extends JpaRepository<GameRoom, UUID>
{
    List<GameRoom> findByStatus(GameRoom.GameRoomStatus status);
    List<GameRoom> findByHost(User host);
}