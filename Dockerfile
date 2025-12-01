# ============================================
# ETAPA 1: BUILD (Construcción del proyecto)
# ============================================
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copiar primero solo pom.xml para aprovechar cache de Docker
COPY pom.xml .

# Descargar dependencias (se cachea si pom.xml no cambia)
RUN mvn dependency:go-offline -B

# Copiar el código fuente
COPY src ./src

# Compilar y empaquetar la aplicación (sin ejecutar tests)
RUN mvn clean package -DskipTests

# ============================================
# ETAPA 2: RUNTIME (Ejecución de la aplicación)
# ============================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Crear usuario no-root para ejecutar la aplicación (seguridad)
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copiar el JAR desde la etapa de build
COPY --from=build /app/target/fe.jar app.jar

# Exponer el puerto de la aplicación
EXPOSE 8080

# Variables de entorno para configuración JVM (opcional)
ENV JAVA_OPTS=""

# Punto de entrada para ejecutar la aplicación
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]