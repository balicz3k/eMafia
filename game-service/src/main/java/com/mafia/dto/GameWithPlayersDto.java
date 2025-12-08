package com.mafia.dto;

import com.mafia.enums.GamePhase;
import com.mafia.enums.GameStatus;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO dla aktywnej gry z listą graczy
 * Używane przez frontend do wyświetlenia stanu gry
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameWithPlayersDto {
  private UUID gameId;
  private String roomCode;
  private GameStatus status;
  private GamePhase currentPhase;
  private int currentDayNumber;
  private List<GamePlayerDto> players;
}
