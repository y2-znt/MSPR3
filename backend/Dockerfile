# Étape 1 : Construire l'application
FROM maven:3.9.9-amazoncorretto-21-debian AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package

# Étape 2 : Créer une image légère pour l'exécution
FROM openjdk:21
WORKDIR /app
COPY --from=build /app/target/backend-0.0.1-SNAPSHOT.jar /app/backend.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "backend.jar"]
