FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Copiamos primero los archivos de gradle para cache de dependencias
COPY build.gradle settings.gradle main.gradle gradle.properties ./
COPY gradlew ./
COPY gradle ./gradle

# Copiamos todo el código del proyecto
COPY . .

# Compilamos y empaquetamos el proyecto (sin tests para acelerar)
RUN ./gradlew clean build -x validateStructure

# --- Etapa de ejecución ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Crear usuario no root
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copiamos el jar correcto desde app-service
COPY --from=builder /app/applications/app-service/build/libs/*.jar app.jar

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=70 -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8081
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
