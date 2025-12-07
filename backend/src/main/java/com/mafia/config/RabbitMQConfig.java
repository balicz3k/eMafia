package com.mafia.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Konfiguracja RabbitMQ dla architektury mikroserwisowej.
 * 
 * Definiuje exchange'e, kolejki i bindingi dla różnych typów zdarzeń:
 * - ROOM_EVENTS: zdarzenia związane z pokojami (tworzenie, dołączanie)
 * - GAME_EVENTS: zdarzenia rozgrywki (start, fazy, głosowania, koniec)
 * - ANALYTICS_EVENTS: zdarzenia do zbierania statystyk
 * 
 * Architektura oparta na wzorcu Event-Driven Architecture (EDA).
 */
@Configuration
@Profile("!test")
public class RabbitMQConfig {

    // ==================== ROOM EVENTS ====================
    // Zdarzenia związane z zarządzaniem pokojami
    
    public static final String ROOM_EVENTS_EXCHANGE = "room.events.exchange";
    public static final String ROOM_CREATION_LOG_QUEUE = "room.creation.log.queue";
    public static final String ROOM_CREATED_ROUTING_KEY = "room.created.event";
    public static final String ROOM_PLAYER_JOINED_QUEUE = "room.player.joined.queue";
    public static final String ROOM_PLAYER_JOINED_ROUTING_KEY = "room.player.joined.event";

    // ==================== GAME EVENTS ====================
    // Zdarzenia związane z przebiegiem rozgrywki
    
    public static final String GAME_EVENTS_EXCHANGE = "game.events.exchange";
    
    // Kolejki dla zdarzeń gry
    public static final String GAME_STARTED_QUEUE = "game.started.queue";
    public static final String GAME_PHASE_CHANGED_QUEUE = "game.phase.changed.queue";
    public static final String GAME_PLAYER_ELIMINATED_QUEUE = "game.player.eliminated.queue";
    public static final String GAME_VOTE_CAST_QUEUE = "game.vote.cast.queue";
    public static final String GAME_ENDED_QUEUE = "game.ended.queue";
    
    // Routing keys dla zdarzeń gry
    public static final String GAME_STARTED_ROUTING_KEY = "game.started";
    public static final String GAME_PHASE_CHANGED_ROUTING_KEY = "game.phase.changed";
    public static final String GAME_PLAYER_ELIMINATED_ROUTING_KEY = "game.player.eliminated";
    public static final String GAME_VOTE_CAST_ROUTING_KEY = "game.vote.cast";
    public static final String GAME_ENDED_ROUTING_KEY = "game.ended";

    // ==================== ANALYTICS EVENTS ====================
    // Zdarzenia do zbierania statystyk i analityki
    
    public static final String ANALYTICS_EXCHANGE = "analytics.exchange";
    public static final String ANALYTICS_QUEUE = "analytics.events.queue";
    public static final String ANALYTICS_ROUTING_KEY = "analytics.#";
    
    // Dodatkowe kolejki analityczne dla poszczególnych typów zdarzeń
    public static final String ANALYTICS_GAME_STARTED_QUEUE = "analytics.game.started.queue";
    public static final String ANALYTICS_GAME_ENDED_QUEUE = "analytics.game.ended.queue";
    public static final String ANALYTICS_PHASE_CHANGED_QUEUE = "analytics.phase.changed.queue";
    public static final String ANALYTICS_PLAYER_ELIMINATED_QUEUE = "analytics.player.eliminated.queue";
    public static final String ANALYTICS_VOTE_CAST_QUEUE = "analytics.vote.cast.queue";
    public static final String ANALYTICS_PLAYER_JOINED_QUEUE = "analytics.player.joined.queue";

    // ==================== ROOM EVENTS BEANS ====================
    
    @Bean
    DirectExchange roomEventsExchange() {
        return new DirectExchange(ROOM_EVENTS_EXCHANGE);
    }

    @Bean
    Queue roomCreationLogQueue() {
        return new Queue(ROOM_CREATION_LOG_QUEUE, true);
    }
    
    @Bean
    Queue roomPlayerJoinedQueue() {
        return new Queue(ROOM_PLAYER_JOINED_QUEUE, true);
    }

    @Bean
    Binding bindingRoomCreationLog(Queue roomCreationLogQueue, DirectExchange roomEventsExchange) {
        return BindingBuilder.bind(roomCreationLogQueue).to(roomEventsExchange).with(ROOM_CREATED_ROUTING_KEY);
    }
    
    @Bean
    Binding bindingRoomPlayerJoined(Queue roomPlayerJoinedQueue, DirectExchange roomEventsExchange) {
        return BindingBuilder.bind(roomPlayerJoinedQueue).to(roomEventsExchange).with(ROOM_PLAYER_JOINED_ROUTING_KEY);
    }

    // ==================== GAME EVENTS BEANS ====================
    
    @Bean
    DirectExchange gameEventsExchange() {
        return new DirectExchange(GAME_EVENTS_EXCHANGE);
    }
    
    @Bean
    Queue gameStartedQueue() {
        return new Queue(GAME_STARTED_QUEUE, true);
    }
    
    @Bean
    Queue gamePhaseChangedQueue() {
        return new Queue(GAME_PHASE_CHANGED_QUEUE, true);
    }
    
    @Bean
    Queue gamePlayerEliminatedQueue() {
        return new Queue(GAME_PLAYER_ELIMINATED_QUEUE, true);
    }
    
    @Bean
    Queue gameVoteCastQueue() {
        return new Queue(GAME_VOTE_CAST_QUEUE, true);
    }
    
    @Bean
    Queue gameEndedQueue() {
        return new Queue(GAME_ENDED_QUEUE, true);
    }
    
    @Bean
    Binding bindingGameStarted(Queue gameStartedQueue, DirectExchange gameEventsExchange) {
        return BindingBuilder.bind(gameStartedQueue).to(gameEventsExchange).with(GAME_STARTED_ROUTING_KEY);
    }
    
    @Bean
    Binding bindingGamePhaseChanged(Queue gamePhaseChangedQueue, DirectExchange gameEventsExchange) {
        return BindingBuilder.bind(gamePhaseChangedQueue).to(gameEventsExchange).with(GAME_PHASE_CHANGED_ROUTING_KEY);
    }
    
    @Bean
    Binding bindingGamePlayerEliminated(Queue gamePlayerEliminatedQueue, DirectExchange gameEventsExchange) {
        return BindingBuilder.bind(gamePlayerEliminatedQueue).to(gameEventsExchange).with(GAME_PLAYER_ELIMINATED_ROUTING_KEY);
    }
    
    @Bean
    Binding bindingGameVoteCast(Queue gameVoteCastQueue, DirectExchange gameEventsExchange) {
        return BindingBuilder.bind(gameVoteCastQueue).to(gameEventsExchange).with(GAME_VOTE_CAST_ROUTING_KEY);
    }
    
    @Bean
    Binding bindingGameEnded(Queue gameEndedQueue, DirectExchange gameEventsExchange) {
        return BindingBuilder.bind(gameEndedQueue).to(gameEventsExchange).with(GAME_ENDED_ROUTING_KEY);
    }

    // ==================== ANALYTICS BEANS ====================
    
    @Bean
    TopicExchange analyticsExchange() {
        return new TopicExchange(ANALYTICS_EXCHANGE);
    }
    
    @Bean
    Queue analyticsQueue() {
        return new Queue(ANALYTICS_QUEUE, true);
    }
    
    @Bean
    Queue analyticsGameStartedQueue() {
        return new Queue(ANALYTICS_GAME_STARTED_QUEUE, true);
    }
    
    @Bean
    Queue analyticsGameEndedQueue() {
        return new Queue(ANALYTICS_GAME_ENDED_QUEUE, true);
    }
    
    @Bean
    Queue analyticsPhaseChangedQueue() {
        return new Queue(ANALYTICS_PHASE_CHANGED_QUEUE, true);
    }
    
    @Bean
    Queue analyticsPlayerEliminatedQueue() {
        return new Queue(ANALYTICS_PLAYER_ELIMINATED_QUEUE, true);
    }
    
    @Bean
    Queue analyticsVoteCastQueue() {
        return new Queue(ANALYTICS_VOTE_CAST_QUEUE, true);
    }
    
    @Bean
    Queue analyticsPlayerJoinedQueue() {
        return new Queue(ANALYTICS_PLAYER_JOINED_QUEUE, true);
    }
    
    @Bean
    Binding bindingAnalytics(Queue analyticsQueue, TopicExchange analyticsExchange) {
        return BindingBuilder.bind(analyticsQueue).to(analyticsExchange).with(ANALYTICS_ROUTING_KEY);
    }
    
    // Bindingi dla kolejek analitycznych - podpinamy do GAME_EVENTS i ROOM_EVENTS exchange
    @Bean
    Binding bindingAnalyticsGameStarted(Queue analyticsGameStartedQueue, DirectExchange gameEventsExchange) {
        return BindingBuilder.bind(analyticsGameStartedQueue).to(gameEventsExchange).with(GAME_STARTED_ROUTING_KEY);
    }
    
    @Bean
    Binding bindingAnalyticsGameEnded(Queue analyticsGameEndedQueue, DirectExchange gameEventsExchange) {
        return BindingBuilder.bind(analyticsGameEndedQueue).to(gameEventsExchange).with(GAME_ENDED_ROUTING_KEY);
    }
    
    @Bean
    Binding bindingAnalyticsPhaseChanged(Queue analyticsPhaseChangedQueue, DirectExchange gameEventsExchange) {
        return BindingBuilder.bind(analyticsPhaseChangedQueue).to(gameEventsExchange).with(GAME_PHASE_CHANGED_ROUTING_KEY);
    }
    
    @Bean
    Binding bindingAnalyticsPlayerEliminated(Queue analyticsPlayerEliminatedQueue, DirectExchange gameEventsExchange) {
        return BindingBuilder.bind(analyticsPlayerEliminatedQueue).to(gameEventsExchange).with(GAME_PLAYER_ELIMINATED_ROUTING_KEY);
    }
    
    @Bean
    Binding bindingAnalyticsVoteCast(Queue analyticsVoteCastQueue, DirectExchange gameEventsExchange) {
        return BindingBuilder.bind(analyticsVoteCastQueue).to(gameEventsExchange).with(GAME_VOTE_CAST_ROUTING_KEY);
    }
    
    @Bean
    Binding bindingAnalyticsPlayerJoined(Queue analyticsPlayerJoinedQueue, DirectExchange roomEventsExchange) {
        return BindingBuilder.bind(analyticsPlayerJoinedQueue).to(roomEventsExchange).with(ROOM_PLAYER_JOINED_ROUTING_KEY);
    }

    // ==================== COMMON BEANS ====================

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}