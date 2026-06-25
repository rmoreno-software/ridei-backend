# Database

## Overview

The project uses **PostgreSQL 16** as its relational database. Schema creation and evolution are managed by **Flyway**, which applies versioned SQL migration scripts on application startup.

- Database name: `ridei_identity`
- Schema management: Flyway 10 (scripts at `classpath:db/migration`)
- JPA strategy: `ddl-auto: validate` — Hibernate validates the schema against the entities but never modifies it; Flyway owns all DDL.

---

## Running the database locally

The repository includes a `docker-compose.yml` at the root:

```bash
docker compose up -d
```

| Parameter | Value |
|---|---|
| Host | `localhost` |
| Port | `5432` |
| Database | `ridei_identity` |
| User | `ridei` |
| Password | `ridei_dev` |

Data is persisted in a named Docker volume (`ridei_postgres_data`).

---

## Migrations

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

## Enums stored as strings

All enum columns (`gender`, `document_type`, `role`, `account_status`) are stored as `VARCHAR` with the enum name as the string value (JPA `EnumType.STRING`). This makes the data human-readable and avoids the fragility of ordinal-based storage.

---

## Flyway configuration

```yaml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
```

`baseline-on-migrate: true` allows Flyway to work with an existing schema (e.g. if the database was created before Flyway was introduced). For new databases, it has no effect.
