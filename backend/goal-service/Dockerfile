FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/goal-service-1.0.0.jar app.jar

EXPOSE 8082 9092

ENTRYPOINT ["java", "-jar", "app.jar"]