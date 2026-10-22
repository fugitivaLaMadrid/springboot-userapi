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

### 1. Run Locally - First time
This application uses JWT authentication via Keycloak. **Keycloak must be configured before the application can start successfully.**

```bash
docker compose up --build
```

After running the docker compose, the below container are running:
1. *PostgreSQL*
Service name: userapi_postgres
2. Spring Boot application
Service name: userapi_app
URL: http://localhost:8080
3. Adminer (Database UI)
Service name: userapi_adminer
URL: http://localhost:8081
4. SonarQube (Code quality)
5. Service name: userapi_sonarqube
URL: http://localhost:9000

The "userapi_app" will failed because for the first time you MUST configure the below:
1. Stop all the services
#### Step 1: Start Keycloak
```bash
docker compose up -d keycloak
```
#### Step 2: Configure Keycloak Realm
1. Access Keycloak Admin Console at http://localhost:8180/admin/master/console/#/master/realms
2. Login with credentials:
    - **Username:** `admin`
    - **Password:** `admin123` (from `.env` file)
3. Run the setup script to create the realm and client:
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

#### Step 3: Start the Application
Now start PostgreSQL and the application:
```bash
docker compose up -d postgres userapi
```

The application will now start successfully since Keycloak is configured and running.



### 2. Run Locally - After first time setup

Choose one of the following options:

**Option A: Via Docker Compose (with all infrastructure):**
Start up all the existing containers (Important keycloak MUST be running and active)
```bash
docker compose up --build
```

**Option B: Via Command Line (infrastructure must be running):** 
Start Up container: userapi_postgres
```bash
mvnw spring-boot:run
```

### 2. Run Tests
Ensure all tests pass before building:
```bash
mvn test
```

### 3. Run Code Quality Analysis & Build

You have two options to build your project:

**Option A: Build with Maven clean verify**
```bash
mvn clean verify
```
This compiles, tests, and packages the application without code quality analysis.

**Option B: Build and run code quality analysis with SonarQube**
> **⚠️ Warning:** SonarQube Docker container must be running! Start it first if not already running:
> ```bash
> # First time only (or after docker compose down)
> docker compose up -d sonarqube
> ```
>
> **Note:** The token must be generated in SonarQube and copied to `.env.local`

Run the analysis script:
```bash
.scripts\sonar.bat
```
This runs `mvn clean verify` with additional SonarQube code quality analysis.


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
Only KeyCloak container
#### Step 1: Start Keycloak
```bash
docker compose up -d keycloak
```

#### Step 2: Configure Keycloak Realm
1. Access Keycloak Admin Console at http://localhost:8180/admin/master/console/#/master/realms
2. Login with credentials:
    - **Username:** `admin`
    - **Password:** `admin123` (from `.env` file)
3. Run the setup script to create the realm and client:
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

#### Step 3: Start the Application
Now start PostgreSQL and the application:
```bash
docker compose up -d postgres userapi
```

The application will now start successfully since Keycloak is configured and running.


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

### 7. Postman
Open postman collection user-api (JWT/OAuth)
It has 4 directories:
1. Auth
2. User Endpoints
3. Error Handling
4. Actuator endpoints
http://localhost:8080/actuator/health
5. test_pagination

- Test with postman:
  - In the main directory "user-api (JWT/OAuth2)" are store the variables,there is one "accesToken" which is empty
  - Get Access Token (Password Grant)
     http://localhost:8180/realms/userapi-realm/protocol/openid-connect/token
  - This would generate the token, the script is postman is ready to copy the token to all endpoint in Headers -->Authoriazion {{accessToken}}
  - Finally you can test http://localhost:8080/users auth

- **They use the same URL but different OAuth2 grant types:**
http://localhost:8180/realms/userapi-realm/protocol/openid-connect/token
  - Password Grant:
    - Uses grant_type=password 
    - Requires: username, password, client_id, client_secret 
    - Authenticates as a user
  - Client Credentials:
    - Uses grant_type=client_credentials 
    - Requires: client_id, client_secret 
    - Authenticates as the application/client (no user context)
- Both hit the same Keycloak token endpoint, but the request body parameters differ based on which grant type you're using.

## Notes and tips
- Keep **secrets out of Git**: use `.env` or your CI secret store.
- Use the **test profile with H2** for unit and integration tests.
- If ports conflict, update the bindings in `docker-compose.yml`.
- The **Docker + PostgreSQL + Adminer + Actuator** setup makes the project resemble a real enterprise microservice environment.

