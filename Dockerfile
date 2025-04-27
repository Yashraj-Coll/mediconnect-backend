# 1st Stage: Build stage
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# 2nd Stage: Production stage
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/MediConnect.war app.war
EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=prod
ENTRYPOINT ["java", "-jar", "app.war"]
