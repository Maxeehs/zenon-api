# ─────────────────────────────────────────────────────────────────────────────
# 1) Phase de build : Maven + Java 21
# ─────────────────────────────────────────────────────────────────────────────
FROM maven:3-eclipse-temurin-21-jammy AS builder

# Définition du répertoire de travail
WORKDIR /app

# Copie des métadonnées et cache des dépendances
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copie du code source et packaging
COPY src ./src
RUN mvn clean package -DskipTests -B

# ─────────────────────────────────────────────────────────────────────────────
# 2) Phase runtime : JRE léger Java 21
# ─────────────────────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-jammy

# Création du répertoire d'exécution
WORKDIR /app

# Copie du JAR construit
COPY --from=builder /app/target/*.jar app.jar

# Exposition du port par défaut de Spring Boot
EXPOSE 8080

# Réglages mémoire Java (à adapter selon vos besoins)
ENV JAVA_OPTS="-Xms512m -Xmx1024m"

# Healthcheck optionnel (requiert actuator sur /actuator/health)
HEALTHCHECK --interval=1m --timeout=10s \
  CMD wget --quiet --spider http://localhost:8080/actuator/health || exit 1

# Démarrage de l’application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]