FROM eclipse-temurin:17

LABEL mentainer="ntphu1206@gmail.com"

WORKDIR /app

COPY target/gps-0.0.1-SNAPSHOT.jar /app/gps.jar
COPY serviceAccountKey.json /app/gps/serviceAccountKey.json

ENV GOOGLE_APPLICATION_CREDENTIALS=/app/gps/serviceAccountKey.json

ENTRYPOINT ["java", "-jar", "gps.jar"]