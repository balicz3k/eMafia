package com.mafia.repositories;

import com.mafia.databaseModels.Game;
import com.mafia.enums.GameStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameRepository extends JpaRepository<Game, UUID> {
  Optional<Game> findById(UUID id);
  List<Game> findByRoom_IdAndStatus(UUID roomId, GameStatus status);
  Optional<Game> findFirstByRoom_IdOrderByCreatedAtDesc(UUID roomId);
}
