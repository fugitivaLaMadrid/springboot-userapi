# Getting Started

## Overview
Lightweight **Spring Boot service for user management**.

This document explains the project layout, prerequisites, common commands, and how to run the app locally using Docker Compose.

## Project structure
```
.ai/
├─ README.md
└─ skills/
     └─ commit.md

src/
├─ main/
│ ├─ java/
│ │ └─ com/
│ │ └─ fugitivalamadrid/
│ │ └─ api/
│ │ └─ userapi/
│ │ ├─ controller/     # REST controllers
│ │ ├─ service/        # Business logic
│ │ ├─ repository/     # Database access
│ │ ├─ dto/            # Request and Response DTOs
│ │ └─ model/          # Entities
│ └─ resources/
│ ├─ application.yml          # Prod / PostgreSQL config
│ └─ application-test.yml     # Test (H2) config
└─ test/
└─ java/
└─ com/
└─ fugitivalamadrid/
└─ api/
└─ userapi/           # Test classes
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

## Notes and tips
- Keep **secrets out of Git**: use `.env` or your CI secret store.
- Use the **test profile with H2** for unit and integration tests.
- If ports conflict, update the bindings in `docker-compose.yml`.
- The **Docker + PostgreSQL + Adminer + Actuator** setup makes the project resemble a real enterprise microservice environment.
