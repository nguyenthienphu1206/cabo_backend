FROM eclipse-temurin:17

LABEL mentainer="ntphu1206@gmail.com"

WORKDIR /app

COPY target/taxi-status-service-0.0.1-SNAPSHOT.jar /app/taxi-status-service.jar
COPY serviceAccountKey.json /app/taxi-status-service/serviceAccountKey.json

ENV GOOGLE_APPLICATION_CREDENTIALS=/app/taxi-status-service/serviceAccountKey.json

ENTRYPOINT ["java", "-jar", "taxi-status-service.jar"]