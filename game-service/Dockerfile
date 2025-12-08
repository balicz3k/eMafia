FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src/main ./src/main

RUN mvn -q compile process-resources
RUN mvn -q package -DskipTests
FROM builder AS tester_base
WORKDIR /app
COPY src/test ./src/test
CMD ["mvn", "test"]

FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]