# Etapa 1: Build de la aplicación con Maven y Java 17
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Copiar pom.xml y descargar dependencias (para aprovechar caché de Docker)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar código fuente y compilar el JAR omitiendo tests
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Imagen liviana de ejecución (JRE 17)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copiar el archivo JAR generado desde la etapa de compilación
COPY --from=build /app/target/*.jar app.jar

# Exponer el puerto
EXPOSE 8080

# Iniciar la aplicación de Spring Boot
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-8080} -jar app.jar"]
