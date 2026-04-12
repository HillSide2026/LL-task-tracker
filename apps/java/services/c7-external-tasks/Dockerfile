# syntax=docker/dockerfile:1
#
# Build context must be apps/java/ (the Maven project root).
# Configured in docker-compose.camunda7.yaml:
#   build:
#     context: apps/java
#     dockerfile: services/c7-external-tasks/Dockerfile

# Stage 1 — compile and package
FROM maven:3.9-amazoncorretto-17 AS builder
WORKDIR /build
COPY . .
RUN --mount=type=cache,target=/root/.m2 \
    mvn -pl services/c7-external-tasks -am package -DskipTests -q

# Stage 2 — minimal runtime image
FROM amazoncorretto:17.0.5
VOLUME /tmp
COPY --from=builder /build/services/c7-external-tasks/target/*.jar app.jar
COPY --from=builder /build/services/c7-external-tasks/application.yml /application.yml
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "/app.jar"]
