# Build em duas etapas para não exigir Maven instalado no computador do usuário.
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B test package

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && useradd --system --uid 10001 --create-home appuser
COPY --from=build /workspace/target/opsbridge-1.0.0.jar /app/opsbridge.jar
USER appuser
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/opsbridge.jar"]
