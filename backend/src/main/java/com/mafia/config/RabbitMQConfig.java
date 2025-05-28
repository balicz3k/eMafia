package com.mafia.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
public class RabbitMQConfig {

    public static final String ROOM_EVENTS_EXCHANGE = "room.events.exchange";
    public static final String ROOM_CREATION_LOG_QUEUE = "room.creation.log.queue";
    public static final String ROOM_CREATED_ROUTING_KEY = "room.created.event";

    @Bean
    DirectExchange roomEventsExchange() {
        return new DirectExchange(ROOM_EVENTS_EXCHANGE);
    }

    @Bean
    Queue roomCreationLogQueue() {
        // Kolejka trwała (durable=true)
        return new Queue(ROOM_CREATION_LOG_QUEUE, true);
    }

    @Bean
    Binding bindingRoomCreationLog(Queue roomCreationLogQueue, DirectExchange roomEventsExchange) {
        return BindingBuilder.bind(roomCreationLogQueue).to(roomEventsExchange).with(ROOM_CREATED_ROUTING_KEY);
    }

    // Konwerter wiadomości na JSON (aby wysyłać obiekty jako JSON)
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // Skonfiguruj RabbitTemplate, aby używał konwertera JSON
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}