FROM eclipse-temurin:17

LABEL mentainer="ntphu1206@gmail.com"

WORKDIR /app

COPY target/call-center-0.0.1-SNAPSHOT.jar /app/call-center.jar
COPY serviceAccountKey.json /app/call-center/serviceAccountKey.json

ENV GOOGLE_APPLICATION_CREDENTIALS=/app/call-center/serviceAccountKey.json

ENTRYPOINT ["java", "-jar", "call-center.jar"]