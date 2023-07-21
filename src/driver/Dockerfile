FROM eclipse-temurin:17

LABEL mentainer="ntphu1206@gmail.com"

WORKDIR /app

COPY target/driver-0.0.1-SNAPSHOT.jar /app/driver.jar
COPY serviceAccountKey.json /app/driver/serviceAccountKey.json

ENV GOOGLE_APPLICATION_CREDENTIALS=/app/driver/serviceAccountKey.json

ENTRYPOINT ["java", "-jar", "driver.jar"]