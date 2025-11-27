# syntax=docker/dockerfile:1.7

ARG BOOT_JAR=backend/bootstrap/build/libs/bootstrap-0.0.1-SNAPSHOT.jar

FROM gradle:8.10.2-jdk17-alpine AS builder
WORKDIR /app

COPY . .
RUN chmod +x gradlew
RUN ./gradlew :bootstrap:bootJar -x test

FROM eclipse-temurin:17-jre-jammy AS runner
ARG BOOT_JAR
WORKDIR /app

COPY --from=builder /app/${BOOT_JAR} /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
