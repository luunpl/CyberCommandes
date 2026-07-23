# syntax=docker/dockerfile:1

# ---- Stage 1: build ----
# Multi-module Maven build. Build context is the repository root so the
# parent POM (root pom.xml) is available to the reactor.
FROM maven:3-eclipse-temurin-26 AS build
WORKDIR /workspace

# Copy POMs first to leverage Docker layer caching for dependencies.
COPY pom.xml ./
COPY backend/pom.xml backend/pom.xml
COPY backend/rest-api/pom.xml backend/rest-api/pom.xml
COPY backend/server/pom.xml backend/server/pom.xml
RUN mvn -B -f pom.xml -pl :server -am dependency:go-offline

# Copy sources and build the runnable jar.
COPY backend/ backend/
RUN mvn -B -f pom.xml -pl :server -am clean package -DskipTests
RUN cp backend/server/target/server-*.jar /workspace/app.jar

# ---- Stage 2: runtime ----
FROM eclipse-temurin:25-jre AS runtime
WORKDIR /app

# curl is needed by the container healthcheck; then drop to an unprivileged user.
RUN apt-get update \
    && apt-get install --no-install-recommends -y curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system spring && useradd --system --gid spring spring
USER spring:spring

COPY --from=build /workspace/app.jar app.jar

EXPOSE 8080
ENV JAVA_OPTS=""

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD ["sh", "-c", "curl -fsS http://localhost:8080/actuator/health || exit 1"]

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
