# ---------- Etapa 1: BUILD ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Cacheamos dependencias
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# Ahora el código y el empaquetado
COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---------- Etapa 2: RUNTIME ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

# curl para el healthcheck de Composen, no viene en la imagen base de Java (es lo que hay)
RUN apt-get update && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

COPY --from=build /app/target/*.jar app.jar
EXPOSE 8000
ENTRYPOINT ["java", "-jar", "app.jar"]