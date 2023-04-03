FROM maven:latest as build

WORKDIR /src/app

COPY pom.xml .

RUN mvn dependency:resolve

COPY ./src src/

RUN mvn package


FROM adoptopenjdk/openjdk11:alpine-jre

EXPOSE 8080

WORKDIR /app

COPY --from=build /src/app/target/*.jar app.jar

CMD java -Dspring.profiles.active=prod -jar app.jar
