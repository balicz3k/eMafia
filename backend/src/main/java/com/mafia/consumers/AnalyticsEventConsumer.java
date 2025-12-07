package com.mafia.consumers;

import com.mafia.config.RabbitMQConfig;
import com.mafia.events.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Konsument zdarzeń dla celów analitycznych.
 * 
 * Zbiera metryki i statystyki z wszystkich zdarzeń gry.
 * Przechowuje agregowane dane w pamięci (w produkcji
 * byłoby to zapisywane do bazy danych lub systemu metryk).
 * 
 * W architekturze mikroserwisowej ten komponent byłby
 * wydzielony jako osobny Analytics Service z własną bazą danych.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AnalyticsEventConsumer {

    // Proste metryki w pamięci (w produkcji użyłoby się bazy danych)
    private final AtomicInteger totalGamesStarted = new AtomicInteger(0);
    private final AtomicInteger totalGamesEnded = new AtomicInteger(0);
    private final AtomicInteger mafiaWins = new AtomicInteger(0);
    private final AtomicInteger citizenWins = new AtomicInteger(0);
    private final AtomicInteger totalEliminations = new AtomicInteger(0);
    private final AtomicLong totalGameDurationSeconds = new AtomicLong(0);
    private final AtomicInteger totalVotesCast = new AtomicInteger(0);
    private final AtomicInteger totalPlayersJoined = new AtomicInteger(0);
    private final ConcurrentHashMap<String, AtomicInteger> phaseTransitionCounts = new ConcurrentHashMap<>();

    /**
     * Analizuje zdarzenie rozpoczęcia gry.
     */
    @RabbitListener(queues = RabbitMQConfig.ANALYTICS_GAME_STARTED_QUEUE)
    public void analyzeGameStarted(GameStartedEvent event) {
        int count = totalGamesStarted.incrementAndGet();
        log.info("[Analytics] Games started total: {}", count);
        log.info("[Analytics] Average players per game: {}", 
            event.getPlayerCount()); // W produkcji byłaby średnia
    }

    /**
     * Analizuje zdarzenie zakończenia gry.
     */
    @RabbitListener(queues = RabbitMQConfig.ANALYTICS_GAME_ENDED_QUEUE)
    public void analyzeGameEnded(GameEndedEvent event) {
        int endedCount = totalGamesEnded.incrementAndGet();
        
        // Zlicz wygrane
        if ("MAFIA".equals(event.getWinner())) {
            mafiaWins.incrementAndGet();
        } else if ("CITIZENS".equals(event.getWinner())) {
            citizenWins.incrementAndGet();
        }
        
        // Akumuluj czas gry
        totalGameDurationSeconds.addAndGet(event.getDurationSeconds());
        
        // Oblicz statystyki
        double mafiaWinRate = endedCount > 0 ? 
            (double) mafiaWins.get() / endedCount * 100 : 0;
        long avgDuration = endedCount > 0 ? 
            totalGameDurationSeconds.get() / endedCount : 0;
        
        log.info("[Analytics] ========== GAME STATISTICS ==========");
        log.info("[Analytics] Total games completed: {}", endedCount);
        log.info("[Analytics] Mafia wins: {} ({:.1f}%)", mafiaWins.get(), mafiaWinRate);
        log.info("[Analytics] Citizen wins: {} ({:.1f}%)", citizenWins.get(), 100 - mafiaWinRate);
        log.info("[Analytics] Average game duration: {} seconds", avgDuration);
        log.info("[Analytics] ===========================================");
    }

    /**
     * Analizuje zdarzenie zmiany fazy.
     */
    @RabbitListener(queues = RabbitMQConfig.ANALYTICS_PHASE_CHANGED_QUEUE)
    public void analyzePhaseChanged(GamePhaseChangedEvent event) {
        String transition = event.getPreviousPhase() + " -> " + event.getNewPhase();
        phaseTransitionCounts
            .computeIfAbsent(transition, k -> new AtomicInteger(0))
            .incrementAndGet();
        
        log.debug("[Analytics] Phase transition recorded: {}", transition);
    }

    /**
     * Analizuje zdarzenie eliminacji.
     */
    @RabbitListener(queues = RabbitMQConfig.ANALYTICS_PLAYER_ELIMINATED_QUEUE)
    public void analyzePlayerEliminated(PlayerEliminatedEvent event) {
        int count = totalEliminations.incrementAndGet();
        log.info("[Analytics] Total eliminations: {}", count);
        log.info("[Analytics] Eliminated role: {} in phase: {}", 
            event.getEliminatedRole(), event.getEliminationPhase());
    }

    /**
     * Analizuje zdarzenie oddania głosu.
     */
    @RabbitListener(queues = RabbitMQConfig.ANALYTICS_VOTE_CAST_QUEUE)
    public void analyzeVoteCast(VoteCastEvent event) {
        int count = totalVotesCast.incrementAndGet();
        // Loguj tylko co 10 głosów żeby nie zaśmiecać logów
        if (count % 10 == 0) {
            log.info("[Analytics] Total votes cast: {}", count);
        }
    }

    /**
     * Analizuje zdarzenie dołączenia gracza.
     */
    @RabbitListener(queues = RabbitMQConfig.ANALYTICS_PLAYER_JOINED_QUEUE)
    public void analyzePlayerJoined(PlayerJoinedRoomEvent event) {
        int count = totalPlayersJoined.incrementAndGet();
        log.info("[Analytics] Total players joined rooms: {}", count);
        log.info("[Analytics] Room {} fill rate: {}/{}",
            event.getRoomCode(), event.getCurrentPlayerCount(), event.getMaxPlayers());
    }

    /**
     * Pobiera bieżące statystyki (może być udostępnione przez REST API).
     */
    public AnalyticsSnapshot getSnapshot() {
        int endedCount = totalGamesEnded.get();
        return new AnalyticsSnapshot(
            totalGamesStarted.get(),
            endedCount,
            mafiaWins.get(),
            citizenWins.get(),
            totalEliminations.get(),
            totalVotesCast.get(),
            totalPlayersJoined.get(),
            endedCount > 0 ? totalGameDurationSeconds.get() / endedCount : 0
        );
    }

    /**
     * Snapshot statystyk do zwrócenia przez API.
     */
    public record AnalyticsSnapshot(
        int gamesStarted,
        int gamesEnded,
        int mafiaWins,
        int citizenWins,
        int totalEliminations,
        int totalVotesCast,
        int totalPlayersJoined,
        long avgGameDurationSeconds
    ) {}
}
