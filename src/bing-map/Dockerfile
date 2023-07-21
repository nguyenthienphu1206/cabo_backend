FROM eclipse-temurin:17

LABEL mentainer="ntphu1206@gmail.com"

WORKDIR /app

COPY target/bing-map-0.0.1-SNAPSHOT.jar /app/bing-map.jar

ENTRYPOINT ["java", "-jar", "bing-map.jar"]