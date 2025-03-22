FROM maven:3.9.9-amazoncorretto-21-debian AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package

FROM openjdk:21

WORKDIR /app

COPY --from=build /app/target/backend-0.0.1-SNAPSHOT.jar /app/backend.jar
COPY .env /app/backend/.env

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "backend.jar"]
