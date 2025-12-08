package com.mafia.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Auth Service - Mikroserwis odpowiedzialny za autentykację i zarządzanie użytkownikami.
 * 
 * Funkcjonalności:
 * - Rejestracja użytkowników
 * - Logowanie i wylogowywanie
 * - Zarządzanie tokenami JWT
 * - Refresh tokenów
 * - Panel administracyjny (zarządzanie użytkownikami)
 * - Profil użytkownika
 */
@SpringBootApplication
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
