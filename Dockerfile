# Dockerfile for Spring application

FROM eclipse-temurin:21-jdk-alpine

ENV SPRING_DATA_MONGDB_URL="mongodb+srv://13012018yaprinceva:olga1309@cluster0.jn9x6p9.mongodb.net/java48db?retryWrites=true&w=majority" \
    TELEGRAM.BOT.USERNAME="MewLogBot" \
    TELEGRAM.BOT.TOKEN="6684507700:AAGsoTAAT6d_l0qFp3xOfC0Qtv_qrjaDBjQ" \
    TELEGRAM.BOT.LINK="https://t.me/MewLogBot"

WORKDIR /app

COPY target/mew-log-bot-0.0.1-SNAPSHOT.jar ./mew-log-bot.jar

EXPOSE 8080

CMD ["java", "-jar", "/app/MewLogBot.jar"]
