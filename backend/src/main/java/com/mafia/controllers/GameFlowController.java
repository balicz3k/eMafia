package com.mafia.controllers;

import com.mafia.models.GamePhase;
import com.mafia.services.GameEngineService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/game")
public class GameFlowController {

    private final GameEngineService engine;

    public GameFlowController(GameEngineService engine) {
        this.engine = engine;
    }

    @PostMapping("/{code}/start")
    public Map<String, Object> start(@PathVariable String code, @RequestBody List<UUID> userIds) {
        engine.startGame(code, userIds);
        return Map.of("status", "STARTED");
    }

    @PostMapping("/{code}/vote")
    public Map<String, Object> vote(@PathVariable String code,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal(expression = "id") UUID userId) {
        GamePhase phase = GamePhase.valueOf(body.get("phase"));
        UUID target = UUID.fromString(body.get("targetUserId"));
        engine.submitVoteRequireAll(code, userId, phase, target);
        return Map.of("status", "OK");
    }
}