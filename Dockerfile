# Stage 1: Build JAR with Maven and Java 21
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Copy Maven wrapper configuration and build files
COPY .mvn .mvn
COPY mvnw pom.xml ./

# Normalize line endings (CRLF to LF) for Linux compatibility and grant execution permissions
RUN tr -d '\r' < mvnw > mvnw.tmp && mv mvnw.tmp mvnw && chmod +x mvnw

# Pre-fetch dependencies
RUN ./mvnw dependency:go-offline -B

# Copy source code and build production package
COPY src ./src
RUN ./mvnw package -DskipTests

# Stage 2: Production runtime image with Java 21 JRE
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]
