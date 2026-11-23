# Database

## Overview

The project uses **PostgreSQL 16** as its relational database. Schema creation and evolution are managed by **Flyway**, which applies versioned SQL migration scripts on application startup.

Since `ridei-garage` was introduced as a separate microservice, **each service owns its own database** — there is no shared schema and no foreign key between them, even though `motorbikes.owner_id` conceptually refers to a `ridei_identity` user (see [§ridei-garage database](#ridei-garage-database) below for how that relationship is modeled without a cross-database FK).

- Schema management: Flyway 10 per service (scripts at each module's own `classpath:db/migration`)
- JPA strategy: `ddl-auto: validate` — Hibernate validates the schema against the entities but never modifies it; Flyway owns all DDL.

This document covers `ridei-identity`'s schema in detail (§1), `ridei-garage`'s schema (§2), `ridei-circuits`' PostGIS schema (§3) and `ridei-sessions`' schema (§4).

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
| `role` | `VARCHAR(20)` | `NOT NULL` | `RIDER`, `ORGANIZER` or `ADMIN` (no CHECK constraint) |
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

#### `V9__create_motorbike_setup_entries.sql`

### `motorbike_setup_entries`

Setup history (front tyre, rear tyre, final drive) — one row per mounted part; `removed_on IS NULL` marks the current one. FK to `motorbikes(id) ON DELETE CASCADE`, check constraints so each row carries only the columns of its component, a partial unique index guaranteeing one current part per component, and a unique `(motorbike_id, component, mounted_on)`. Full column list and rationale in [motorbike-setup.md § Persistence](motorbike-setup.md#5-persistence).

### Cross-database ownership — no foreign key

`owner_id` refers to a `ridei_identity.users.id`, but the two tables live in **physically separate databases** (potentially separate hosts in the future), so a real `FOREIGN KEY` is impossible and wouldn't be appropriate anyway — it would couple the two bounded contexts at the schema level, which contradicts the whole point of splitting them into independent microservices.

Instead, ownership is enforced entirely at the **application layer**:
- `owner_id` is only ever set from the authenticated JWT subject (`OwnerId.of(authentication.getName())` in `MotorbikeController`), **never** trusted from a request body — this is the same anti-IDOR pattern used throughout `ridei-identity` (`UserId.of(authentication.getName())`).
- There is no reverse lookup from garage into identity (no HTTP call, no shared table) — garage doesn't need to know a user exists to store their motorbikes; if a `userId` in a token is fabricated or belongs to a deleted account, that's identity's concern to prevent at token-issuance time, not garage's to validate at read time.
- If `ridei-identity` ever needs to show "this user has N motorbikes", that's a cross-service query (an authenticated call from identity to garage's own API), not a database join.

---

## 3. `ridei-circuits` database

Catalog of venues → tracks → layouts (see [circuits.md](circuits.md)). It runs on its **own PostGIS container** (`ridei-postgis`, port `5433`, image `postgis/postgis:16-3.4`) rather than in `ridei-postgres`. Switching the shared container's image (alpine → debian) would reuse identity's and garage's data volume across a libc change, which can invalidate text indexes (collation version mismatch).

| Parameter | Value |
|---|---|
| Host / port | `localhost:5433` |
| Database | `ridei_circuits` |
| User / password | `ridei_circuits` / `ridei_circuits_dev` (created by `docker-compose.yml`) |

### Migrations

| Script | Content |
|---|---|
| `V1__enable_extensions.sql` | `CREATE EXTENSION IF NOT EXISTS postgis` and `pg_trgm`. **`postgis` needs a superuser**: in production a superuser creates it when provisioning the DB, and V1 is then a no-op. |
| `V2__create_catalog_tables.sql` | `venue`, `track`, `layout` + constraints and indexes |
| `V3__seed_spanish_circuits.sql` | Main Spanish circuits, `VERIFIED`, `created_by NULL` (see "Seed" below) |
| `V4__update_track_disciplines.sql` | Adds `TRIAL`, `MIXED`, `DIRT_TRACK`; `SUPERMOTO` → `MIXED` |
| `V5__layout_geometry_optional.sql` | `layout.length_m` and `direction` become nullable (computed by the first GPS session) |
| `V6__layout_path.sql` | `layout.path` (drawing of the layout) + Montmeló and Calafat layouts with their drawings from OpenStreetMap (ODbL) |
| `V7__layout_finish_line.sql` | `layout.finish_line` + Montmeló's finish line (GP, GP con chicane, Nacional) from OpenStreetMap; their drawings rewritten to start at it |
| `V8__calafat_finish_line.sql` | Calafat Principal's finish line (point supplied by the team); its drawing rewritten to start at it |
| `V9__alcarras_speed_layout.sql` | Alcarràs speed track's *Principal* layout (default, 3743 m, 14 turns, counter-clockwise) with drawing and finish line from OpenStreetMap |

### Schema highlights

- Nothing is deleted. Duplicates get `status = 'MERGED'` and `merged_into_id`, a FK to the same table. `CHECK` keeps `merged_into_id` set if and only if the row is `MERGED`.
- `venue.latitude` and `venue.longitude` are plain columns. `location GEOGRAPHY(POINT, 4326)` is a `GENERATED ALWAYS … STORED` column, so Hibernate Spatial isn't needed. The JPA entity doesn't map it; geographic and trigram queries are native SQL in `VenueNativeQueries`.
- `normalized_name` (venue, track, layout) is computed in Java by `NameNormalizer` and is the input to `pg_trgm`.
- `venue.version` is the optimistic lock (`@Version`) of the whole aggregate.
- `layout.path geometry(LineString, 4326)` is the drawing of the layout in lap order, `NULL` until it has one. It is stored **closed** (last point = first) so it is a valid PostGIS ring; the API returns it open. At most 201 stored points (200 distinct, `LayoutPath.MAX_POINTS`), so listings stay light. No Hibernate Spatial: `LayoutJpaEntity` maps it as WKT text with `@ColumnTransformer` (`ST_AsText(path, 6)` / `ST_GeomFromText(?, 4326)`), converted in `LayoutGeometryWkt`. `CHECK`s: closed ring with 4–201 points, coordinates in range. When the layout has a finish line, the drawing **starts** where the finish line crosses it.
- `layout.finish_line geometry(LineString, 4326)`: the finish-line gate, exactly 2 points: left then right, looking in the direction of travel. `NULL` = unknown. Same WKT mapping; the domain (`LayoutPath.startingAt`) rejects a gate that doesn't cross the drawing, crosses it more than once, or has its ends swapped. Coordinates of drawings and gates are kept at 6 decimals (`GeoPoint.roundedToMicrodegrees`) in the domain too, so what a write returns matches what is read later.

| Constraint / index | Purpose |
|---|---|
| `uq_venue_external_place_id` — `UNIQUE (external_place_id) WHERE external_place_id IS NOT NULL` | Same map place ⇒ same venue |
| `ix_venue_location` — GIST on `location` | `ST_DWithin` / `ST_Distance` |
| `ix_venue_normalized_name_trgm`, `ix_track_normalized_name_trgm` — GIN `gin_trgm_ops` | `similarity`, `<%` |
| `uq_track_active_name` — `UNIQUE (venue_id, discipline, normalized_name) WHERE status <> 'MERGED'` | No duplicate track |
| `uq_layout_active_name` — `UNIQUE (track_id, normalized_name) WHERE status <> 'MERGED'` | No duplicate layout name |
| `ex_layout_single_default` — `EXCLUDE USING btree (track_id WITH =) WHERE (is_default AND status <> 'MERGED') DEFERRABLE INITIALLY DEFERRED` | One default per track |

The single-default rule is the **deferrable** form of `UNIQUE (track_id) WHERE is_default AND status <> 'MERGED'`. Changing the default takes two `UPDATE`s, and Hibernate doesn't guarantee their order within a flush; an immediate constraint would reject a valid swap if the new default were written first. The domain guarantees the invariant, and the venue's optimistic lock prevents concurrent writers.

### Seed

Fixed ids (`10000000-…` venues, `20000000-…` tracks, `30000000-…` layouts). The seed includes only data that public sources confirm consistently:

- **11 venues**: Barcelona-Catalunya, Jerez, MotorLand Aragón, Ricardo Tormo, Cartagena, Almería, Navarra, Albacete, Calafat, Alcarràs, Parcmotor Castellolí.
- **Tracks**: a road-racing track per venue; Alcarràs also has a mixed circuit (karting/supermoto) and flat track; Castellolí also has karting, motocross and enduro.
- **Layouts**: Montmeló GP (4657 m, 14 turns, clockwise, default) and Jerez GP (4423 m, 13 turns, clockwise) in V3. V6 adds Montmeló *GP con chicane* (4675 m), *Nacional* (2977 m) and *Escuela* (1650 m), all clockwise and with unknown turns (so none is "equivalent" to the GP), and Calafat *Principal* (3250 m, clockwise, default) and *Corto* (`PENDING_REVIEW`, no length, direction or drawing).
- **Drawings (`path`)**: all four Montmeló layouts and Calafat Principal, from OpenStreetMap `highway=raceway` ways (© OpenStreetMap contributors, ODbL; way ids and method in the migration header). `SeedDataIT` checks that each drawn length is within 5 % of `length_m`, that its orientation matches `direction` and that the venue pin falls on the default layout's drawing.

| Layout | Points | Drawn length | `length_m` | Finish line | Notes |
|---|---|---|---|---|---|
| Montmeló GP | 75 | 4653 m | 4657 | yes | |
| Montmeló GP con chicane | 77 | 4668 m | 4675 | yes | final chicane ways instead of the "AZ" sweepers |
| Montmeló Nacional | 44 | 2972 m | 2977 | yes | shortcut way "Catalunya (Cutting Half)" |
| Montmeló Escuela | 39 | 1704 m | 1650 | — (doesn't use the main straight) | +3.3 %; built with an unnamed OSM link way — worth a check against the circuit's map |
| Calafat Principal | 62 | 3240 m | 3250 | yes | direction inferred (see below) |
| Calafat Corto | — | — | — | — | no source identifies its shortcut |
| Alcarràs Principal (V9) | 59 | 3731 m | 3743 | yes | OSM `type=circuit` relation 21388960 (ways 194653541 + 1558804057); counter-clockwise from `oneway=yes`, matches Lapmeta |

**Finish lines**: Montmeló's comes from OSM node `14202158171` (`raceway=finish`), the finish of the `type=circuit` relations 284540 (GP without chicane), 2049529 (GP with chicane) and 21420925 (MotoGP); their ways match the ones used for the drawings. The gate is perpendicular to the direction of travel through that node: 10 m to the left and 7 m to the right, where the pit lane is. The pit lane's axis (way 178416729) is 14 m from the track's, so the gate stops at the midpoint, the pit wall, and does not enter the pit lane (its right end is 6.9 m from the pit lane's axis). The start line (`raceway=start`, 127 m earlier) is not stored. Alcarràs's (V9) is OSM node `14178221871` (`raceway=start-finish`, on the track's axis): 10 m to the left and 6.15 m to the right, half the 12.3 m to the pit lane's axis (way 194653524). Calafat has no finish line in OSM or any source found; the team supplied the point 40°55'56.0"N 0°50'21.9"E. It is 0.8 m from the track's axis, 82 m into the main straight. V8 projects it onto the axis and draws the gate the same way: 10 m to the left, and 6.5 m to the right towards the pit wall, half the 12.9 m between the track's axis and the pit lane's (way 119486546).

Calafat's direction is not published by any source found and its OSM way has no `oneway`. V6 seeds *clockwise* as an **inference**. The team's "the first corner is a left" fits it: going clockwise from the finish line, the straight ends in a gentle left (~26°) before a sharp right. It does not rule out counter-clockwise, though: that way the first corner would be a sharp left (~117°) 230 m after the line. It is the only orientation of the ring with more right-hand than left-hand corners, which fits the official "9 right / 7 left". It is also the only one where the bypass of *Curva de Escuela* (way 713130114) falls on the 3rd corner and before the cut of turns 5-6-7 (way 713130115), matching the official corner order and racingcircuits.info. Confirm with the circuit if possible.

#### Seed pending verification

These layouts were left out because sources disagree on length, turns or direction. Add them in a **new** migration once confirmed with the circuit's official data:

| Venue | Pending | Conflict found |
|---|---|---|
| Barcelona-Catalunya | Turns of *GP con chicane* (seeded in V6 without turns) | 16 turns per Wikipedia only |
| MotorLand Aragón | FIM GP layout (counter-clockwise) | 5078 m / 16 turns (Wikipedia) vs 5077 m / 17 turns (Michelin preview) |
| Ricardo Tormo | GP layout (counter-clockwise) | 14 turns (Wikipedia, Lapmeta) vs 13 (5 R + 8 L, motogp.com) |
| Navarra | Full circuit since 2024 (clockwise) | 16 turns (Wikipedia) vs 17 (Lapmeta) |
| Albacete | Full circuit 3550 m / 14 turns | Direction not stated by any source |
| Cartagena | Circuit ~3506 m / 18 turns | Length format inconsistent in the source; direction not stated |
| Almería | 4025 m, clockwise | "13 turns (8 R, 5 L and a chicane)": unclear whether the chicane counts |
| Calafat | Turns of *Principal*; *Corto* length/direction/drawing | 16 vs 17 turns in the same source; direction not stated (V6 infers clockwise, see above) |
| Alcarràs | Mixed circuit 1017 m / 13 turns (the speed track was added in V9, see above) | Lengths differ across sources; no drawing yet |
| Alcarràs | **Motocross track** | No source confirms a motocross track at the venue (not seeded) |
| Parcmotor Castellolí | Speed circuit; karting 1340 m / 27 turns; school circuit 1700 m | Speed length 4146 vs 4113 m; no direction for any |

Venue coordinates come from Wikipedia and were cross-checked with OpenStreetMap for Alcarràs and Albacete. Cartagena, Almería and Calafat come from a single source each; they are accurate enough for the 2 km dedup radius, but worth a check.

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

---

## 4. `ridei-sessions` database

Recorded track sessions (see [sessions.md](sessions.md)). Database `ridei_sessions` in `ridei-postgres`, role `ridei_sessions`, created by hand like garage's (see [getting-started.md](getting-started.md)).

| Migration | Content |
|---|---|
| `V1__create_sessions.sql` | `riding_session`, `session_lap`, `session_telemetry` |
| `V2__create_manual_lap_times.sql` | `manual_lap_time` |

- `riding_session`: one row per session. It stores a **snapshot** of the layout (`layout_path` and `finish_line` as JSONB `[[lat, lon], …]`, `sector_count`) and of the motorbike, so lap times don't change when the catalog does. `rider_id`, `venue_id`, `layout_id` and `motorbike_id` are ids from other contexts, **without FK**. `best_lap_ms`, `ideal_lap_ms` and `valid_lap_count` are denormalized for listings.
- `session_lap`: PK `(session_id, lap_number)`, `sector_ms BIGINT[]` (one element per sector, `NULL` = gate not detected), `validity` (`VALID`, `TOO_SHORT`, `TOO_LONG`, `GPS_GAP`).
- `session_telemetry`: raw GPS/IMU samples, gzip-compressed binary (`encoding = 'ridei-bin-v1'`, about 21 bytes per sample). Kept to re-time laps and to compute traces and corner analysis on demand.
- `ON DELETE CASCADE` from the session to its laps and telemetry.
- `manual_lap_time`: lap times typed in by the rider for sessions before Ridei (no GPS). Same layout/motorbike snapshot as `riding_session` but without geometry, plus `lap_date DATE`, `best_lap_ms`, `lap_precision` (`SECONDS`…`THOUSANDTHS`, how it was written down), `time_source` (`OFFICIAL_TIMING`, `LAPTIMER`, `STOPWATCH`, `APPROXIMATE`) and the optional `lap_count`, `track_condition`, `tyres`, `other_laps_ms BIGINT[]`, `notes`. Ids from other contexts **without FK**. Not linked to `riding_session`.

