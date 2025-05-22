# backend/Dockerfile
# Etap 1: Budowanie (z narzędziami Mavena i JDK)
FROM maven:3.8.5-openjdk-17 AS builder
WORKDIR /app
COPY pom.xml .
# Pobierz zależności, aby przyspieszyć kolejne buildy, jeśli kod się nie zmienia
RUN mvn dependency:go-offline
COPY src ./src
# Sklompiluj i spakuj aplikację (w tym uruchom testy, jeśli nie są pomijane globalnie)
# Jeśli chcesz, aby testy były częścią tego etapu, usuń -DskipTests lub zmień na profil testowy
RUN mvn package -DskipTests

# Etap 2: Środowisko uruchomieniowe (lekki obraz z JRE)
FROM openjdk:17-jdk-slim AS runtime
WORKDIR /app
# Kopiuj tylko zbudowany artefakt .jar z etapu builder do finalnego obrazu
COPY --from=builder /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]docker-compose run --rm mafia-backend-tests