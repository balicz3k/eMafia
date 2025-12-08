package com.mafia.consumers;

import com.mafia.config.RabbitMQConfig;
import com.mafia.dto.gameRoom.CreateGameRoomResp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class GameRoomEventConsumer {

  private static final Logger logger = LoggerFactory.getLogger(GameRoomEventConsumer.class);

  @RabbitListener(queues = RabbitMQConfig.ROOM_CREATION_LOG_QUEUE)
  public void handleRoomCreationLog(CreateGameRoomResp createGameRoomResp) {

    logger.info(
        "Received room creation event for logging via RabbitMQ: Room Code - {}, Name - {}",
        createGameRoomResp.getRoomCode(),
        createGameRoomResp.getName());

    try {
      Thread.sleep(2000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      logger.error("Consumer interrupted during simulated processing", e);
    }
    logger.info(
        "Finished processing room creation event for: {}", createGameRoomResp.getRoomCode());
  }
}
