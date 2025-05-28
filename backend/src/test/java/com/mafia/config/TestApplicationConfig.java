package com.mafia.config;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import static org.mockito.Mockito.mock;

@TestConfiguration
@Profile("test")
public class TestApplicationConfig {

    @Bean
    @Primary
    public RabbitTemplate mockRabbitTemplate() {
        return mock(RabbitTemplate.class);
    }
}