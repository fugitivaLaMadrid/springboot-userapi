# Getting Started

## Overview
Lightweight **Spring Boot service for user management**.

This document explains the project layout, prerequisites, and how to run the app from scratch.

## Project Structure
```
src/
├─ main/
│  ├─ java/
│  │  └─ com/fugitivalamadrid/api/userapi/     # App packages
│  │     ├─ controller/                        # REST controllers
│  │     ├─ service/                           # Business logic
│  │     ├─ repository/                        # Database access
│  │     ├─ model/                             # Entities / DTOs
│  │     ├─ dto/                               # Request and Response DTOs
│  │     ├─ exception/                         # Custom exceptions
│  │     ├─ mapper/                            # Object mappers (MapStruct)
│  │     ├─ config/                            # Security & application config
│  │     └─ ratelimit/                         # Rate limiting logic
│  └─ resources/
│     ├─ application.yml                       # Prod config (PostgreSQL)
│     ├─ application-test.yml                  # Test config (H2)
│     └─ logback-spring.xml                    # Logging configuration
└─ test/
   └─ java/
      └─ com/fugitivalamadrid/api/userapi/    # Test classes
         ├─ controller/                         # Integration tests
         ├─ service/                            # Unit tests
         └─ ratelimit/                          # Rate limiter tests
```

### Important Files
- `application.yml` – Production configuration using PostgreSQL
- `application-test.yml` – H2 in-memory configuration for tests
- `.env` – Environment variables (ignored in Git; contains DB credentials)
- `Dockerfile` – Container image for the Spring Boot application
- `docker-compose.yml` – Brings up PostgreSQL, Keycloak, the app, Adminer and SonarQube
- `UserRepository.java` – Spring Data JPA repository for DB access

## Prerequisites
- Java 21
- Maven 3.9+
- Docker & Docker Compose
- Git

---

# Quick Start (First-Time Setup)

Follow these steps to run the project from scratch:

## Step 1: Clone and Build
```bash
git clone <repository-url>
cd userapi
mvn clean package
```

## Step 2: Configure Keycloak (Required for Authentication)
This application uses JWT authentication via Keycloak. Keycloak must be configured before the application can start.

### 2.1 Start Keycloak
```bash
docker compose up -d keycloak
```

### 2.2 Configure Keycloak Realm
1. Access Keycloak Admin Console at http://localhost:8180/admin/master/console/#/master/realms
2. Login with credentials:
   - **Username:** `admin`
   - **Password:** `admin123` (from `.env` file)
3. Run the setup script to create the realm and client (from project root):
   ```bash
   powershell -ExecutionPolicy Bypass -File .scripts\setup-keycloak.ps1
   ```
4. Refresh the admin console - you will now see `userapi-realm` listed
5. **Activate the realm:**
   - Click on "Manage Realms" in the top-left
   - Click on `userapi-realm` to activate it (it changes from blue to grey)
6. **Verify the client:**
   - Navigate to http://localhost:8180/admin/master/console/#/userapi-realm/clients
   - Confirm `userapi-client` is displayed
7. The setup script automatically saves the client secret to `.env` as `KEYCLOAK_CLIENT_SECRET`

## Step 3: Start the Application
Start PostgreSQL and the application:
```bash
docker compose up -d postgres userapi
```

The application will now start successfully. Access it at:
- **Application:** http://localhost:8080
- **Adminer (Database UI):** http://localhost:8081
- **SonarQube (Code Quality):** http://localhost:9000

## Step 4: Test the Application
Use the Postman collection to test endpoints:
1. Get access token: http://localhost:8180/realms/userapi-realm/protocol/openid-connect/token
2. Access protected endpoints: http://localhost:8080/users

---

# Development Workflow

## Running the Application

### Option A: Via Docker Compose (Recommended)
Start all services (Keycloak must already be configured):
```bash
docker compose up --build
```

Stop all services:
```bash
docker compose down
```

### Option B: Via Command Line or IntelliJ
Start infrastructure services:
```bash
docker compose start postgres keycloak
```

Stop the application container if running:
```bash
docker compose stop userapi
```

Build and run locally:
```bash
mvn clean install
mvnw spring-boot:run
```

## Running Tests
Tests use H2 in-memory database and don't require Keycloak:
```bash
mvn test
```

## Code Quality Analysis with SonarQube

### First-Time SonarQube Setup
1. Start SonarQube:
   ```bash
   docker compose up -d sonarqube
   ```
2. Access SonarQube at http://localhost:9000
3. Update the default password
4. Generate a token:
   - Go to http://localhost:9000/account/security
   - Add:
     - **Name:** user-api-maven
     - **Type:** User token
     - **Expires:** No expiration
5. Copy the token to your `.env` and `.env.local` files

### Run Analysis
```bash
.scripts\sonar.bat
```

---

# Services Reference

## Docker Compose Services
- **PostgreSQL** - Service name: `userapi_postgres`
- **Spring Boot App** - Service name: `userapi_app`, URL: `http://localhost:8080`
- **Adminer** (Database UI) - Service name: `userapi_adminer`, URL: `http://localhost:8081`
- **SonarQube** (Code Quality) - Service name: `userapi_sonarqube`, URL: `http://localhost:9000`
- **Keycloak** (Authentication) - Service name: `userapi_keycloak`, URL: `http://localhost:8180`

## Database Connection
**JDBC:**
- URL: `jdbc:postgresql://localhost:5432/userdb`
- Username: defined in `.env` (`POSTGRES_USER`)
- Password: defined in `.env` (`POSTGRES_PASSWORD`)

**Adminer UI:** http://localhost:8081

## Logging & Observability
Spring Boot Actuator endpoints:
- Health: `/actuator/health`
- Metrics: `/actuator/metrics`

View container logs:
```bash
docker logs -f userapi_app
```

---

# Postman Collection

The Postman collection `user-api (JWT/OAuth2)` includes:
1. **Auth** - Get access tokens
2. **User Endpoints** - CRUD operations
3. **Error Handling** - Test error scenarios
4. **Actuator Endpoints** - Health and metrics
5. **Test Pagination** - Pagination testing

### OAuth2 Grant Types
Both grant types use the same token endpoint: http://localhost:8180/realms/userapi-realm/protocol/openid-connect/token

- **Password Grant:** Uses `grant_type=password`, requires username, password, client_id, client_secret. Authenticates as a user.
- **Client Credentials:** Uses `grant_type=client_credentials`, requires client_id, client_secret. Authenticates as the application/client (no user context).

---

# CI/CD
Example pipeline stages:
- build
- test
- package
- docker_build
- Artifacts: `.jar` in `target/`
- Can be extended to publish to GitLab Container Registry, Docker Hub, or Nexus

---

# AI Workflow
This project includes an `.ai` folder that defines AI-assisted development workflows.

When generating commit messages, follow `.ai/skills/commit.md`:
- `feat(model): add User entity with JPA annotations`
- `feat(api): implement GET /users endpoint`

---

# Notes and Tips
- Keep **secrets out of Git**: use `.env` or your CI secret store
- Use the **test profile with H2** for unit and integration tests
- If ports conflict, update the bindings in `docker-compose.yml`
- The **Docker + PostgreSQL + Keycloak + Adminer + Actuator** setup resembles a real enterprise microservice environment

