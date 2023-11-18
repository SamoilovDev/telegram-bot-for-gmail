FROM maven:3.8.4-openjdk-17

LABEL authors="vladimir.samoilov"

COPY . /telegram-bot-for-gmail

WORKDIR /telegram-bot-for-gmail

RUN mvn clean install

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "target/TelegramBotForGmail-0.0.1-SNAPSHOT.jar"]