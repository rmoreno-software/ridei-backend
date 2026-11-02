# Motorbike Setup (`ridei-garage`)

Tracks what is mounted on each motorbike (front tyre, rear tyre and final drive ratio) together with the full history of changes, so riders can later compare lap times between setups.

Source design: Claude Design project *Ridei App Screens* — screens **11** (motorbike sheet, "SETUP · CON HISTÓRICO" block), **11G** (empty sheet, "+ Añadir setup actual"), **11B** (setup history per component) and **11C** (register a setup change).

---

## 1. Scope

| In scope (backend) | Out of scope |
|---|---|
| Current setup per component, with "since" date and number of changes (11, 11G) | Sessions, laps and best lap per setup period shown in 11B ("2 SESIONES · 30 VUELTAS · 1:42.318"). Sessions are recorded and stored **on the phone** ("Las sesiones se guardan en local"), so the backend has no data to compute them. The client joins its local sessions with the history periods (`mountedOn` → `removedOn`). |
| History per component, newest first (11B) | Wear notes ("DESGASTE") — not modelled yet. |
| Register a change of one or several components on the same date — "Delantero / Trasero / Los dos" (11C) | Removing a part without a replacement, or deleting a history entry. |
| Initial setup of a motorbike with no setup yet (11G) | Suspension, tyre pressures and other setup items. |
| Correct an existing entry — "Editar" in 11B (typo in model/size/date) | |

---

## 2. Domain model

The setup is a **separate aggregate** (`MotorbikeSetup`), not another collection inside `Motorbike`:

- The history grows with every change; loading it every time the motorbike data is edited (or the garage is listed) would be wasteful.
- `MotorbikeJpaEntity.photos` is already an eager `List`; a second eager bag on the same entity makes Hibernate fail at startup (`MultipleBagFetchException`).
- Invariants of the setup (chronological chain per component) don't involve any motorbike field, so they don't need to share a consistency boundary.

The aggregate is identified by the `MotorbikeId`. Ownership is still checked through the motorbike: every service calls `OwnedMotorbikes.require(...)` first, so a motorbike that doesn't exist or isn't owned by the caller returns `404` (anti-IDOR).

| Type | Kind | Description |
|---|---|---|
| `MotorbikeSetup` | Aggregate root | All entries of one motorbike. Commands: `mount(component, spec, mountedOn)`, `correct(entryId, spec, mountedOn)`. Queries: `current(component)`, `history(component)` (newest first), `changeCount(component)`. |
| `SetupEntry` | Entity | One period of a component: `id`, `component`, `spec`, `mountedOn`, `removedOn` (`null` = currently mounted), `createdAt`. Only the aggregate can mutate it (package-private methods). |
| `SetupEntryId` | Value object | UUID wrapper. |
| `SetupComponent` | Enum | `FRONT_TYRE`, `REAR_TYRE`, `FINAL_DRIVE`. `accepts(spec)` says which spec type each component takes. |
| `SetupSpec` | Sealed interface | Permits `TyreSpec` and `FinalDriveRatio`. |
| `TyreSpec` | Value object | `model` (required, trimmed, ≤ 100) and `size` (optional, trimmed, ≤ 30, blank → `null`). The size is free text on purpose: road (`180/55 ZR17`), off-road (`110/90-19`) and classic (`3.00-21`) formats differ. |
| `FinalDriveRatio` | Value object | `frontSprocket` / `rearSprocket` teeth (1–99 each). `ratio()` = rear / front with 3 decimals (16 / 48 → `3.000`). |

### Invariants (per component)

1. Entries form a continuous, non-overlapping chain ordered by `mountedOn`.
2. At most one entry is current (`removedOn == null`), and it is the most recent one.
3. Mounting a new part closes the current one **the day before** the new `mountedOn` (11C: *"El trasero actual pasa al histórico con fin 02/10/2026"* when mounted on 03/10/2026).
4. A new `mountedOn` must be **strictly after** the current entry's `mountedOn` → otherwise `SetupChangeDateConflictException` (422). Backdating a part before the current one is not allowed.
5. A correction may change `mountedOn` only within its neighbours (strictly after the previous entry, strictly before the next one); the previous entry is re-closed the day before the new date.
6. `mountedOn` is required and cannot be in the future (`IllegalArgumentException` → 400).
7. The spec type must match the component (tyre spec for tyres, sprockets for the final drive) → 400.

Mounting the same tyre model again is allowed (11B shows two consecutive Power Cup 2 periods — a worn tyre replaced by a new one of the same model).

Deliberately **not** enforced: `mountedOn` before the motorbike's `acquisitionDate`, or changes on a retired motorbike. Neither appears in the design; add them in the service if the product needs them.

### Domain exceptions

| Exception | HTTP |
|---|---|
| `SetupChangeDateConflictException` | `422 Unprocessable Entity` |
| `SetupEntryNotFoundException` | `404 Not Found` |

Both are mapped in garage's `GlobalExceptionHandler`.

---

## 3. Application layer

Wired manually in `BeanConfig`, as every other service.

| Use case (`application/port/in`) | Service | Command | Transactional |
|---|---|---|---|
| `GetMotorbikeSetupUseCase.get(ownerId, motorbikeId)` | `GetMotorbikeSetupService` | — | No (read) |
| `RegisterSetupChangeUseCase.register(cmd)` | `RegisterSetupChangeService` | `RegisterSetupChangeCommand(ownerId, motorbikeId, mountedOn, Map<SetupComponent, SetupSpec> changes)` | Yes |
| `CorrectSetupEntryUseCase.correct(cmd)` | `CorrectSetupEntryService` | `CorrectSetupEntryCommand(ownerId, motorbikeId, entryId, spec, mountedOn)` | Yes |

`RegisterSetupChangeCommand` rejects an empty `changes` map and stores it as an `EnumMap`, so components are applied in a deterministic order. All changes of one command are applied inside a single transaction: either every component changes or none does ("Los dos").

The history endpoint reuses `GetMotorbikeSetupUseCase` and calls `setup.history(component)` — no dedicated use case is needed for a projection of the same aggregate.

Outbound port: `MotorbikeSetupRepositoryPort` (`findByMotorbikeId` always returns a setup, empty when nothing has been registered; `save`; `deleteByMotorbikeId`, used when the motorbike is deleted — see [motorbike-deletion.md](motorbike-deletion.md)).

---

## 4. REST API

Controller: `MotorbikeSetupController`. Base path: `/api/v1/garage/motorbikes/{id}/setup`. All endpoints require `Authorization: Bearer <access-token>`; the owner comes from the token subject.

| Method | Path | Screen | Success |
|---|---|---|---|
| `GET` | `/setup` | 11, 11G | `200` `MotorbikeSetupResponseDTO` |
| `GET` | `/setup/{component}/history` | 11B | `200` `SetupEntryResponseDTO[]` (newest first) |
| `POST` | `/setup/changes` | 11C, 11G | `201` `MotorbikeSetupResponseDTO` |
| `PUT` | `/setup/entries/{entryId}` | 11B "Editar" | `200` `MotorbikeSetupResponseDTO` |

`{component}` accepts `front-tyre`, `rear-tyre`, `final-drive` (case-insensitive; `REAR_TYRE` also works).

### `GET /api/v1/garage/motorbikes/{id}/setup`

All three components are always present. For an empty setup (11G) `current` is `null` and `changeCount` is `0` — never a `404`.

```json
{
  "motorbikeId": "3f2504e0-4f89-11d3-9a0c-0305e82c3301",
  "frontTyre": {
    "current": {
      "id": "9b1c…",
      "component": "FRONT_TYRE",
      "tyre": { "model": "Michelin Power Cup 2", "size": "120/70 ZR17" },
      "finalDrive": null,
      "mountedOn": "2026-09-02",
      "removedOn": null,
      "current": true
    },
    "changeCount": 3
  },
  "rearTyre": { "current": { "…": "…" }, "changeCount": 5 },
  "finalDrive": {
    "current": {
      "id": "c47e…",
      "component": "FINAL_DRIVE",
      "tyre": null,
      "finalDrive": { "frontSprocket": 16, "rearSprocket": 48, "ratio": 3.000 },
      "mountedOn": "2024-03-12",
      "removedOn": null,
      "current": true
    },
    "changeCount": 1
  }
}
```

`changeCount` is the number of parts registered in the component, including the current one ("3 CAMBIOS"). The "since" date is `current.mountedOn`.

### `GET /api/v1/garage/motorbikes/{id}/setup/{component}/history`

```json
[
  { "id": "…", "component": "REAR_TYRE", "tyre": { "model": "Michelin Power Cup 2", "size": "180/55 ZR17" },
    "finalDrive": null, "mountedOn": "2026-09-19", "removedOn": null, "current": true },
  { "id": "…", "component": "REAR_TYRE", "tyre": { "model": "Michelin Power Cup 2", "size": "180/55 ZR17" },
    "finalDrive": null, "mountedOn": "2026-09-02", "removedOn": "2026-09-18", "current": false },
  { "id": "…", "component": "REAR_TYRE", "tyre": { "model": "Pirelli Diablo Supercorsa SP", "size": "180/55 ZR17" },
    "finalDrive": null, "mountedOn": "2026-05-14", "removedOn": "2026-09-01", "current": false }
]
```

### `POST /api/v1/garage/motorbikes/{id}/setup/changes`

Registers one or more components mounted on the same date. Components left out don't change ("El delantero no cambia"). Also used to fill the initial setup from 11G.

```json
{
  "mountedOn": "2026-10-03",
  "frontTyre": { "model": "Michelin Power Cup 2", "size": "120/70 ZR17" },
  "rearTyre":  { "model": "Michelin Power Cup 2", "size": "180/55 ZR17" },
  "finalDrive": { "frontSprocket": 16, "rearSprocket": 48 }
}
```

| Field | Type | Required | Constraints |
|---|---|---|---|
| `mountedOn` | ISO date | Yes | Not in the future; strictly after the current entry of every changed component |
| `frontTyre`, `rearTyre` | object | At least one of the three | `model` non-blank ≤ 100; `size` optional ≤ 30 |
| `finalDrive` | object | At least one of the three | `frontSprocket`, `rearSprocket` required, 1–99 |

Returns `201` with the full setup summary (same shape as `GET /setup`).

### `PUT /api/v1/garage/motorbikes/{id}/setup/entries/{entryId}`

Corrects an entry (full replacement of its spec and `mountedOn`). Send `tyre` for a tyre entry or `finalDrive` for the final drive entry — exactly one.

```json
{
  "mountedOn": "2026-09-19",
  "tyre": { "model": "Michelin Power Cup 2", "size": "180/55 ZR17" }
}
```

Returns `200` with the full setup summary.

### Errors

| Status | When |
|---|---|
| `400` | DTO validation (missing `mountedOn`, no component, both `tyre` and `finalDrive`, teeth out of range…), future date, spec not matching the entry's component, unknown `{component}`, malformed UUID |
| `401` | Missing/invalid token |
| `404` | Motorbike doesn't exist or isn't owned by the caller; unknown `entryId` |
| `422` | `SetupChangeDateConflictException` — the date would break the chronological order of the component's history |

---

## 5. Persistence

Migration `V9__create_motorbike_setup_entries.sql` — table `motorbike_setup_entries`:

| Column | Type | Constraints |
|---|---|---|
| `id` | `UUID` | `PRIMARY KEY` |
| `motorbike_id` | `UUID` | `NOT NULL`, FK → `motorbikes(id) ON DELETE CASCADE` (same database, so a real FK is fine) |
| `component` | `VARCHAR(20)` | `NOT NULL`, `CHECK IN ('FRONT_TYRE','REAR_TYRE','FINAL_DRIVE')` |
| `tyre_model` | `VARCHAR(100)` | tyres only |
| `tyre_size` | `VARCHAR(30)` | tyres only, nullable |
| `front_sprocket` | `INTEGER` | final drive only |
| `rear_sprocket` | `INTEGER` | final drive only |
| `mounted_on` | `DATE` | `NOT NULL` |
| `removed_on` | `DATE` | nullable; `>= mounted_on` |
| `created_at` | `TIMESTAMP` | `NOT NULL DEFAULT now()` |

- `chk_setup_entries_spec_matches_component`: each row carries exactly the columns of its spec type (the sealed `SetupSpec` flattened into nullable columns).
- `uq_setup_entries_current` — partial unique index on `(motorbike_id, component) WHERE removed_on IS NULL`: the database also guarantees a single current part per component, which protects against two concurrent change requests (the loser gets a constraint violation instead of a corrupt history; today it surfaces as a generic `500`).
- `uq_setup_entries_mounted_on` — unique `(motorbike_id, component, mounted_on)`; also serves as the per-motorbike lookup index.

Adapter: `MotorbikeSetupPersistenceAdapter` + `SetupEntryJpaEntity` + `SetupEntryJpaRepository`. `motorbikeId` is a plain UUID column, not a `@ManyToOne`, because the setup is its own aggregate.

**Flush ordering.** Hibernate executes `INSERT`s before `UPDATE`s within one flush. When a change is registered, the new current row would be inserted while the previous one still has `removed_on IS NULL`, violating `uq_setup_entries_current`. `save()` therefore persists and flushes the already-existing entries first (`saveAllAndFlush`) and inserts the new ones afterwards.

---

## 6. Tests

- `MotorbikeSetupTest` (domain): empty state, closing the previous entry the day before, re-mounting the same model, independence between components, date-order conflicts, future dates, spec/component mismatch, failed mount leaving state untouched, corrections within/outside neighbours, `TyreSpec` normalisation, `FinalDriveRatio` validation and ratio.
- `RegisterSetupChangeServiceTest` (application, Mockito): "Los dos" mounts both tyres on the same date and saves; another owner's motorbike → `MotorbikeNotFoundException` and nothing saved; empty command rejected.

Not covered by unit tests: the controller, the JPA adapter and the V9 migration (Flyway + `ddl-auto: validate` run only when the service starts against PostgreSQL).

---

## 7. Possible next steps

- Undo the last change of a component (delete the current entry and reopen the previous one).
- Map `DataIntegrityViolationException` from concurrent changes to `409 Conflict`.
- Optional rules: `mountedOn >= acquisitionDate`, no changes on retired motorbikes.
- More setup items (suspension clicks, tyre pressures) as new `SetupComponent` values + `SetupSpec` implementations.
