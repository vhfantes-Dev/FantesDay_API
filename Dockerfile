# ---------- Build ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Baixa as dependencias primeiro (camada reaproveitada enquanto o pom.xml nao muda)
COPY pom.xml ./
RUN mvn -q -B dependency:go-offline

COPY src src
RUN mvn -q -B package -DskipTests

# ---------- Execucao ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

# Roda sem root
RUN useradd --system --uid 1001 fantesday
USER fantesday

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
# Variaveis obrigatorias: SUPABASE_DB_URL, SUPABASE_DB_USER, SUPABASE_DB_PASSWORD e JWT_SECRET (veja o README)
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
