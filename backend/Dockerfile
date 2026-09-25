FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy pre-built production Spring Boot executable JAR
COPY target/digital-library-backend-1.0.0-SNAPSHOT.jar app.jar

# Install curl for healthchecks
RUN apk add --no-cache curl tzdata && \
    cp /usr/share/zoneinfo/Asia/Kolkata /etc/localtime && \
    echo "Asia/Kolkata" > /etc/timezone

EXPOSE 8080

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
