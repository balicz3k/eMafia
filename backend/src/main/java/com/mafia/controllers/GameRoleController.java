package com.mafia.controllers;

import com.mafia.models.PlayerGameState;
import com.mafia.models.GameRole;
import com.mafia.repositories.PlayerGameStateRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/game")
public class GameRoleController {
    private final PlayerGameStateRepository players;

    public GameRoleController(PlayerGameStateRepository players) {
        this.players = players;
    }

    @GetMapping("/{code}/me/role")
    public Map<String, Object> myRole(@PathVariable String code,
            @AuthenticationPrincipal(expression = "id") UUID userId) {
        PlayerGameState me = players.findByRoomCodeAndUserId(code, userId);
        if (me == null)
            throw new IllegalStateException("Not in room");

        boolean revealMafia = false;

        List<Map<String, Object>> known = List.of();
        if (revealMafia && me.getRole() == GameRole.MAFIA) {
            known = players.findByRoomCodeAndAliveTrueAndRole(code, GameRole.MAFIA).stream()
                    .filter(p -> !p.getUserId().equals(userId))
                    .map(p -> Map.<String, Object>of("userId", p.getUserId(), "alive", p.isAlive()))
                    .toList();
        }

        return Map.of(
                "role", me.getRole().name(),
                "alive", me.isAlive(),
                "knownMafiosi", known);
    }
}