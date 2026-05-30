FROM gradle:8.14.3-jdk21 AS build

WORKDIR /app

COPY build.gradle settings.gradle gradle.properties ./
COPY src ./src

RUN gradle --no-daemon bootJar

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]