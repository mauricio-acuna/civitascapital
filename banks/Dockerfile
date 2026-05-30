# ── Build stage ────────────────────────────────────────────────────────────────
FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src
COPY .mvn .mvn
COPY mvnw .

RUN chmod +x mvnw && ./mvnw -q -DskipTests package

# ── Runtime stage (Distroless) ──────────────────────────────────────────────────
FROM gcr.io/distroless/java25-debian12:nonroot AS runtime

WORKDIR /app

COPY --from=build /app/target/magenta-banks-*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
