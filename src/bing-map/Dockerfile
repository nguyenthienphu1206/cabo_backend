FROM eclipse-temurin:17

LABEL mentainer="ntphu1206@gmail.com"

WORKDIR /app

COPY target/bing-map-0.0.1-SNAPSHOT.jar /app/bing-map.jar

ENV bing.map.apiKey=Aj6QhUcXi3KwZdDylmum4xiiypL9l2s2kqsnhP087cfTuvR2mKY7wuTBDgTfcaKq

ENTRYPOINT ["java", "-jar", "bing-map.jar"]