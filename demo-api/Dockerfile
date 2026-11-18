# Etapa 1: compilación
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY pom.xml .
COPY demo-api/pom.xml demo-api/pom.xml
COPY demo-api/src demo-api/src

RUN mvn -B -f demo-api/pom.xml clean package -DskipTests

# Etapa 2: ejecución
FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S app && adduser -S -G app app

WORKDIR /app

COPY --from=build --chown=app:app \
    /workspace/demo-api/target/demo-api-*.jar /app/app.jar

USER app

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "/app/app.jar"]