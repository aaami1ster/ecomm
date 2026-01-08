# Multi-stage Dockerfile for E-commerce Microservices
# Build argument: MODULE (product-service, order-service, user-service, api-gateway)

# Stage 1: Build
FROM maven:3.9-eclipse-temurin-25 AS build

WORKDIR /app

# Copy pom files first for better layer caching
COPY pom.xml .
COPY shared/pom.xml ./shared/pom.xml
COPY services/product-service/pom.xml ./services/product-service/
COPY services/order-service/pom.xml ./services/order-service/
COPY services/user-service/pom.xml ./services/user-service/
COPY services/api-gateway/pom.xml ./services/api-gateway/

# Copy source code
COPY shared ./shared
COPY services/product-service/src ./services/product-service/src
COPY services/order-service/src ./services/order-service/src
COPY services/user-service/src ./services/user-service/src
COPY services/api-gateway/src ./services/api-gateway/src

# Build argument for module selection
ARG MODULE

# Build the specified module (skip tests for faster builds)
RUN mvn clean package -pl services/${MODULE} -am -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

# Install curl for health checks
RUN apk add --no-cache curl

# Create non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy the built JAR from build stage
ARG MODULE
COPY --from=build /app/services/${MODULE}/target/*.jar app.jar

# Expose port (will be overridden by docker-compose)
EXPOSE 8080

# Health check (can be overridden by docker-compose)
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application with JVM optimizations
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]

