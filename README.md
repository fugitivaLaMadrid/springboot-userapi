# Getting Started

## Overview
Lightweight **Spring Boot service for user management**.

This document explains the project layout, prerequisites, common commands, and how to run the app locally using Docker Compose.

## Project structure
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
- `docker-compose.yml` – Brings up PostgreSQL, the app, Adminer and SonarQube
- `UserRepository.java` – Spring Data JPA repository for DB access

## Prerequisites
- Java 17+
- Maven 3.9+
- Docker & Docker Compose
- Git

## Common commands

**Build and run locally:**
```bash
mvn clean package
docker compose up --build
```

**Stop containers:**
```bash
docker compose down
```

**Run tests (local, without Docker):**
```bash
mvn test
```
This runs tests using the H2 in-memory database via the `test` Spring profile, which is activated automatically by `@ActiveProfiles("test")` in the test classes.

## Local Development Workflow

Complete workflow for local development before running the application:

### 1. Run Tests
Ensure all tests pass before building:
```bash
mvn test
```

### 2. Run Code Quality Analysis & Build

> **⚠️ Warning:** SonarQube Docker container must be running! Start it first if not already running:
> ```bash
> # First time only (or after docker compose down)
> docker compose up -d sonarqube
> ```

Run SonarQube analysis and build the application:
```bash
.scripts\sonar.bat
```

> **Note:** This runs `mvn clean verify` which compiles, tests, and packages the application.

### 3. Run Locally
Choose one of the following options:

**Option A: Via Docker Compose (with all infrastructure):**
```bash
docker compose up --build
```

**Option B: Via Command Line (infrastructure must be running):**
```bash
mvnw spring-boot:run
```

## Services (Docker Compose)
1. **PostgreSQL**
    - Service name: `userapi_postgres`
2. **Spring Boot application**
    - Service name: `userapi_app`
    - URL: `http://localhost:8080`
3. **Adminer** (Database UI)
    - Service name: `userapi_adminer`
    - URL: `http://localhost:8081`
4. **SonarQube** (Code quality)
    - Service name: `userapi_sonarqube`
    - URL: `http://localhost:9000`

## Database connection

**JDBC:**
- URL: `jdbc:postgresql://localhost:5432/userdb`
- Username: defined in `.env` (`POSTGRES_USER`)
- Password: defined in `.env` (`POSTGRES_PASSWORD`)

**Adminer UI:**
- URL: `http://localhost:8081`

You can inspect and manage the user table there for CRUD testing.

## Logging & observability
Spring Boot Actuator endpoints:
- Actuator: `/actuator/*`
- Health: `/actuator/health`
- Metrics: `/actuator/metrics`

**View container logs:**
```bash
docker logs -f userapi_app
```

## CI/CD
Example pipeline stages:
- build
- test
- package
- docker_build
- Artifacts:
    - `.jar` in `target/`
- The pipeline can be extended to publish container images to:
    - GitLab Container Registry
    - Docker Hub
    - Nexus

## AI Workflow
This project includes an `.ai` folder that defines **AI-assisted development workflows.**

When generating commit messages, you can instruct AI tools to follow:
`.ai/skills/commit.md`

Example commit messages:
- `feat(model): add User entity with JPA annotations`
- `feat(api): implement GET /users endpoint`

## SonarQube Analysis

Start SonarQube before running the analysis:
```bash
docker compose up -d sonarqube
```

Then run the analysis (Windows):
```bash
.scripts/sonar.bat
```

> **Note:** The script reads `SONAR_TOKEN` from `.env.local` and runs the analysis using the `localProfile` Maven profile.

## Detailed Setup Instructions

### 1. Start Infrastructure Services

First, start all required services with Docker Compose:

```bash
docker compose up -d
```

### 2. Configure Keycloak

Access Keycloak Admin Console:
```
http://localhost:8180/realms/master/protocol/openid-connect/auth?client_id=security-admin-console&redirect_uri=http%3A%2F%2Flocalhost%3A8180%2Fadmin%2Fmaster%2Fconsole%2F%23%2Fuserapi-realm%2Frealms&state=15425c2e-2801-4a5f-bab2-92d979974f10&response_mode=query&response_type=code&scope=openid&nonce=4cafbdda-9105-4fb8-ab33-0bb09114ad72&code_challenge=XBM0NqY6G1TO4hoG-fwxDfFgbAC2rG_08e-ZQBL8Wok&code_challenge_method=S256
```

Run the setup script to create the realm:
```powershell
powershell -ExecutionPolicy Bypass -File .scripts\setup-keycloak.ps1
```

**Activate the realm:**
1. Go to **Manage Realm** → Search for `userapi-realm`
2. Click on it to activate (blue turns to grey when activated)

### 3. Configure SonarQube

Access SonarQube:
```
http://localhost:9000/sessions/new?return_to=%2F
```

**Initial setup:**
1. Update the default password
2. Generate a token:
    - Go to http://localhost:9000/account/security
    - Add:
        - **Name:** user-api-maven
        - **Type:** User token
        - **Expires:** No expiration
3. Copy the token to your `.env` and `.env.local` files

### 4. Configure Database

Access Adminer:
```
http://localhost:8081/?pgsql=postgres&username=user&db=userdb&ns=public&select=users
```

**Credentials:** Check `.env.local` for `POSTGRES_USER` and `POSTGRES_PASSWORD`

**Create database:**
- Create a new database named `userdb`

### 5. Running the Application

**Option 1: Via Docker**
The application is already running when you start Docker Compose (if `userapi_app` service is enabled).

**Option 2: Via Command Line or IntelliJ**

First, stop the application container in Docker:
```bash
docker compose stop userapi_app
```

Then run:

**Development profile:**
```bash
mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

**Production profile:**
```bash
mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

**Default:**
```bash
mvnw spring-boot:run
```

### 6. SonarQube Analysis

Run SonarQube analysis (requires SonarQube Docker to be running):
```bash
.scripts\sonar.bat
```

> **Note:** The script reads `SONAR_TOKEN` from `.env.local` and runs the analysis using the `localProfile` Maven profile.

---

## Notes and tips
- Keep **secrets out of Git**: use `.env` or your CI secret store.
- Use the **test profile with H2** for unit and integration tests.
- If ports conflict, update the bindings in `docker-compose.yml`.
- The **Docker + PostgreSQL + Adminer + Actuator** setup makes the project resemble a real enterprise microservice environment.
