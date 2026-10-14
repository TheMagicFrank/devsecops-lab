# Imágenes base fijadas por digest (DSL-9): la etiqueta es solo informativa;
# Docker usa el sha256, que identifica una imagen exacta e inmutable.
# Para actualizar: docker pull de la etiqueta, escanear con Trivy y cambiar el digest.

# ---------- Etapa 1: compilación ----------
# Imagen con Maven y JDK 21 solo para compilar; no llega a la imagen final
FROM maven:3.9-eclipse-temurin-21@sha256:99e61abcff91a9b1333463bd8451fb18495d6eba9250ac66a338b518f8278320 AS build
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
FROM eclipse-temurin:21-jre-alpine@sha256:51ab5e3302e7141ce665ca3ea85e8b5cd648eafbc3c0c90dd79d6537684e4555
WORKDIR /app

# Seguridad: usuario sin privilegios; la app nunca se ejecuta como root
RUN addgroup -S app && adduser -S app -G app

# El jar queda propiedad de root: el usuario app puede leerlo y ejecutarlo, pero no modificarlo
COPY --from=build /build/target/*.jar app.jar

USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
