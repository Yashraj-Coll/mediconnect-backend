FROM maven:3.8.5-openjdk-17 AS build 
WORKDIR /app 
COPY . . 
RUN mvn clean package -DskipTests 
 
FROM openjdk:17-jdk-slim 
WORKDIR /app 
COPY --from=build /app/target/MediConnect-0.0.1-SNAPSHOT.war app.war 
EXPOSE 8080 
ENV SPRING_PROFILES_ACTIVE=prod 
ENTRYPOINT ["java", "-jar", "app.war"] 
