package com.mafia.auth.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Konfiguracja RabbitMQ dla Auth Service.
 * Definiuje exchange i kolejki do komunikacji z innymi serwisami.
 */
@Configuration
public class RabbitMQConfig {

    // Exchange dla zdarzeń użytkownika
    public static final String USER_EVENTS_EXCHANGE = "user.events.exchange";
    
    // Routing keys
    public static final String USER_CREATED_ROUTING_KEY = "user.created";
    public static final String USER_UPDATED_ROUTING_KEY = "user.updated";
    public static final String USER_DELETED_ROUTING_KEY = "user.deleted";

    // Kolejki
    public static final String USER_EVENTS_QUEUE = "user.events.queue";

    @Bean
    public TopicExchange userEventsExchange() {
        return new TopicExchange(USER_EVENTS_EXCHANGE, true, false);
    }

    @Bean
    public Queue userEventsQueue() {
        return QueueBuilder.durable(USER_EVENTS_QUEUE).build();
    }

    @Bean
    public Binding userCreatedBinding(Queue userEventsQueue, TopicExchange userEventsExchange) {
        return BindingBuilder.bind(userEventsQueue)
            .to(userEventsExchange)
            .with("user.*");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        return template;
    }
}
