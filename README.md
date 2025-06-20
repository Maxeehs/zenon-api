# Zenon API

Zenon API est une application REST construite avec **Java 21** et **Spring Boot 3.5**, conçue pour gérer des entités métiers avec sécurité JWT, documentation Swagger et gestion de base de données via Liquibase.

## 🚀 Fonctionnalités principales

- API RESTful avec Spring Web
- Authentification sécurisée via **JWT**
- **Spring Security** pour la protection des endpoints
- **Spring Data JPA** pour la persistance
- **Liquibase** pour les migrations de schéma
- **Swagger / OpenAPI** pour la documentation automatique
- Prête à l’emploi en **local ou Docker**

---

## 📦 Prérequis

- Java 21 (Eclipse Temurin ou autre JDK compatible)
- Maven 3.8+ (ou wrapper `./mvnw`)
- MariaDB (ou autre SGBD compatible avec Spring Data JPA)

---

## ⚙️ Configuration

Le profil `dev` utilise la configuration suivante (définie dans `src/main/resources/application-dev.yml`) :

```yaml
spring:
  datasource:
    url: jdbc:mariadb://localhost:3315/zenon
    username: zenon
    password: zenonPass
  security:
    user:
      name: admin
      password: admin
  jwt:
    secret: <clé secrète>
    expiration-ms: 3600000 # 1h
