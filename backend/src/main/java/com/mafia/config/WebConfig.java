package com.mafia.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  // Czyta ze zmiennej środowiskowej (z .env przez docker-compose)
  @Value("${app.cors.allowed-origins}")
  private String allowedOrigins;

  @Override
  public void addCorsMappings(@NonNull CorsRegistry registry) {
    // Rozdziel origins po przecinku (jeśli jest ich więcej)
    String[] origins = allowedOrigins.split(",");
    
    registry
        .addMapping("/**")
        .allowedOrigins(origins)
        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
        .allowedHeaders("*")
        .exposedHeaders("Authorization", "Content-Type")
        .allowCredentials(true)
        .maxAge(3600);
  }
}
