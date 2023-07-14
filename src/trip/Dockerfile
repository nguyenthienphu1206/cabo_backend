FROM eclipse-temurin:17

LABEL mentainer="ntphu1206@gmail.com"

WORKDIR /app

COPY target/trip-0.0.1-SNAPSHOT.jar /app/trip.jar
COPY serviceAccountKey.json /app/trip/serviceAccountKey.json

ENV GOOGLE_APPLICATION_CREDENTIALS=/app/trip/serviceAccountKey.json

ENTRYPOINT ["java", "-jar", "trip.jar"]