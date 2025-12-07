package com.mafia.consumers;

import com.mafia.config.RabbitMQConfig;
import com.mafia.events.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Konsument zdarzeń pokojów z RabbitMQ.
 * 
 * Odbiera zdarzenia związane z zarządzaniem pokojami:
 * - Utworzenie pokoju (istniejące)
 * - Dołączenie gracza do pokoju
 * 
 * W architekturze mikroserwisowej ten komponent mógłby być
 * częścią Room Management Service lub Notification Service.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class RoomEventConsumer {

    /**
     * Konsumuje zdarzenie dołączenia gracza do pokoju.
     * Loguje informacje o nowych graczach dołączających do pokojów.
     */
    @RabbitListener(queues = RabbitMQConfig.ROOM_PLAYER_JOINED_QUEUE)
    public void handlePlayerJoined(PlayerJoinedRoomEvent event) {
        log.info("=== PLAYER JOINED ROOM EVENT ===");
        log.info("Room: {} ({})", event.getRoomName(), event.getRoomCode());
        log.info("Player: {} (ID: {})", event.getPlayerUsername(), event.getPlayerId());
        log.info("Capacity: {}/{}", event.getCurrentPlayerCount(), event.getMaxPlayers());
        log.info("Joined at: {}", event.getJoinedAt());
        
        // Tu można dodać:
        // - Powiadomienia push dla innych graczy w pokoju
        // - Aktualizacja statystyk popularności pokojów
        // - Automatyczne dopasowywanie graczy
        // - Monitoring aktywności
    }
}
