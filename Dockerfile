# ─── Stage 1: Build Stage ──────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build

# Copy maven wrapper & pom.xml first (optimizes layer caching)
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy source code and compile package
COPY src ./src
RUN ./mvnw clean package -DskipTests

# ─── Stage 2: Production Runtime Stage ─────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy compiled executable JAR from builder stage
COPY --from=builder /build/target/event-management-system-*.jar app.jar

# Expose HTTP port
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
