# syntax=docker/dockerfile:1
#
# Build context must be apps/java/ (the Maven project root).
# Configured in docker-compose.yaml:
#   build:
#     context: apps/java
#     dockerfile: services/storage-api/Dockerfile

# Stage 1 — compile and package
FROM maven:3.9-amazoncorretto-17 AS builder
WORKDIR /build
COPY . .
RUN --mount=type=cache,target=/root/.m2 \
    mvn -pl services/storage-api -am package -DskipTests -q

# Stage 2 — minimal runtime image
FROM amazoncorretto:17.0.5
VOLUME /tmp
COPY --from=builder /build/services/storage-api/target/*.jar app.jar
COPY --from=builder /build/services/storage-api/application.yml /application.yml
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "/app.jar"]
