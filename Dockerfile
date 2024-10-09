FROM maven:3.8.4-openjdk-17 AS build
LABEL authors="vladimir.samoilov"
WORKDIR /app

COPY pom.xml .

RUN mvn dependency:go-offline

COPY src ./src

RUN mvn clean package -DskipTests

FROM openjdk:17-jdk-slim
WORKDIR /app

COPY --from=build /app/target/telegram-bot-for-gmail-1.0.0.jar app.jar

EXPOSE 8080

ENV GMAIL_TOKENS_PATH=/tokens
ENV GMAIL_CREDENTIALS_PATH=/credentials/google_credentials.json
ENV TELEGRAM_BOT_NAME=GmailCheckerBot

ENTRYPOINT ["java", "-jar", "app.jar"]