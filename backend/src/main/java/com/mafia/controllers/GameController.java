package com.mafia.controllers;

import com.mafia.databaseModels.User;
import com.mafia.dto.GameStateResponse;
import com.mafia.dto.GameWithPlayersDto;
import com.mafia.dto.StartGameRequest;
import com.mafia.services.GameService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/games")
@Tag(name = "Games", description = "Game lifecycle endpoints")
@RequiredArgsConstructor
public class GameController {

  private final GameService gameService;

  @PostMapping("/start")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Start a new game in a room")
  @ApiResponse(
      responseCode = "200",
      description = "Game started",
      content = @Content(mediaType = "application/json", schema = @Schema(implementation = GameStateResponse.class)))
  public ResponseEntity<GameStateResponse> start(@Valid @RequestBody StartGameRequest request) {
    return ResponseEntity.ok(gameService.startGame(request));
  }

  @GetMapping("/{gameId}")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Get current game state")
  public ResponseEntity<GameStateResponse> get(@PathVariable UUID gameId) {
    return ResponseEntity.ok(gameService.getState(gameId));
  }

  @PostMapping("/{gameId}/advance-phase")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Advance game phase")
  public ResponseEntity<GameStateResponse> advance(@PathVariable UUID gameId) {
    return ResponseEntity.ok(gameService.advancePhase(gameId));
  }

  @PostMapping("/{gameId}/end")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "End game")
  public ResponseEntity<GameStateResponse> end(@PathVariable UUID gameId) {
    return ResponseEntity.ok(gameService.endGame(gameId));
  }

  @GetMapping("/rooms/{roomCode}/active-game")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Get active game for room with players")
  @ApiResponse(
      responseCode = "200",
      description = "Active game found",
      content = @Content(mediaType = "application/json", schema = @Schema(implementation = GameWithPlayersDto.class)))
  @ApiResponse(
      responseCode = "404",
      description = "No active game found for room")
  public ResponseEntity<GameWithPlayersDto> getActiveGame(@PathVariable String roomCode) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    User currentUser = (User) authentication.getPrincipal();
    
    GameWithPlayersDto game = gameService.getActiveGameByRoomCode(roomCode, currentUser.getId());
    return ResponseEntity.ok(game);
  }
}
