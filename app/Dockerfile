# ---------- Etapa 1: compilación ----------
# Imagen con Maven y JDK 21 solo para compilar; no llega a la imagen final
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# Primero solo el pom.xml: así Docker cachea las dependencias
# y no las vuelve a descargar si solo cambia el código
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
# Los tests se ejecutan en el pipeline de CI, no al construir la imagen
RUN mvn -B package -DskipTests

# ---------- Etapa 2: ejecución ----------
# Solo el JRE (sin compilador ni Maven): imagen más pequeña y menos superficie de ataque
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Seguridad: usuario sin privilegios; la app nunca se ejecuta como root
RUN addgroup -S app && adduser -S app -G app

COPY --from=build /build/target/*.jar app.jar
RUN chown app:app app.jar

USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
