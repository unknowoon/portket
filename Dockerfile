FROM openjdk:17-jdk-slim

COPY ./build/libs/portket-0.0.1-SNAPSHOT.jar /app/portket.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/portket.jar"]
