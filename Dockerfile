# ---- Fase 1: compilar el proyecto ----------------------------------
FROM maven:3.9.7-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q package -DskipTests   # genera target/bitsealer-*.jar

# ---- Fase 2: imagen final ligera -----------------------------------
FROM eclipse-temurin:17-jre-alpine
WORKDIR /opt/bitsealer
COPY --from=builder /app/target/bitsealer-*.jar app.jar
ENV SPRING_PROFILES_ACTIVE=docker
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
