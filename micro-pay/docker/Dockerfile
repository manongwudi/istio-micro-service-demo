FROM openjdk:8u191-jre-alpine3.9
ENTRYPOINT ["/usr/bin/java", "-jar", "/app.jar"]
ARG JAR_FILE
ADD ${JAR_FILE} /app.jar
EXPOSE 9092