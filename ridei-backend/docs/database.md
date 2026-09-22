# Database

## Overview

The project uses **PostgreSQL 16** as its relational database. Schema creation and evolution are managed by **Flyway**, which applies versioned SQL migration scripts on application startup.

Since `ridei-garage` was introduced as a separate microservice, **each service owns its own database** — there is no shared schema and no foreign key between them, even though `motorbikes.owner_id` conceptually refers to a `ridei_identity` user (see [§ridei-garage database](#ridei-garage-database) below for how that relationship is modeled without a cross-database FK).

- Schema management: Flyway 10 per service (scripts at each module's own `classpath:db/migration`)
- JPA strategy: `ddl-auto: validate` — Hibernate validates the schema against the entities but never modifies it; Flyway owns all DDL.

This document covers `ridei-identity`'s schema in detail (§1) and `ridei-garage`'s schema (§2).

---

## Running the databases locally

The repository includes a `docker-compose.yml` at the root, which brings up the shared Postgres container used by both services (each with its own database and role inside it):

```bash
docker compose up -d
```

| Parameter | `ridei-identity` | `ridei-garage` |
|---|---|---|
| Host | `localhost` | `localhost` |
| Port | `5432` | `5432` |
| Database | `ridei_identity` | `ridei_garage` |
| User | `ridei` | `ridei_garage` |
| Password | `ridei_dev` | `ridei_garage_dev` |

`ridei_garage`'s database and role aren't created by `docker-compose.yml` automatically — they're provisioned once against the running container:

```bash
docker exec -it ridei-postgres psql -U ridei -d postgres \
  -c "CREATE USER ridei_garage WITH PASSWORD 'ridei_garage_dev';" \
  -c "CREATE DATABASE ridei_garage OWNER ridei_garage;" \
  -c "REVOKE CONNECT ON DATABASE ridei_garage FROM PUBLIC;"
```

The `REVOKE CONNECT ... FROM PUBLIC` means only the `ridei_garage` role (and superusers) can connect to that database — `ridei-identity`'s role has no access to it, and vice versa. The same pattern is used in production with a generated, non-default password (see [deployment.md](deployment.md)).

Data is persisted in a named Docker volume (`ridei_postgres_data`).

---

## 1. `ridei-identity` database

### Migrations

Flyway migration scripts live at:

```
ridei-identity/src/main/resources/db/migration/
```

Scripts follow the naming convention `V{version}__{description}.sql`. They are applied in version order and never modified after being applied to any environment.

### `V1__create_identity_schema.sql`

Creates the initial identity schema with three tables.

---

## Schema

### `users`

The core user table. Every registered account, regardless of role, has a row here.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | `UUID` | `PRIMARY KEY` | Unique user identifier |
| `email` | `VARCHAR(255)` | `UNIQUE NOT NULL` | Login email |
| `password_hash` | `VARCHAR(255)` | `NOT NULL` | BCrypt hash of the password |
| `username` | `VARCHAR(32)` | `UNIQUE NOT NULL` | Public handle (starts with `@`) |
| `first_name` | `VARCHAR(100)` | `NOT NULL` | Given name |
| `last_name` | `VARCHAR(100)` | `NOT NULL` | Family name |
| `gender` | `VARCHAR(10)` | nullable | `MALE`, `FEMALE`, `OTHER` |
| `phone_number` | `VARCHAR(20)` | nullable | International phone number |
| `date_of_birth` | `DATE` | nullable | Date of birth |
| `country_code` | `VARCHAR(2)` | nullable | ISO 3166-1 alpha-2 |
| `document_type` | `VARCHAR(30)` | nullable | `NATIONAL_ID`, `PASSPORT`, `RESIDENCE_PERMIT`, `DRIVER_LICENSE` |
| `document_number` | `VARCHAR(30)` | nullable | Document number |
| `role` | `VARCHAR(20)` | `NOT NULL` | `RIDER` or `ORGANIZER` |
| `account_status` | `VARCHAR(30)` | `NOT NULL DEFAULT 'PENDING_VERIFICATION'` | Account lifecycle state |
| `terms_accepted` | `BOOLEAN` | `NOT NULL DEFAULT FALSE` | Whether terms were accepted |
| `terms_accepted_at` | `TIMESTAMP` | nullable | When terms were accepted |
| `created_at` | `TIMESTAMP` | `NOT NULL DEFAULT now()` | Account creation timestamp |

---

### `rider_profiles`

Stores additional data for users with `role = 'RIDER'`. Has a one-to-one relationship with `users`.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `user_id` | `UUID` | `PRIMARY KEY`, `REFERENCES users(id)` | Foreign key to `users` |
| `profile_type` | `VARCHAR(20)` | nullable | Type of rider profile |
| `racing_license_number` | `VARCHAR(50)` | nullable | Official racing licence number |

The `user_id` column serves both as primary key and foreign key, enforcing the one-to-one relationship at the database level.

---

### `organizer_accounts`

Stores additional data for users with `role = 'ORGANIZER'`. Has a one-to-one relationship with `users`.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `user_id` | `UUID` | `PRIMARY KEY`, `REFERENCES users(id)` | Foreign key to `users` |
| `legal_name` | `VARCHAR(150)` | `NOT NULL` | Registered legal name |
| `tax_id` | `VARCHAR(30)` | `NOT NULL` | Tax identification number |
| `iban` | `VARCHAR(34)` | `NOT NULL` | Bank account number for payouts |

---

## Entity relationships

```
users (1) ──────────── (0..1) rider_profiles
users (1) ──────────── (0..1) organizer_accounts
```

- A user can have at most one rider profile and at most one organizer account.
- Both profile tables use `user_id` as their primary key (`@MapsId` in JPA), ensuring a true one-to-one at the database level.
- Cascade delete is configured at the JPA level (`CascadeType.ALL`, `orphanRemoval = true`): deleting a `User` entity will delete its associated profile or account rows.

---

### Enums stored as strings

All enum columns (`gender`, `document_type`, `role`, `account_status`) are stored as `VARCHAR` with the enum name as the string value (JPA `EnumType.STRING`). This makes the data human-readable and avoids the fragility of ordinal-based storage.

---

## 2. `ridei-garage` database

Introduced 2026-09 as the first bounded context split into its own microservice with its own database (`ridei_garage`). It currently has a single table.

### Migrations

```
ridei-garage/src/main/resources/db/migration/
```

#### `V1__create_motorbikes.sql`

### `motorbikes`

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | `UUID` | `PRIMARY KEY` | Unique motorbike identifier |
| `owner_id` | `UUID` | `NOT NULL`, indexed (`idx_motorbikes_owner_id`) | The owning user's id — **no foreign key**, see below |
| `brand` | `VARCHAR(100)` | `NOT NULL` | |
| `model` | `VARCHAR(100)` | `NOT NULL` | |
| `year` | `INTEGER` | `NOT NULL` | |
| `displacement_cc` | `INTEGER` | nullable | |
| `weight_kg` | `NUMERIC(6,2)` | nullable | |
| `acquisition_date` | `DATE` | nullable | |
| `disposal_date` | `DATE` | nullable | Non-null means the motorbike has been retired/sold |
| `photo_url` | `VARCHAR(500)` | nullable | |
| `created_at` | `TIMESTAMP` | `NOT NULL DEFAULT now()` | |

Check constraint: `disposal_date` must be `>= acquisition_date` when both are set.

### Cross-database ownership — no foreign key

`owner_id` refers to a `ridei_identity.users.id`, but the two tables live in **physically separate databases** (potentially separate hosts in the future), so a real `FOREIGN KEY` is impossible and wouldn't be appropriate anyway — it would couple the two bounded contexts at the schema level, which contradicts the whole point of splitting them into independent microservices.

Instead, ownership is enforced entirely at the **application layer**:
- `owner_id` is only ever set from the authenticated JWT subject (`OwnerId.of(authentication.getName())` in `MotorbikeController`), **never** trusted from a request body — this is the same anti-IDOR pattern used throughout `ridei-identity` (`UserId.of(authentication.getName())`).
- There is no reverse lookup from garage into identity (no HTTP call, no shared table) — garage doesn't need to know a user exists to store their motorbikes; if a `userId` in a token is fabricated or belongs to a deleted account, that's identity's concern to prevent at token-issuance time, not garage's to validate at read time.
- If `ridei-identity` ever needs to show "this user has N motorbikes", that's a cross-service query (an authenticated call from identity to garage's own API), not a database join.

---

## Flyway configuration

Same pattern for both services:

```yaml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
```

`baseline-on-migrate: true` allows Flyway to work with an existing schema (e.g. if the database was created before Flyway was introduced). For new databases, it has no effect.
