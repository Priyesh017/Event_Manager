# Dockerfile for EventHub
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the built JAR (build first: ./mvnw -DskipTests package)
COPY target/event-management-system-*.jar app.jar

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
