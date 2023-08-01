FROM eclipse-temurin:17

LABEL mentainer="ntphu1206@gmail.com"

WORKDIR /app

COPY target/booking-0.0.1-SNAPSHOT.jar /app/booking.jar
COPY serviceAccountKey.json /app/booking/serviceAccountKey.json

ENV GOOGLE_APPLICATION_CREDENTIALS=/app/booking/serviceAccountKey.json

ENTRYPOINT ["java", "-jar", "booking.jar"]