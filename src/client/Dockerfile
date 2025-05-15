FROM openjdk:11-jdk
LABEL authors="Dodo"

WORKDIR /app

# Copy server and utility source files into container
COPY ./client/ ./client
COPY ./utility/ ./utility

RUN javac -d out $(find . -name "*.java")

CMD ["java", "-cp", "out", "client.Client"]