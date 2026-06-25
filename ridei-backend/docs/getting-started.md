# Getting Started

## Prerequisites

| Tool | Minimum version | Purpose |
|---|---|---|
| JDK | 21 | Compile and run the application |
| Maven | 3.9+ | Build tool (or use the included `mvnw` wrapper) |
| Docker | 24+ | Run PostgreSQL via Docker Compose |
| Docker Compose | 2.x | Orchestrate the local database |

---

## 1. Clone the repository

```bash
git clone https://github.com/ridei/ridei-backend.git
cd ridei-backend
```

---

## 2. Start the database

The project ships with a `docker-compose.yml` at the root that starts a PostgreSQL 16 instance pre-configured for local development.

```bash
docker compose up -d
```

This creates:

| Parameter | Value |
|---|---|
| Host | `localhost` |
| Port | `5432` |
| Database | `ridei_identity` |
| User | `ridei` |
| Password | `ridei_dev` |

Data is persisted in a named Docker volume (`ridei_postgres_data`) and survives container restarts.

Verify the database is healthy:

```bash
docker compose ps
```

You should see `ridei-postgres` with status `healthy`.

---

## 3. Run database migrations

Flyway migrations run automatically on application startup. No manual step is needed — when the application starts, it applies all pending scripts from `ridei-identity/src/main/resources/db/migration/`.

---

## 4. Start the `ridei-identity` service

```bash
# Using the Maven wrapper (recommended — no local Maven required)
./mvnw spring-boot:run -pl ridei-identity

# Or with a locally installed Maven
mvn spring-boot:run -pl ridei-identity
```

The service starts on **port 8081**. You should see a log line similar to:

```
Started IdentityServiceApplication in X.XXX seconds
```

---

## 5. Verify the service is up

```bash
curl -s -o /dev/null -w "%{http_code}" http://localhost:8081/api/v1/users/register \
  -X POST -H "Content-Type: application/json" \
  -d '{}'
```

Expected response: `400` (validation errors — the server is running and validating the empty body).

---

## 6. (Optional) Build without running

```bash
# Build all modules, skip tests
./mvnw clean package -DskipTests

# Build and run all tests
./mvnw clean verify
```

---

## Environment profiles

| Profile | File | Purpose |
|---|---|---|
| Default | `application.yml` | Shared base configuration |
| Local | `application-local.yml` | Local overrides (git-ignored sensitive values) |

To activate the local profile:

```bash
./mvnw spring-boot:run -pl ridei-identity -Dspring-boot.run.profiles=local
```

---

## Stop everything

```bash
docker compose down
```

To also remove the persisted volume (full reset):

```bash
docker compose down -v
```
