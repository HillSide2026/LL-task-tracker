# syntax=docker/dockerfile:1
#
# Build context must be apps/java/ (the Maven project root).
# Configured in docker-compose.demo-data-loader.camunda7.yaml:
#   build:
#     context: apps/java
#     dockerfile: services/demo-data-loader/Dockerfile

# Stage 1 — compile and package
FROM maven:3.9-amazoncorretto-17 AS builder
WORKDIR /build
COPY . .
RUN --mount=type=cache,target=/root/.m2 \
    mvn -pl services/demo-data-loader -am package -DskipTests -q

# Stage 2 — runtime image
FROM amazoncorretto:17.0.5
ENV IMPORT_DIR_PATH /opt/data
COPY --from=builder /build/services/demo-data-loader/target/*.jar app.jar
COPY --from=builder /build/services/demo-data-loader/application.yml /application.yml
COPY --from=builder /build/services/demo-data-loader/data /opt/data
ENTRYPOINT ["java", "-jar", "/app.jar"]
