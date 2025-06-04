FROM maven:3.9.9-amazoncorretto-21-alpine AS builder

WORKDIR /app

# Copy only the POM file first to cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create data directory
RUN mkdir -p src/main/resources/data

# Copy the JAR from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Copy CSV files
COPY src/main/resources/data/*.csv src/main/resources/data/

EXPOSE 8080

# Run the application with the prod profile
CMD ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]