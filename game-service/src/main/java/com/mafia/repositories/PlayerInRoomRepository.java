package com.mafia.repositories;

import com.mafia.databaseModels.GameRoom;
import com.mafia.databaseModels.PlayerInRoom;
import com.mafia.databaseModels.User;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerInRoomRepository extends JpaRepository<PlayerInRoom, UUID> {

  List<PlayerInRoom> findAllByUser(User user);

  List<PlayerInRoom> findAllByGameRoom(GameRoom gameRoom);
}
