FROM gradle:8.5-jdk17 as builder

WORKDIR /app

COPY gradlew .
COPY build.gradle .
COPY settings.gradle .
COPY gradle ./gradle

COPY src ./src

RUN ./gradlew clean build -x test

FROM --platform=linux/amd64 eclipse-temurin:17-jre-alpine

WORKDIR /app

RUN addgroup -S livechat && adduser -S chatuser
USER chatuser

COPY --from=builder /app/build/libs/*.jar app.jar

LABEL authors="Chae"

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
