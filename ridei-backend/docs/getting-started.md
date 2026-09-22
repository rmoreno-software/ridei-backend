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

The project ships with a `docker-compose.yml` at the root that starts a PostgreSQL 16 instance pre-configured for local development. Both `ridei-identity` and `ridei-garage` run against this same container, each with its own database and role.

```bash
docker compose up -d
```

This creates `ridei-identity`'s database:

| Parameter | Value |
|---|---|
| Host | `localhost` |
| Port | `5432` |
| Database | `ridei_identity` |
| User | `ridei` |
| Password | `ridei_dev` |

`ridei-garage`'s database isn't created automatically — provision it once against the running container:

```bash
docker exec -it ridei-postgres psql -U ridei -d postgres \
  -c "CREATE USER ridei_garage WITH PASSWORD 'ridei_garage_dev';" \
  -c "CREATE DATABASE ridei_garage OWNER ridei_garage;" \
  -c "REVOKE CONNECT ON DATABASE ridei_garage FROM PUBLIC;"
```

Data is persisted in a named Docker volume (`ridei_postgres_data`) and survives container restarts.

Verify the database is healthy:

```bash
docker compose ps
```

You should see `ridei-postgres` with status `healthy`.

---

## 3. Run database migrations

Flyway migrations run automatically on each service's startup. No manual step is needed — `ridei-identity` applies scripts from `ridei-identity/src/main/resources/db/migration/`, `ridei-garage` from its own `ridei-garage/src/main/resources/db/migration/`, each against its own database only.

---

## 4. Generate a local JWT key pair (one-time)

`ridei-identity` signs tokens with ES256 (asymmetric); `ridei-garage` verifies them with the matching public key. Neither has a usable default — you need a real key pair even for local dev (a local one, never reused in production).

Using `jshell` (no external tools required):

```bash
jshell
```
```java
var kpg = java.security.KeyPairGenerator.getInstance("EC");
kpg.initialize(new java.security.spec.ECGenParameterSpec("secp256r1"));
var kp = kpg.generateKeyPair();
var priv = java.util.Base64.getEncoder().encodeToString(kp.getPrivate().getEncoded());
var pub = java.util.Base64.getEncoder().encodeToString(kp.getPublic().getEncoded());
System.out.println("JWT_PRIVATE_KEY=" + priv);
System.out.println("JWT_PUBLIC_KEY=" + pub);
```

Or with OpenSSL (Git Bash on Windows, since `openssl` isn't on the plain PowerShell `PATH`):

```bash
openssl ecparam -name prime256v1 -genkey -noout -out local_private.pem
openssl pkcs8 -topk8 -nocrypt -in local_private.pem -outform DER | base64 -w0
openssl ec -in local_private.pem -pubout -outform DER | base64 -w0
```

Paste the two Base64 values (no `-----BEGIN...-----` headers) into:

- `ridei-identity/src/main/resources/application-local.yml`:
  ```yaml
  jwt:
    private-key: <JWT_PRIVATE_KEY value>
    public-key: <JWT_PUBLIC_KEY value>
  ```
- `ridei-garage/src/main/resources/application-local.yml` — **only** the public key, same value as above:
  ```yaml
  jwt:
    public-key: <same JWT_PUBLIC_KEY value>
  ```

---

## 5. Start the services

```bash
# ridei-identity (port 8081)
./mvnw spring-boot:run -pl ridei-identity -Dspring-boot.run.profiles=local

# ridei-garage (port 8082) — in a separate terminal
./mvnw spring-boot:run -pl ridei-garage -Dspring-boot.run.profiles=local
```

You should see, respectively:
```
Started IdentityServiceApplication in X.XXX seconds
```
```
Started GarageServiceApplication in X.XXX seconds
```

If using the IDE's Run/Debug panel instead of the CLI, make sure the launch config for each service sets `SPRING_PROFILES_ACTIVE=local` (see `.vscode/launch.json`) — running via the ▶ button directly on `main()` skips this and the app falls back to the default profile, which has no real database credentials or JWT keys.

---

## 6. Verify the services are up

```bash
curl -s -o /dev/null -w "%{http_code}" http://localhost:8081/api/v1/users/register \
  -X POST -H "Content-Type: application/json" \
  -d '{}'
# Expected: 400 (validation errors — the server is running and validating the empty body)

curl -s -o /dev/null -w "%{http_code}" http://localhost:8082/api/v1/garage/motorbikes
# Expected: 401 (no token — the server is running and enforcing auth)
```

---

## 7. (Optional) Build without running

```bash
# Build all modules, skip tests
./mvnw clean package -DskipTests

# Build and run all tests
./mvnw clean verify

# Build a single module and whatever it depends on
./mvnw clean package -pl ridei-garage -am -DskipTests
```

---

## Environment profiles

| Profile | File | Purpose |
|---|---|---|
| Default | `application.yml` | Shared base configuration (placeholders only, resolved from real env vars in production) |
| Local | `application-local.yml` | Local overrides, git-ignored, real values written directly (including the JWT key pair from step 4) |

---

## Stop everything

```bash
docker compose down
```

To also remove the persisted volume (full reset):

```bash
docker compose down -v
```
