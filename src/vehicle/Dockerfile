FROM eclipse-temurin:17

LABEL mentainer="ntphu1206@gmail.com"

WORKDIR /app

COPY target/vehicle-0.0.1-SNAPSHOT.jar /app/vehicle.jar
COPY serviceAccountKey.json /app/vehicle/serviceAccountKey.json

ENV GOOGLE_APPLICATION_CREDENTIALS=/app/vehicle/serviceAccountKey.json

ENTRYPOINT ["java", "-jar", "vehicle.jar"]