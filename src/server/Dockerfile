FROM openjdk:11-jdk
LABEL authors="Dodo"

WORKDIR /app

COPY ./server/ ./server
COPY ./utility/ ./utility
COPY ./res /data


RUN javac -d out $(find . -name "*.java")

CMD ["java", "-cp", "out", "server.Server"]