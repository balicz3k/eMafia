package com.mafia.services;

import com.mafia.databaseModels.*;
import com.mafia.dto.GameStateResponse;
import com.mafia.dto.StartGameRequest;
import com.mafia.enums.GamePhase;
import com.mafia.enums.GameRole;
import com.mafia.enums.GameRoomStatus;
import com.mafia.enums.GameStatus;
import com.mafia.exceptions.GameRoomNotFoundException;
import com.mafia.repositories.*;
import com.mafia.services.voting.VotingSessionService;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameService {
  private final GameRepository gameRepository;
  private final GameRoomRepository gameRoomRepository;
  private final GamePlayerRepository gamePlayerRepository;
  private final PlayerInRoomRepository playerInRoomRepository;
  private final VotingSessionService votingSessionService;

  @Transactional
  public GameStateResponse startGame(StartGameRequest request) {
    log.info(
        "Starting game for room {} with {} mafia and {}s discussion time",
        request.getRoomId(),
        request.getMafiaCount(),
        request.getDiscussionTimeSeconds());

    GameRoom room =
        gameRoomRepository
            .findById(request.getRoomId())
            .orElseThrow(() -> new GameRoomNotFoundException("Room not found"));

    // Ensure no active game exists for this room
    List<Game> active = gameRepository.findByRoom_IdAndStatus(room.getId(), GameStatus.IN_PROGRESS);
    if (!active.isEmpty()) {
      log.warn("Active game already exists for room {}", room.getId());
      throw new IllegalStateException("An active game already exists for this room");
    }

    // Pobierz graczy z pokoju
    List<PlayerInRoom> playersInRoom = playerInRoomRepository.findAllByGameRoom(room);
    if (playersInRoom.isEmpty()) {
      throw new IllegalStateException("No players in room");
    }

    int totalPlayers = playersInRoom.size();
    int mafiaCount = request.getMafiaCount();

    // Walidacja liczby graczy
    if (totalPlayers < 3) {
      throw new IllegalStateException("At least 3 players required to start game");
    }

    if (mafiaCount >= totalPlayers) {
      throw new IllegalStateException(
          "Mafia count must be less than total players (mafia: "
              + mafiaCount
              + ", total: "
              + totalPlayers
              + ")");
    }

    // Zapisz konfigurację w pokoju
    room.setMafiaCount(mafiaCount);
    room.setDiscussionTimeSeconds(request.getDiscussionTimeSeconds());
    room.setGameRoomStatus(GameRoomStatus.GAME_IN_PROGRESS);
    gameRoomRepository.save(room);

    // Utwórz grę
    Game game = new Game();
    game.setRoom(room);
    game.setStatus(GameStatus.IN_PROGRESS);
    game.setCurrentPhase(GamePhase.NIGHT_VOTE); // Zaczynamy od nocy
    game.setCurrentDayNumber(1);
    game.setStartedAt(LocalDateTime.now());

    Game savedGame = gameRepository.save(game);
    log.info("Game created: {}", savedGame.getId());

    // Przydziel role graczom
    assignRolesToPlayers(savedGame, playersInRoom, mafiaCount);

    // Rozpocznij pierwszą sesję głosowania (NIGHT_VOTE)
    try {
      votingSessionService.startVotingSession(
          savedGame, GamePhase.NIGHT_VOTE, request.getDiscussionTimeSeconds());
      log.info("First voting session (NIGHT_VOTE) started for game {}", savedGame.getId());
    } catch (Exception e) {
      log.error("Error starting voting session", e);
      throw new IllegalStateException("Failed to start voting session: " + e.getMessage());
    }

    return toResponse(savedGame);
  }

  /**
   * Przydziela role graczom losowo
   */
  private void assignRolesToPlayers(Game game, List<PlayerInRoom> playersInRoom, int mafiaCount) {
    log.info("Assigning roles: {} mafia, {} citizens", mafiaCount, playersInRoom.size() - mafiaCount);

    // Losowo wybierz graczy
    List<PlayerInRoom> shuffledPlayers = new ArrayList<>(playersInRoom);
    Collections.shuffle(shuffledPlayers);

    // Przydziel role
    for (int i = 0; i < shuffledPlayers.size(); i++) {
      PlayerInRoom playerInRoom = shuffledPlayers.get(i);
      GameRole role = i < mafiaCount ? GameRole.MAFIA : GameRole.CITIZEN;

      GamePlayer gamePlayer = new GamePlayer();
      gamePlayer.setGame(game);
      gamePlayer.setUser(playerInRoom.getUser());
      gamePlayer.setGameNick(playerInRoom.getUser().getUsername());
      gamePlayer.setAssignedRole(role);
      gamePlayer.setAlive(true);

      gamePlayerRepository.save(gamePlayer);

      log.info(
          "Player {} assigned role: {}",
          playerInRoom.getUser().getUsername(),
          role);
    }
  }

  public GameStateResponse getState(UUID gameId) {
    Game game =
        gameRepository
            .findById(gameId)
            .orElseThrow(() -> new IllegalArgumentException("Game not found"));
    return toResponse(game);
  }

  @Transactional
  public GameStateResponse advancePhase(UUID gameId) {
    Game game =
        gameRepository
            .findById(gameId)
            .orElseThrow(() -> new IllegalArgumentException("Game not found"));

    // Simple phase switch logic
    if (game.getCurrentPhase() == GamePhase.DAY_VOTE) {
      game.setCurrentPhase(GamePhase.NIGHT_VOTE);
    } else {
      game.setCurrentPhase(GamePhase.DAY_VOTE);
      game.setCurrentDayNumber(game.getCurrentDayNumber() + 1);
    }

    Game saved = gameRepository.save(game);
    return toResponse(saved);
  }

  @Transactional
  public GameStateResponse endGame(UUID gameId) {
    Game game =
        gameRepository
            .findById(gameId)
            .orElseThrow(() -> new IllegalArgumentException("Game not found"));

    game.setStatus(GameStatus.FINISHED);
    game.setEndedAt(LocalDateTime.now());

    Game saved = gameRepository.save(game);

    // Optionally update room status back
    GameRoom room = saved.getRoom();
    room.setGameRoomStatus(GameRoomStatus.OPEN);
    gameRoomRepository.save(room);

    return toResponse(saved);
  }

  /**
   * Sprawdza warunki wygranej po każdej eliminacji
   */
  @Transactional
  public void checkGameEndConditions(Game game) {
    log.info("Checking game end conditions for game {}", game.getId());

    // Policz żywych graczy według ról
    int aliveMafia =
        gamePlayerRepository.countByGameAndAssignedRoleAndIsAlive(game, GameRole.MAFIA, true);

    int totalAlive = gamePlayerRepository.countByGameAndIsAlive(game, true);
    int aliveCitizens = totalAlive - aliveMafia;

    log.info(
        "Game {} status: {} alive mafia, {} alive citizens",
        game.getId(),
        aliveMafia,
        aliveCitizens);

    // Warunek wygranej Obywateli: wszystkie mafie wyeliminowane
    if (aliveMafia == 0) {
      log.info("Citizens win! All mafia eliminated in game {}", game.getId());
      endGameWithWinner(game, "CITIZENS");
      return;
    }

    // Warunek wygranej Mafii: mafia >= obywatele
    if (aliveMafia >= aliveCitizens) {
      log.info("Mafia wins! Mafia count >= citizens in game {}", game.getId());
      endGameWithWinner(game, "MAFIA");
      return;
    }

    // Gra trwa dalej
    log.info("Game {} continues", game.getId());
  }

  /**
   * Kończy grę z określonym zwycięzcą
   */
  @Transactional
  public void endGameWithWinner(Game game, String winner) {
    log.info("Ending game {} with winner: {}", game.getId(), winner);

    game.setStatus(GameStatus.FINISHED);
    game.setCurrentPhase(GamePhase.GAME_OVER);
    game.setEndedAt(LocalDateTime.now());
    gameRepository.save(game);

    // Aktualizuj status pokoju
    GameRoom room = game.getRoom();
    room.setGameRoomStatus(GameRoomStatus.OPEN);
    gameRoomRepository.save(room);

    log.info("Game {} ended. Winner: {}", game.getId(), winner);
  }

  /**
   * Pobiera aktywną grę dla pokoju z listą graczy
   */
  @Transactional(readOnly = true)
  public com.mafia.dto.GameWithPlayersDto getActiveGameByRoomCode(String roomCode, UUID currentUserId) {
    log.info("Getting active game for room: {}", roomCode);

    GameRoom room =
        gameRoomRepository
            .findByRoomCode(roomCode)
            .orElseThrow(() -> new GameRoomNotFoundException("Room not found: " + roomCode));

    // Znajdź aktywną grę
    List<Game> activeGames = gameRepository.findByRoom_IdAndStatus(room.getId(), GameStatus.IN_PROGRESS);
    
    if (activeGames.isEmpty()) {
      throw new IllegalStateException("No active game found for room: " + roomCode);
    }

    Game game = activeGames.get(0);
    
    // Pobierz graczy
    List<GamePlayer> gamePlayers = gamePlayerRepository.findByGame(game);
    boolean gameEnded = game.getStatus() == GameStatus.FINISHED;

    List<com.mafia.dto.GamePlayerDto> playerDtos =
        gamePlayers.stream()
            .map(gp -> toGamePlayerDto(gp, currentUserId, gameEnded))
            .collect(Collectors.toList());

    com.mafia.dto.GameWithPlayersDto dto = new com.mafia.dto.GameWithPlayersDto();
    dto.setGameId(game.getId());
    dto.setRoomCode(roomCode);
    dto.setStatus(game.getStatus());
    dto.setCurrentPhase(game.getCurrentPhase());
    dto.setCurrentDayNumber(game.getCurrentDayNumber());
    dto.setPlayers(playerDtos);

    return dto;
  }

  /**
   * Konwertuje GamePlayer do DTO z uwzględnieniem widoczności roli
   */
  private com.mafia.dto.GamePlayerDto toGamePlayerDto(GamePlayer gp, UUID currentUserId, boolean gameEnded) {
    com.mafia.dto.GamePlayerDto dto = new com.mafia.dto.GamePlayerDto();
    dto.setUserId(gp.getUser().getId());
    dto.setUsername(gp.getUser().getUsername());
    dto.setGameNick(gp.getGameNick());
    // Lombok @Data generates setters without the 'is' prefix: setAlive, setCurrentUser
    dto.setAlive(gp.isAlive());
    dto.setCurrentUser(gp.getUser().getId().equals(currentUserId));

    // Rola widoczna tylko dla właściciela lub po zakończeniu gry
    if (gp.getUser().getId().equals(currentUserId) || gameEnded) {
      dto.setAssignedRole(gp.getAssignedRole());
    } else {
      dto.setAssignedRole(null);
    }

    return dto;
  }

  private GameStateResponse toResponse(Game game) {
    return new GameStateResponse(
        game.getId(),
        game.getRoom().getId(),
        game.getStatus(),
        game.getCurrentPhase(),
        game.getCurrentDayNumber(),
        game.getCreatedAt(),
        game.getStartedAt(),
        game.getEndedAt());
  }
}
