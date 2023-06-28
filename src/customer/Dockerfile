FROM eclipse-temurin:17

LABEL mentainer="ntphu1206@gmail.com"

WORKDIR /app

COPY target/customer-0.0.1-SNAPSHOT.jar /app/customer.jar
COPY serviceAccountKey.json /app/customer/serviceAccountKey.json

ENV GOOGLE_APPLICATION_CREDENTIALS=/app/customer/serviceAccountKey.json

ENTRYPOINT ["java", "-jar", "customer.jar"]