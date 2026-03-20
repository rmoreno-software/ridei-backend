# ETAPA 1: Construcción del proyecto Multi-Módulo
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# 1. Copiamos el pom.xml padre
COPY pom.xml .

# 2. Copiamos todos los módulos
COPY ridei-api ./ridei-api
COPY ridei-identity ./ridei-identity
COPY ridei-shared ./ridei-shared

# 3. Compilamos todo el proyecto desde la raíz
RUN mvn clean package -DskipTests

# ETAPA 2: Ejecución
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# 4. Copiamos el .jar compilado desde el módulo principal (ridei-api)
COPY --from=build /app/ridei-api/target/*.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]