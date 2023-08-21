FROM eclipse-temurin:17

LABEL mentainer="ntphu1206@gmail.com"

WORKDIR /app

COPY target/bing-map-0.0.1-SNAPSHOT.jar /app/bing-map.jar
COPY serviceAccountKey.json /app/bing-map/serviceAccountKey.json

ENV GOOGLE_APPLICATION_CREDENTIALS=/app/bing-map/serviceAccountKey.json

ENTRYPOINT ["java", "-jar", "bing-map.jar"]