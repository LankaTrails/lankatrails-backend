# PRODUCTION-GRADE DOCKERFILE FOR LANKATRAILS
# Build stage
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Make mvnw executable
RUN chmod +x mvnw

# Download dependencies
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src src

# Build the application
RUN ./mvnw clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Install curl for health checks
RUN apk add --no-cache curl

# Create non-root user
RUN addgroup -g 1001 -S spring && \
    adduser -S spring -u 1001

# Copy the JAR file from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Create logs and uploads directories with proper permissions BEFORE switching to non-root user
RUN mkdir -p /app/logs /app/uploads && \
    chown -R spring:spring /app && \
    chmod -R 755 /app/logs /app/uploads

# Expose port
EXPOSE 8080

# Health check (run as root for curl access)
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Switch to non-root user
USER spring

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
