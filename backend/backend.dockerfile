FROM maven:3.9.9-amazoncorretto-21-debian

WORKDIR /app

# Copy only the POM file first to cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
COPY .env .

EXPOSE 8080

CMD ["mvn", "spring-boot:run", "-Dspring-boot.run.profiles=dev"]
