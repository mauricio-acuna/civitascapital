# ── Stage 1: Build ────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -q -DskipTests clean package 2>/dev/null || \
    mvn -q -DskipTests clean package

# ── Stage 2: Runtime (Distroless) ─────────────────────────────────────────────
FROM gcr.io/distroless/java21-debian12:nonroot AS runtime

LABEL org.opencontainers.image.source="https://github.com/magenta/magenta-platform"
LABEL org.opencontainers.image.description="Magenta Areas service"

WORKDIR /app
COPY --from=builder /build/target/areas-*.jar app.jar

EXPOSE 8081

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", \
            "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app/app.jar"]
