package com.mafia.consumers;

import com.mafia.config.RabbitMQConfig;
import com.mafia.events.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Konsument zdarzeń gry z RabbitMQ.
 * 
 * Odbiera zdarzenia związane z przebiegiem gry:
 * - Rozpoczęcie gry
 * - Zmiana fazy
 * - Eliminacja gracza
 * - Oddanie głosu
 * - Zakończenie gry
 * 
 * W architekturze mikroserwisowej ten komponent mógłby być
 * wydzielony do osobnego serwisu (np. Game Analytics Service).
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class GameEventConsumer {

    /**
     * Konsumuje zdarzenie rozpoczęcia gry.
     * Loguje informacje o nowej grze i jej uczestnikach.
     */
    @RabbitListener(queues = RabbitMQConfig.GAME_STARTED_QUEUE)
    public void handleGameStarted(GameStartedEvent event) {
        log.info("=== GAME STARTED EVENT ===");
        log.info("Game ID: {}", event.getGameId());
        log.info("Room: {} ({})", event.getRoomName(), event.getRoomCode());
        log.info("Players: {} total, {} mafia, {} citizens", 
            event.getPlayerCount(), event.getMafiaCount(), event.getCitizenCount());
        log.info("Started at: {}", event.getStartedAt());
        
        // Tu można dodać:
        // - Zapis do analityki
        // - Powiadomienia push
        // - Aktualizację statystyk graczy
        // - Integrację z zewnętrznymi systemami
    }

    /**
     * Konsumuje zdarzenie zmiany fazy gry.
     * Loguje przejścia między fazami gry.
     */
    @RabbitListener(queues = RabbitMQConfig.GAME_PHASE_CHANGED_QUEUE)
    public void handlePhaseChanged(GamePhaseChangedEvent event) {
        log.info("=== PHASE CHANGED EVENT ===");
        log.info("Game: {}", event.getGameId());
        log.info("Transition: {} -> {}", event.getPreviousPhase(), event.getNewPhase());
        log.info("Day: {}", event.getDayNumber());
        log.info("Alive: {} players ({} mafia, {} citizens)", 
            event.getAlivePlayersCount(), event.getAliveMafiaCount(), event.getAliveCitizensCount());
        
        // Tu można dodać:
        // - Śledzenie czasu trwania faz
        // - Metryki wydajności gry
        // - Analiza wzorców rozgrywki
    }

    /**
     * Konsumuje zdarzenie eliminacji gracza.
     * Loguje szczegóły eliminacji.
     */
    @RabbitListener(queues = RabbitMQConfig.GAME_PLAYER_ELIMINATED_QUEUE)
    public void handlePlayerEliminated(PlayerEliminatedEvent event) {
        log.info("=== PLAYER ELIMINATED EVENT ===");
        log.info("Game: {}", event.getGameId());
        log.info("Eliminated: {} ({})", event.getEliminatedUsername(), event.getEliminatedRole());
        log.info("Phase: {}, Day: {}", event.getEliminationPhase(), event.getDayNumber());
        log.info("Votes received: {}", event.getVotesReceived());
        log.info("Remaining: {} players ({} mafia, {} citizens)",
            event.getRemainingPlayers(), event.getRemainingMafia(), event.getRemainingCitizens());
        
        // Tu można dodać:
        // - Aktualizacja statystyk "ile razy wyeliminowany"
        // - Analiza skuteczności głosowań
        // - Wykrywanie wzorców zachowań
    }

    /**
     * Konsumuje zdarzenie oddania głosu.
     * Loguje informacje o głosowaniu (poziom DEBUG ze względu na wysoką częstotliwość).
     */
    @RabbitListener(queues = RabbitMQConfig.GAME_VOTE_CAST_QUEUE)
    public void handleVoteCast(VoteCastEvent event) {
        // Używamy DEBUG bo może być dużo takich zdarzeń
        log.debug("=== VOTE CAST EVENT ===");
        log.debug("Game: {}, Phase: {}", event.getGameId(), event.getVotingPhase());
        log.debug("Voter: {} -> Target: {}", event.getVoterUsername(), event.getTargetUsername());
        log.debug("Progress: {}/{}", event.getCurrentVoteCount(), event.getTotalEligibleVoters());
        
        // Tu można dodać:
        // - Analiza wzorców głosowania
        // - Wykrywanie koalicji między graczami
        // - Statystyki aktywności głosowania
    }

    /**
     * Konsumuje zdarzenie zakończenia gry.
     * Loguje pełne podsumowanie rozgrywki.
     */
    @RabbitListener(queues = RabbitMQConfig.GAME_ENDED_QUEUE)
    public void handleGameEnded(GameEndedEvent event) {
        log.info("=== GAME ENDED EVENT ===");
        log.info("Game: {}", event.getGameId());
        log.info("Room: {} ({})", event.getRoomName(), event.getRoomCode());
        log.info("Winner: {} - {}", event.getWinner(), event.getWinReason());
        log.info("Duration: {} seconds over {} days", event.getDurationSeconds(), event.getTotalDays());
        log.info("Started: {}, Ended: {}", event.getStartedAt(), event.getEndedAt());
        
        if (event.getPlayerResults() != null) {
            log.info("Player Results:");
            for (GameEndedEvent.PlayerResult result : event.getPlayerResults()) {
                String status = result.isSurvived() ? "SURVIVED" : "ELIMINATED";
                String winner = result.isWinner() ? " [WINNER]" : "";
                log.info("  - {} ({}) - {}{}", 
                    result.getUsername(), result.getRole(), status, winner);
            }
        }
        
        // Tu można dodać:
        // - Aktualizacja rankingu graczy
        // - Zapis historii gier
        // - Obliczanie statystyk (win rate, survival rate)
        // - Wysłanie powiadomień o zakończeniu gry
        // - Generowanie podsumowania dla graczy
    }
}
