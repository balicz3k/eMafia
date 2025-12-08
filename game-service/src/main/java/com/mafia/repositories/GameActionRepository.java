package com.mafia.repositories;

import com.mafia.databaseModels.GameAction;
import com.mafia.enums.GamePhase;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameActionRepository extends JpaRepository<GameAction, UUID> {
  List<GameAction> findAllByGameIdAndDayNumberAndPhaseOrderByExecutedAtAsc(UUID gameId, int dayNumber, GamePhase phase);
}
