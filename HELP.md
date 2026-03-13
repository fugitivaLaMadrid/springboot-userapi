# Getting Started

## Overview
Lightweight **Spring Boot service for user management**. 

This document explains the project layout, prerequisites, common commands, and how to run the app locally using Docker Compose.

## Project structure
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
│ │ ├─ controller/ # REST controllers
│ │ ├─ service/ # Business logic
│ │ ├─ repository/ # Database access
│ │ └─ model/ # Entities / DTOs
│ └─ resources/
│ ├─ application.yml # Prod / PostgreSQL config
│ └─ application-test.yml # Test (H2) config
└─ test/
└─ java/
└─ com/
└─ fugitivalamadrid/
└─ api/
└─ userapi/ # Test classes


### Important Files
- `application.yml` – Production configuration using PostgreSQL
- `application-test.yml` – H2 in-memory configuration for tests
- `.env` – Environment variables (ignored in Git; contains DB credentials)
- `Dockerfile` – Container image for the Spring Boot application
- `docker-compose.yml` – Brings up PostgreSQL, the app, and Adminer

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

## Stop Containers
```bash
docker compose down
```
-----
## Run tests (local, without Docker):
```bash
mvn test -P test
```
This runs tests using the H2 in-memory database.

## Services (Docker Compose)
1. PostgreSQL 
   - Service name: `userapi_postgres`
2. Spring Boot application 
   - Service name: `userapi_app`
3. Adminer (Database UI)
   - URL: `(http://localhost:8081)`

## Database connection
1. JDBC URL: `jdbc:postgresql://localhost:5432/userdb`
   2. Username: defined in .env `(POSTGRES_USER)`
   3. Password: defined in .env `(POSTGRES_PASSWORD)`

2. Adminer UI:
   3. Service name: `userapi_adminer`
   4. URL: `http://localhost:8081`

You can inspect and manage the user table there for CRUD testing.

## Logging & observability
Spring Boot Actuator endpoints:
- Actuator: `/actuator/*`
- Health: `/actuator/health`
- Metrics: `/actuator/metrics`

## View container logs:
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
  - .jar in target/. 
- The pipeline can be extended to publish container images to:
    - GitLab Container Registry
    - Docker Hub
    - Nexus
## AI Workflow
This project includes an `.ai` folder that defines **AI-assisted development workflows.**

When generating commit messages, you can instruct AI tools to follow:
`.ai/skills/commit.md.`

Example commit messages:
- `feat(model): add User entity with JPA annotations`
- `feat(api): implement GET /users endpoint`

## Notes and tips
- Keep **secrets out of Git**: use `.env` or your CI secret store.
- Use the **test profile with H2** for unit and integration tests.
- If ports conflict, update the bindings in `docker-compose.yml`
- The **Docker + PostgreSQL + Adminer + Actuator** setup makes the project resemble a real enterprise microservice environment.