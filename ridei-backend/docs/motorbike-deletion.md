# Motorbike Deletion (`ridei-garage`)

Permanently deletes a motorbike together with everything that belongs to it in the garage: its setup history, its photo rows and the photo objects stored in Cloudflare R2.

Source design: screen **11F** (*Editar moto*) — "Eliminar moto · Borra la moto y sus 14 sesiones. Para conservarlas, usa la fecha de baja."

> Sessions live on the phone, not in the backend. Deleting the sessions mentioned in 11F is the client's job; the backend only removes garage data. "Retiring" a motorbike (keeping history) is still done by setting `disposalDate` via `PUT /motorbikes/{id}`.

---

## 1. Endpoint

### `DELETE /api/v1/garage/motorbikes/{id}`

Requires `Authorization: Bearer <access-token>`. The owner comes from the token subject.

| Status | When |
|---|---|
| `204 No Content` | Motorbike, setup and photo rows deleted. Stored photos are purged right after (see §3). |
| `400 Bad Request` | `{id}` is not a UUID |
| `401 Unauthorized` | Missing/invalid token |
| `404 Not Found` | Motorbike doesn't exist **or** belongs to another user (anti-IDOR: indistinguishable) |

Not idempotent in status code: a second `DELETE` of the same id returns `404`. Deleting the active motorbike is allowed; no other motorbike is auto-activated.

---

## 2. What gets deleted, and where

| Data | Owner | How |
|---|---|---|
| `motorbike_setup_entries` rows | `MotorbikeSetup` aggregate | `MotorbikeSetupRepositoryPort.deleteByMotorbikeId` — explicit bulk delete, **before** the motorbike (it references it) |
| `motorbike_photos` rows | `Motorbike` aggregate | Part of the aggregate: `cascade = ALL, orphanRemoval = true` on `MotorbikeJpaEntity.photos` when `MotorbikeRepositoryPort.delete` runs |
| `motorbikes` row | `Motorbike` aggregate | `MotorbikeRepositoryPort.delete` |
| R2 objects under `garage/{ownerId}/{motorbikeId}/` | Storage | `MotorbikePhotoStoragePort.deleteAllOf`, **after commit** |

The `ON DELETE CASCADE` foreign keys in V8/V9 stay as a safety net, but the application does not rely on them: each aggregate's data is removed through its own port, so the intent is visible in the code and doesn't depend on the schema.

The storage purge deletes by the motorbike's **key prefix**, not by the list of confirmed photos, so it also removes objects uploaded with a presigned URL whose `confirm` never happened.

---

## 3. Flow and consistency

```
DELETE /motorbikes/{id}
  └─ MotorbikeController.delete
       └─ DeleteMotorbikeService.delete              @Transactional
            ├─ OwnedMotorbikes.require(...)            → 404 if missing / not owned
            ├─ setupRepository.deleteByMotorbikeId(id)
            ├─ motorbikeRepository.delete(motorbike)   (photo rows cascade)
            └─ eventPublisher.publish(MotorbikeDeletedEvent)
       ── COMMIT ──
  └─ MotorbikeDeletedListener.on(event)              @TransactionalEventListener(AFTER_COMMIT)
       └─ PurgeMotorbikePhotosService.purge(event)
            └─ photoStorage.deleteAllOf(ownerId, motorbikeId)   → R2 ListObjectsV2 + DeleteObject per key
```

Why the storage is purged **after commit** (unlike single-photo deletion, which calls R2 inside the transaction):

- R2 is not transactional. Purging inside the transaction and then failing to commit would leave a live motorbike whose photos point to deleted objects.
- With `AFTER_COMMIT`, a rollback leaves the photos untouched; the purge only runs once the database deletion is final.
- If R2 fails after commit, the listener logs `Could not purge stored photos of deleted motorbike …; objects left orphaned` at `ERROR` (with both ids) and the request still returns `204`. The cost is orphaned objects in the bucket, which can be found by that prefix — never broken references in the database.

`DeleteObjects` (batch) is intentionally not used: AWS SDK 2.47 sends flexible checksums on it that R2 has not always accepted; a motorbike has few photos and `DeleteObject` is the same call already used in production by `DELETE /photos/{photoId}`.

The purge runs synchronously in the request thread after commit, so it adds R2 latency to the response. If that becomes noticeable, annotate the listener with `@Async` (and enable async) — the consistency guarantees stay the same.

---

## 4. Components

| Layer | Type | Role |
|---|---|---|
| Domain | `domain/event/MotorbikeDeletedEvent` | `ownerId`, `motorbikeId`, `occurredAt` (all required) |
| Domain | `EventPublisherPort` (out) | Publishes domain events. Same pattern as identity's port (duplicated on purpose, no shared module) |
| Domain | `MotorbikeRepositoryPort.delete(Motorbike)` | New method |
| Domain | `MotorbikeSetupRepositoryPort.deleteByMotorbikeId(MotorbikeId)` | New method; no-op when there is no setup |
| Domain | `MotorbikePhotoStoragePort.deleteAllOf(OwnerId, MotorbikeId)` | New method; idempotent |
| Application | `DeleteMotorbikeUseCase` / `DeleteMotorbikeService` / `DeleteMotorbikeCommand` | Transactional use case above |
| Application | `PurgeMotorbikePhotosUseCase` / `PurgeMotorbikePhotosService` | Storage clean-up, triggered by the event |
| Infra (in) | `adapter/in/event/MotorbikeDeletedListener` | Event-driven inbound adapter, `AFTER_COMMIT`, logs and swallows storage failures |
| Infra (out) | `adapter/out/event/SpringEventPublisherAdapter` | `EventPublisherPort` → Spring `ApplicationEventPublisher` |
| Infra (out) | `MotorbikePersistenceAdapter.delete`, `MotorbikeSetupPersistenceAdapter.deleteByMotorbikeId` (+ `SetupEntryJpaRepository.deleteAllByMotorbikeId`), `CloudflareR2MotorbikePhotoStorageAdapter.deleteAllOf` | Port implementations |
| Infra (in) | `MotorbikeController.delete` | `DELETE /{id}` → `204` |
| Config | `BeanConfig` | `deleteMotorbikeUseCase`, `purgeMotorbikePhotosUseCase` beans |

No migration is needed and no new exception: not-found reuses `MotorbikeNotFoundException` (404).

---

## 5. Tests

| Test | Covers |
|---|---|
| `DeleteMotorbikeServiceTest` | Setup deleted before the motorbike (`InOrder`); `MotorbikeDeletedEvent` published with owner and id; another owner's / unknown motorbike → `MotorbikeNotFoundException` and nothing deleted or published |
| `PurgeMotorbikePhotosServiceTest` | Delegates to `deleteAllOf` with the event's owner and motorbike |
| `MotorbikeDeletedListenerTest` | Delegates to the purge use case; storage failure is logged and not propagated; the handler is bound to `AFTER_COMMIT` without fallback execution |
| `MotorbikeDeletionTransactionTest` | Real Spring wiring (`@Transactional` proxy + `@TransactionalEventListener`) with a minimal context and an in-memory transaction manager, no database: photos purged after a commit, **not** purged when the deletion rolls back |
| `MotorbikeDeletedEventTest` | Event invariants |

`MotorbikeDeletionTransactionTest` is the only garage test that starts a (tiny) Spring context; it's needed because the guarantee being tested lives in Spring's transaction synchronization, not in our code.

Not covered: the JPA adapters and the R2 adapter (need PostgreSQL / R2). To verify end to end with the `local` profile: create a motorbike, add a photo and a setup change, `DELETE` it → `204`; then `GET /motorbikes` no longer lists it, `motorbike_setup_entries` / `motorbike_photos` have no rows for it, and the bucket has no keys under `garage/{ownerId}/{motorbikeId}/`.
