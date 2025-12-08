# Multi-stage build dla optymalizacji
FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /app

# Kopiowanie plików Maven
COPY pom.xml .
COPY src ./src

# Budowanie aplikacji
RUN mvn clean package -DskipTests

# Etap produkcyjny
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Kopiowanie zbudowanego JAR
COPY --from=build /app/target/*.jar app.jar

# Tworzenie użytkownika bez uprawnień root
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

USER appuser

# Expose portu auth-service
EXPOSE 8081

# Ustawienie zmiennych środowiskowych
ENV JAVA_OPTS="-Xms256m -Xmx512m"
ENV SPRING_PROFILES_ACTIVE=docker

# Health check - zwiększony start-period dla startu Spring Boot
HEALTHCHECK --interval=30s --timeout=10s --start-period=120s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8081/actuator/health || exit 1

# Uruchomienie aplikacji
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
