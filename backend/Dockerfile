# Etap 1: Budowanie głównego artefaktu aplikacji (builder)
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

# Kopiuj pom.xml i pobierz zależności
COPY pom.xml .
RUN mvn dependency:go-offline

# Kopiuj TYLKO źródła głównej aplikacji i zasoby
COPY src/main ./src/main

# Kompiluj główne źródła i przetwarzaj zasoby
# Na tym etapie src/test nie istnieje w obrazie
RUN mvn compile process-resources

# Pakuj główną aplikację.
# Ponieważ src/test nie zostało skopiowane, testy nie będą kompilowane,
# a spring-boot:repackage nie będzie "widział" klas testowych.
# Flaga -DskipTests jest tu dla pewności, aby pominąć fazy testowe,
# gdyby jakimś cudem zostały aktywowane.
RUN mvn package -DskipTests

# Etap 1.5: Baza dla testera (tester_base)
# Ten etap dziedziczy z 'builder', więc ma już skompilowane klasy główne
# i spakowany artefakt app.jar (choć go nie użyjemy bezpośrednio do testów).
# Dodajemy teraz kod testowy.
FROM builder AS tester_base
WORKDIR /app
# pom.xml i src/main są już obecne z etapu 'builder'
# Kopiuj źródła testowe
COPY src/test ./src/test

# Domyślna komenda dla tego etapu, jeśli będzie uruchamiany bezpośrednio.
# Usługa w docker-compose.yml prawdopodobnie nadpisze tę komendę
# swoimi parametrami dla 'mvn test'.
CMD ["mvn", "test"]

# Etap 2: Środowisko uruchomieniowe (runtime)
FROM eclipse-temurin:17-jre AS runtime
WORKDIR /app
# Kopiuj tylko zbudowany artefakt .jar z etapu 'builder' (który nie "widział" src/test podczas pakowania)
COPY --from=builder /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]