# syntax=docker/dockerfile:1
#
# Build context must be apps/java/ (the Maven project root) so all library
# modules are on the classpath. Configured in docker-compose.camunda7.yaml:
#   build:
#     context: apps/java
#     dockerfile: services/case-engine-rest-api/Dockerfile

# Stage 1 — compile and package
FROM maven:3.9-amazoncorretto-17 AS builder
WORKDIR /build
COPY . .
# --mount=type=cache persists ~/.m2 across builds; requires BuildKit (default in Docker Desktop).
RUN --mount=type=cache,target=/root/.m2 \
    mvn -pl services/case-engine-rest-api -am package -DskipTests -q

# Stage 2 — minimal runtime image
FROM amazoncorretto:17.0.5
VOLUME /tmp
COPY --from=builder /build/services/case-engine-rest-api/target/*.jar app.jar
COPY --from=builder /build/services/case-engine-rest-api/application.yml /application.yml
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app.jar"]
