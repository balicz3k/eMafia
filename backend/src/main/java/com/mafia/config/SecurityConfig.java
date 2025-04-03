package com.mafia.config;

import com.mafia.components.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration @EnableWebSecurity @EnableMethodSecurity public class SecurityConfig
{

    private final JwtTokenProvider jwtTokenProvider;

    public SecurityConfig(JwtTokenProvider jwtTokenProvider) { this.jwtTokenProvider = jwtTokenProvider; }

    @Bean public SecurityFilterChain filterChain(HttpSecurity http) throws Exception
    {
        http.csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth
                                   -> auth.requestMatchers("/api/auth/**")
                                          .permitAll() // Publiczne endpointy
                                          .requestMatchers("/api/test")
                                          .permitAll() // Dodaj publiczny dostęp do /api/test
                                          .requestMatchers("/api/users/**")
                                          .authenticated() // Endpointy wymagające uwierzytelnienia
                                          .anyRequest()
                                          .authenticated());

        return http.build();
    }

    @Bean public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
}