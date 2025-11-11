# ---------- Fase 1: build ----------
FROM maven:3.9.7-eclipse-temurin-17 AS builder
WORKDIR /app

# Cache deps
COPY pom.xml .
RUN mvn -q -e -DskipTests dependency:go-offline

# Código
COPY src ./src

# Empaquetar
RUN mvn -q -DskipTests package

# ---------- Fase 2: runtime ----------
FROM eclipse-temurin:17-jre-alpine
WORKDIR /opt/bitsealer

# Usuario no-root
RUN addgroup -S app && adduser -S app -G app
USER app

# Jar
COPY --from=builder /app/target/*-SNAPSHOT.jar app.jar

# Flags JVM opcionales desde fuera
ENV JAVA_OPTS=""

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
