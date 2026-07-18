# ---- Stage 1: Build the application ----
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom and source
COPY pom.xml .
COPY src ./src

# Build JAR
RUN mvn clean package -DskipTests

# ---- Stage 2: Run the application ----
FROM eclipse-temurin:21-jdk
WORKDIR /app

# Copy the JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose port 8080
EXPOSE 8080

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]
