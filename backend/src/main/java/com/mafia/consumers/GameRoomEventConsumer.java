// filepath: /Users/balicz3k/Documents/eMafia/backend/src/main/java/com/mafia/consumers/GameRoomEventConsumer.java
package com.mafia.consumers;

import com.mafia.config.RabbitMQConfig;
import com.mafia.dto.GameRoomResponse; // Upewnij się, że ten DTO jest serializowalny (np. ma domyślny konstruktor i gettery/settery)
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class GameRoomEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(GameRoomEventConsumer.class);

    @RabbitListener(queues = RabbitMQConfig.ROOM_CREATION_LOG_QUEUE)
    public void handleRoomCreationLog(GameRoomResponse gameRoomResponse) {
        // Ta metoda zostanie wywołana asynchronicznie, gdy wiadomość pojawi się w kolejce
        logger.info("Received room creation event for logging via RabbitMQ: Room Code - {}, Name - {}",
                gameRoomResponse.getRoomCode(), gameRoomResponse.getName());

        // Symulacja dłuższego przetwarzania
        try {
            Thread.sleep(2000); // Poczekaj 2 sekundy
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Consumer interrupted during simulated processing", e);
        }
        logger.info("Finished processing room creation event for: {}", gameRoomResponse.getRoomCode());
    }
}