# ── Stage 1: Build ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:25-jdk-alpine AS builder
WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN apk add --no-cache maven && \
    mvn -B -DskipTests package

# ── Stage 2: Runtime (Distroless) ───────────────────────────────────────────────
FROM gcr.io/distroless/java25-debian12:nonroot AS runtime

WORKDIR /app

COPY --from=builder /app/target/magenta-customers-*.jar app.jar

# Puerto del servicio
EXPOSE 8083

# Usuario no-root (distroless nonroot usa UID 65532)
USER 65532:65532

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
