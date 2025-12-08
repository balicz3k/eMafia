package com.mafia.services;

import com.mafia.databaseModels.GameRoom;
import com.mafia.databaseModels.PlayerInRoom;
import com.mafia.databaseModels.User;
import com.mafia.repositories.PlayerInRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlayerInRoomService {
  private final PlayerInRoomRepository playerInRoomRepository;

  public void addPlayerToGameRoom(User user, GameRoom room) {
    var playerInRoom = new PlayerInRoom();
    playerInRoom.setUser(user);
    playerInRoom.setGameRoom(room);
    playerInRoomRepository.save(playerInRoom);
  }
}
