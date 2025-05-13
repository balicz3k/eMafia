package com.mafia.repositiories;

import com.mafia.models.GameRoom;
import com.mafia.models.PlayerInRoom;
import com.mafia.models.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerInRoomRepository extends JpaRepository<PlayerInRoom, UUID> {
    boolean existsByUserAndGameRoom(User user, GameRoom gameRoom);
    Optional<PlayerInRoom> findByUserAndGameRoom(User user, GameRoom gameRoom);
}