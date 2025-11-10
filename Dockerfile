FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/cachecraft-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080 8081

ENTRYPOINT ["java", "-jar", "app.jar"]