# Etapa 1: compilación
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY pom.xml .
COPY gateway/pom.xml gateway/pom.xml
COPY gateway/src gateway/src

RUN mvn -B -f gateway/pom.xml clean package -DskipTests

# Etapa 2: ejecución
FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S app && adduser -S -G app app

WORKDIR /app

COPY --from=build --chown=app:app \
    /workspace/gateway/target/gateway-*.jar /app/app.jar

USER app

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]