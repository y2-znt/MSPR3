FROM maven:3.9.9-amazoncorretto-21-alpine

WORKDIR /app

# Copy only the POM file first to cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

COPY . .

EXPOSE 8080

CMD ["mvn", "spring-boot:run", "-Dspring-boot.run.profiles=dev"]
