FROM eclipse-temurin:17

LABEL mentainer="ntphu1206@gmail.com"

WORKDIR /app

COPY target/receiver-service-0.0.1-SNAPSHOT.jar /app/receiver-service.jar
COPY serviceAccountKey.json /app/receiver-service/serviceAccountKey.json

ENV GOOGLE_APPLICATION_CREDENTIALS=/app/receiver-service/serviceAccountKey.json

ENTRYPOINT ["java", "-jar", "receiver-service.jar"]