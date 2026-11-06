# Circuits (`ridei-circuits`)

Catalog of motorcycle racing circuits, shared by the rest of the platform (events, sessions and lap times reference its ids). Port **8083**, base path **`/api/v1/circuits`**, database **`ridei_circuits`** on a PostGIS-enabled PostgreSQL.

## 1. Domain model

Three levels, one aggregate:

```
Venue (complex)       ← aggregate root; WHERE it is
 └─ Track (pista)     ← entity; WHICH discipline is ridden
     └─ Layout        ← entity; WHICH exact route
```

| Real case | How it is modelled |
|---|---|
| Alcarràs | One venue, several tracks of different disciplines (road racing, karting, flat track…) very close together. Being close does **not** make them duplicates. |
| Montmeló | One venue, one road-racing track, several layouts (GP, GP con chicane…). |
| Jerez | One venue, one track. |

- **Venue**: `VenueId`, `VenueName`, `CountryCode` (ISO 3166-1 alpha-2 — not the numeric phone prefix used by identity), `GeoPoint`, optional `ExternalPlaceId` (Google Places / Mapbox id sent by the client), `CatalogStatus`, `mergedInto`, `createdBy` (from the JWT; `null` for seed data), timestamps, optimistic `version`.
- **Track**: `TrackId`, `Discipline` (`ROAD_RACING`, `KARTING`, `ENDURO`, `TRIAL`, `MOTOCROSS`, `MIXED`, `FLAT_TRACK`, `DIRT_TRACK`; `MIXED` combines asphalt and dirt, e.g. supermoto), `TrackName`, status, `mergedInto`.
- **Layout**: `LayoutId`, `LayoutName`, optional `Length` (m, > 0), optional `Turns` (≥ 0), optional `Direction` (`CLOCKWISE`, `COUNTER_CLOCKWISE`), `isDefault`, status, `mergedInto`. Length, turns and direction come from the GPS of the first session, so a newly proposed layout may have only a name (`LayoutSpec.hasGeometry()` is false).
- **CatalogStatus**: `PENDING_REVIEW` (user proposals — usable immediately) → `VERIFIED` (admin). `MERGED` is terminal: the row is kept (other services hold its id) and points to the canonical item.

Invariants enforced by the aggregate (and backed by DB constraints):

- A track with active layouts has **exactly one default**; the first layout created becomes the default.
- `Venue.addTrack` rejects same discipline + similar name → `DuplicateTrackException`.
- `Track.addLayout` rejects an equivalent layout → `DuplicateLayoutException`.
- `Layout.isEquivalentTo`: similar name **or** (length within `Layout.EQUIVALENT_LENGTH_TOLERANCE_METERS` = 25 m **and** same turns **and** same direction). Unknown turns on only one side do not count as "same turns". Geometry only counts when both layouts have length and direction: a layout without them is compared by name only. Layout names with different numbers are never similar ("Variante 5" ≠ "Variante 6", the names the app gives to unnamed layouts).
- `MERGED` items reject every change (`MergedCatalogItemException`).

The domain package is pure Java: no Spring, JPA or Lombok.

### Name normalization and similarity

`VenueName` / `TrackName` / `LayoutName` keep the original text and expose `normalized()`: lowercase, diacritics removed, punctuation → spaces, spaces collapsed, generic words removed (`circuit`, `circuito`, `pista`, `track`, `de`, `del`, `d`, `la`, `el`, `les`, `los`… — see `NameNormalizer.GENERIC_WORDS`). If only generic words remain, they are kept.

`"Circuit de Barcelona-Catalunya"` and `"circuito barcelona catalunya"` both normalize to `barcelona catalunya`.

Similarity is trigram-based. `TrigramSimilarity` is a Java replica of `pg_trgm`'s `similarity()` so that in-memory checks (tracks, layouts) and SQL checks (venues) agree. The normalized name is persisted in `normalized_name`; changing the normalization rules requires a migration that recomputes it (`SeedDataIT` fails otherwise).

### Domain events

`VenueProposed`, `TrackAdded`, `LayoutAdded`, `VenueMerged`, `TrackMerged`, `LayoutMerged` are recorded by the aggregate and published through `DomainEventPublisherPort` after a successful save. The current adapter publishes on Spring's in-process bus; it can be replaced by a broker adapter (ideally with an outbox) without touching domain or application. **Merge events matter to other services**: they must re-point references from `source` to `target` ids.

## 2. Deduplication rules

| Situation | Result |
|---|---|
| Same `externalPlaceId` | Same venue → **200** with the existing (canonical) one — even with `confirmedNotDuplicate = true` |
| Venue < 2 km away **and** name similarity ≥ 0.4 | Possible duplicate → **409 `POSSIBLE_DUPLICATE_VENUE`** with candidates |
| Venue < 2 km away with a very different name | New venue (independent neighbouring facilities) |
| Same venue + same discipline + similar name | **409 `DUPLICATE_TRACK`** with the existing track |
| Same venue + different discipline | New track (Alcarràs case) |
| Same track + equivalent layout | **409 `DUPLICATE_LAYOUT`** with the existing layout |

- Radius and venue similarity threshold: `circuits.deduplication.venue-radius-km` / `venue-min-similarity` in `application.yml` (defaults 2 / 0.4). Track and layout similarity use the domain default (`SimilarityThreshold.DEFAULT` = 0.4).
- After seeing the candidates, the client can resend with `confirmedNotDuplicate: true` to force creation (not applicable to tracks/layouts, nor to a matching `externalPlaceId`).

### Race conditions

Two concurrent writes must end with one winner and the other getting the existing item or a 409 — **never a 500**:

1. **Area lock** — `ProposeVenue` takes `pg_advisory_xact_lock` on every 0.05° grid cell covering the dedup radius around the point, in a fixed order, *before* checking duplicates. Overlapping proposals serialize, and the second one sees the first.
2. **Optimistic lock** — every aggregate mutation touches `updated_at`, so `@Version` on `venue` always increments; concurrent `addTrack`/`addLayout` on the same venue collide.
3. **UNIQUE constraints** — last barrier (`external_place_id`, active track/layout names, single default).

The persistence adapter translates `DataIntegrityViolationException` → `CatalogItemAlreadyExistsException` and optimistic-lock failures → `ConcurrentCatalogModificationException`. `RetryingUseCases` wraps `ProposeVenue`, `AddTrack` and `AddLayout`: on either exception it retries **once in a new transaction**, where the domain now sees the winner and answers with the existing item / a duplicate 409. If it still fails, the handler returns 409 `CATALOG_ITEM_ALREADY_EXISTS` / `CONCURRENT_MODIFICATION`.

### Merges

- Ids never change. `MergeVenues(source, target)` moves **all** source tracks to the target (their `venue_id` is updated). A source track that clashes with a target track (same discipline + similar name) is merged into it: equivalent layouts become `MERGED`, the others move to the target track as non-default.
- `MergeTracks` works within one venue or across venues (same discipline only). `MergeLayouts` only within the same track; if the source was the default, the target becomes the default.
- `GET /venues/{id}` on a merged id follows `mergedInto` (max 10 hops) and returns the canonical venue with `redirectedFromMerged: true`.

## 3. Use cases

| Kind | Use case | Service |
|---|---|---|
| Query | `SearchVenuesUseCase` — free text (`word_similarity` over venue and track names), country, discipline, proximity; paginated; excludes `MERGED` | `SearchVenuesService` |
| Query | `FindNearbyVenuesUseCase` — by distance | `FindNearbyVenuesService` |
| Query | `GetVenueDetailUseCase` — follows merges | `GetVenueDetailService` |
| User | `ProposeVenueUseCase`, `AddTrackToVenueUseCase`, `AddLayoutToTrackUseCase` | one service each, wrapped by `RetryingUseCases` |
| Admin | `VerifyVenue/Track/LayoutUseCase` | `VerifyCatalogService` (implements the three) |
| Admin | `MergeVenues/Tracks/LayoutsUseCase` | one service each |
| Admin | `SetDefaultLayoutUseCase` | `SetDefaultLayoutService` |
| Admin | `UpdateVenue/Track/LayoutUseCase` (partial corrections; dedup rules re-applied) | `CorrectCatalogService` (implements the three) |

Queries take `*Query` records; commands take `*Command` records. A `java.time.Clock` bean is injected for deterministic tests.

## 4. REST API (`/api/v1/circuits`)

There is a frontend-oriented guide (in Spanish) with JSON examples and error handling: [circuits-frontend-guide.md](circuits-frontend-guide.md). The machine-readable spec is [openapi/ridei-circuits.json](openapi/ridei-circuits.json); `OpenApiExportIT` regenerates it when run with `-Dopenapi.export=true`.

All endpoints need a valid **access** token issued by `ridei-identity`; `/admin/**` needs role `ADMIN`.

Tokens are checked in two steps:
1. Locally: ES256 signature, expiry, `type=access`.
2. Online: `GET /api/v1/auth/validate` on identity confirms the account is active and the token isn't revoked (`tv`). `CachingTokenValidator` caches each verdict for 60 s (`circuits.identity.validation-cache-ttl`), so a revocation takes effect within about a minute.

Responses: `401` without a token, or with an invalid or revoked one; `403` with an insufficient role; `503` if identity can't be reached to validate (fails closed). Identity is only called for tokens that passed step 1.

### User endpoints

| Method | Path | Success | Errors |
|---|---|---|---|
| `GET` | `/venues?q=&country=&discipline=&lat=&lon=&radiusKm=&page=&size=` | 200 page | 400 (`lat`, `lon`, `radiusKm` must go together; `size` ≤ 100; radius ≤ 500 km) |
| `GET` | `/venues/nearby?lat=&lon=&radiusKm=&page=&size=` | 200 page sorted by distance (`radiusKm` default 50) | 400 |
| `GET` | `/venues/{venueId}` | 200 `{requestedId, redirectedFromMerged, venue}` | 404 |
| `POST` | `/venues` | **201** `{outcome: CREATED, venue}` + `Location` · **200** `{outcome: EXISTING_BY_EXTERNAL_PLACE_ID, venue}` | 400, 409 `POSSIBLE_DUPLICATE_VENUE` |
| `POST` | `/venues/{venueId}/tracks` | 201 track | 404, 409 `DUPLICATE_TRACK` / `CATALOG_ITEM_MERGED` |
| `POST` | `/venues/{venueId}/tracks/{trackId}/layouts` | 201 layout | 404, 409 `DUPLICATE_LAYOUT` |

`POST /venues` body (`createdBy` is always taken from the JWT; a body field is ignored):

```json
{
  "name": "Circuit de Barcelona-Catalunya",
  "countryCode": "ES",
  "latitude": 41.57,
  "longitude": 2.26111,
  "externalPlaceId": "ChIJ…",
  "confirmedNotDuplicate": false,
  "track":  { "discipline": "ROAD_RACING", "name": "Velocidad" },
  "layout": { "name": "GP", "lengthMeters": 4657, "turns": 14, "direction": "CLOCKWISE" }
}
```

`layout` and `externalPlaceId` are optional. Responses only include active (non-`MERGED`) tracks and layouts.

### Admin endpoints (`/admin`)

| Method | Path | Returns |
|---|---|---|
| `POST` | `/admin/venues/{id}/verify`, `/admin/tracks/{id}/verify`, `/admin/layouts/{id}/verify` | 200 venue |
| `POST` | `/admin/venues/{sourceId}/merge-into/{targetId}` (same for `/tracks` and `/layouts`) | 200 target venue · 422 invalid merge · 409 `CATALOG_ITEM_MERGED` |
| `PUT` | `/admin/venues/{venueId}/tracks/{trackId}/default-layout/{layoutId}` | 200 track |
| `PATCH` | `/admin/venues/{venueId}` | 200 venue |
| `PATCH` | `/admin/venues/{venueId}/tracks/{trackId}` | 200 track |
| `PATCH` | `/admin/venues/{venueId}/tracks/{trackId}/layouts/{layoutId}` | 200 layout |

Verify and merge of tracks/layouts take plain ids (the admin may not know the venue).

### Errors

Same `ApiError` shape as `ridei-identity` (`status`, `error`, `message`, `details`, `timestamp`). Conflicts add `code` and `candidates`:

```json
{
  "status": 409,
  "error": "Conflict",
  "message": "A similar venue already exists nearby",
  "details": [],
  "timestamp": "2026-11-04T10:00:00Z",
  "code": "POSSIBLE_DUPLICATE_VENUE",
  "candidates": [
    { "id": "10000000-0000-4000-8000-000000000001", "name": "Circuit de Barcelona-Catalunya",
      "distanceMeters": 812, "similarity": 0.733, "status": "VERIFIED" }
  ]
}
```

| `code` | When |
|---|---|
| `POSSIBLE_DUPLICATE_VENUE` | Nearby venue(s) with similar name; `candidates` sorted by similarity |
| `DUPLICATE_TRACK` / `DUPLICATE_LAYOUT` | `candidates` holds the existing item (`distanceMeters` is `null`) |
| `CATALOG_ITEM_MERGED` | Writing to a merged item; operate on the canonical one |
| `CATALOG_ITEM_ALREADY_EXISTS` | A DB constraint rejected the write even after the retry |
| `CONCURRENT_MODIFICATION` | Optimistic-lock conflict on an admin command |

## 5. Persistence

See [database.md §3](database.md#3-ridei-circuits-database).

## 6. Out of scope (for now)

- Layout geometry (`LINESTRING`) and GPS-based layout detection — a new nullable `geometry` column on `layout` plus a new use case; nothing in the model prevents it (PostGIS is already installed).
- Real Google Places integration — only the client-provided id is stored.
