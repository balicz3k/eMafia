package com.mafia.integration;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;

/**
 * Bazowa klasa konfiguracyjna dla testów integracyjnych.
 * 
 * Używa Testcontainers do uruchomienia prawdziwych kontenerów Docker:
 * - PostgreSQL dla bazy danych
 * - RabbitMQ dla brokera wiadomości
 * 
 * Kontenery są współdzielone między wszystkimi testami (singleton pattern),
 * co przyspiesza wykonywanie testów i zapewnia ich stabilność.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("integration-test")
public abstract class BaseIntegrationTest {

    // Singleton containers - started once and reused across all tests
    static final PostgreSQLContainer<?> postgres;
    static final RabbitMQContainer rabbitmq;
    
    static {
        postgres = new PostgreSQLContainer<>("postgres:16-alpine")
                .withDatabaseName("testdb")
                .withUsername("test")
                .withPassword("test")
                .withReuse(true);
        postgres.start();
        
        rabbitmq = new RabbitMQContainer("rabbitmq:3.13-management-alpine")
                .withReuse(true);
        rabbitmq.start();
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // PostgreSQL configuration
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        
        // RabbitMQ configuration
        registry.add("spring.rabbitmq.host", rabbitmq::getHost);
        registry.add("spring.rabbitmq.port", rabbitmq::getAmqpPort);
        registry.add("spring.rabbitmq.username", rabbitmq::getAdminUsername);
        registry.add("spring.rabbitmq.password", rabbitmq::getAdminPassword);

        // Force Hibernate to create tables
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    /**
     * Czyści dane w tabelach przed każdym testem.
     * Wyłącza i włącza z powrotem ograniczenia klucza obcego podczas czyszczenia.
     */
    @BeforeEach
    void cleanDatabase() {
        // Wyłącz sprawdzanie kluczy obcych
        jdbcTemplate.execute("SET session_replication_role = replica;");
        
        // Pobierz listę wszystkich tabel użytkownika i wyczyść je
        jdbcTemplate.execute("DO $$ DECLARE\n" +
                "    r RECORD;\n" +
                "BEGIN\n" +
                "    FOR r IN (SELECT tablename FROM pg_tables WHERE schemaname = 'public') LOOP\n" +
                "        EXECUTE 'TRUNCATE TABLE ' || quote_ident(r.tablename) || ' CASCADE';\n" +
                "    END LOOP;\n" +
                "END $$;");
        
        // Włącz z powrotem sprawdzanie kluczy obcych
        jdbcTemplate.execute("SET session_replication_role = DEFAULT;");
    }
}
