# ridei-circuits — Guía de integración para el frontend

Catálogo de circuitos: **venue** (el complejo) → **track** (la pista de una disciplina) → **layout** (el trazado exacto).

La especificación completa, para generar el cliente o importarla en Postman o Insomnia, está en [`openapi/ridei-circuits.json`](openapi/ridei-circuits.json) (OpenAPI 3). Esta guía explica cómo usarla.

---

## 1. Conexión

| | |
|---|---|
| Base URL (producción) | `https://api.rideiapp.com/api/v1/circuits` |
| Base URL (local) | `http://localhost:8083/api/v1/circuits` (Swagger UI: `http://localhost:8083/swagger-ui.html`) |
| Autenticación | `Authorization: Bearer <accessToken>` en **todas** las peticiones. Es el access token de ridei-identity (login / refresh). |
| Formato | JSON UTF-8. Ids: UUID en texto. Fechas: ISO-8601 en UTC (`2026-11-04T10:00:00Z`). Distancias en metros. |
| Admin | Las rutas `/admin/**` exigen un usuario con rol `ADMIN` (en la app, solo para herramientas internas). |

Los campos opcionales llegan como `null`, no se omiten: `externalPlaceId`, `turns`, `distanceMeters` y `similarity`.

---

## 2. Modelo de datos (respuestas)

```jsonc
// VenueResponseDTO
{
  "id": "10000000-0000-4000-8000-000000000001",
  "name": "Circuit de Barcelona-Catalunya",
  "countryCode": "ES",                 // ISO 3166-1 alfa-2
  "latitude": 41.57,
  "longitude": 2.26111,
  "externalPlaceId": null,             // id de Google Places/Mapbox si se envió
  "status": "VERIFIED",                // PENDING_REVIEW | VERIFIED
  "tracks": [ /* TrackResponseDTO */ ],
  "createdAt": "2026-11-04T00:00:00Z",
  "updatedAt": "2026-11-04T00:00:00Z"
}

// TrackResponseDTO
{
  "id": "20000000-0000-4000-8000-000000000001",
  "discipline": "ROAD_RACING",         // ROAD_RACING | KARTING | MOTOCROSS | SUPERMOTO | ENDURO | FLAT_TRACK
  "name": "Velocidad",
  "status": "VERIFIED",
  "layouts": [ /* LayoutResponseDTO */ ]
}

// LayoutResponseDTO
{
  "id": "30000000-0000-4000-8000-000000000001",
  "name": "GP",
  "lengthMeters": 4657,
  "turns": 14,                         // puede ser null (desconocido)
  "direction": "CLOCKWISE",            // CLOCKWISE | COUNTER_CLOCKWISE
  "isDefault": true,                   // exactamente uno por track (si el track tiene layouts)
  "status": "VERIFIED"
}
```

- **`status`**: lo que propone un usuario entra como `PENDING_REVIEW` y se puede usar en el acto. Recomendación: mostrar una etiqueta "Pendiente de verificación", sin bloquear nada. Un admin lo pasa a `VERIFIED`.
- Las respuestas **nunca** incluyen elementos fusionados (`MERGED`).
- Un track puede no tener layouts (`layouts: []`). Cuando los tiene, preselecciona el que lleva `isDefault: true`.

---

## 3. Endpoints de usuario

### 3.1 Buscar — `GET /venues`

Todos los parámetros son opcionales y se pueden combinar.

| Parámetro | Tipo | Notas |
|---|---|---|
| `q` | string (≤ 150) | Texto libre. Busca palabras del nombre del venue **y de sus tracks**. Ignora mayúsculas, acentos, guiones y palabras genéricas ("circuito", "de"…): `jerez` encuentra "Circuito de Jerez - Ángel Nieto". |
| `country` | string (2) | `ES`, `PT`… |
| `discipline` | enum | Solo venues que tengan un track activo de esa disciplina. |
| `lat`, `lon`, `radiusKm` | number | **Los tres juntos** o ninguno. Radio > 0 y ≤ 500. |
| `page` | int ≥ 0 | Por defecto `0`. |
| `size` | int 1–100 | Por defecto `20`. |

Orden de los resultados: primero por similitud de texto, después por distancia y por último por nombre.

```http
GET /api/v1/circuits/venues?q=jerez&size=10
```
```jsonc
// 200 — PageResponseDTO<VenueSearchItemDTO>
{
  "content": [
    {
      "venue": { /* VenueResponseDTO */ },
      "distanceMeters": null,   // solo si se envió lat/lon/radiusKm
      "similarity": 1.0         // solo si se envió q (0..1)
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 1,
  "totalPages": 1
}
```

Para un buscador "mientras escribes", usa un debounce de unos 300 ms y empieza a buscar a partir de 2 caracteres.

### 3.2 Cerca de mí — `GET /venues/nearby`

| Parámetro | Obligatorio | Notas |
|---|---|---|
| `lat`, `lon` | sí | |
| `radiusKm` | no | Por defecto `50`, máximo `500`. |
| `page`, `size` | no | Como en la búsqueda. |

La respuesta tiene la misma forma que `GET /venues`, ordenada de más cerca a más lejos, con `distanceMeters` relleno y `similarity: null`.

### 3.3 Detalle — `GET /venues/{venueId}`

```jsonc
// 200 — VenueDetailResponseDTO
{
  "requestedId": "aaaaaaaa-…",
  "redirectedFromMerged": false,
  "venue": { /* VenueResponseDTO con tracks y layouts */ }
}
```

> ⚠️ **Ids fusionados.** Si un admin detecta que dos venues son el mismo, los fusiona. El id antiguo **sigue funcionando**: la respuesta trae el venue canónico con `redirectedFromMerged: true`, y `venue.id` es distinto de `requestedId`. **Sustituye en tu caché o almacenamiento local el id antiguo por `venue.id`.** Haz lo mismo con los ids de tracks y layouts que tengas guardados, recargándolos desde este detalle.

`404` si el id no existe; `400` si no es un UUID.

### 3.4 Proponer un circuito — `POST /venues`

```jsonc
{
  "name": "Circuit de Barcelona-Catalunya",   // obligatorio, ≤ 150
  "countryCode": "ES",                        // obligatorio, ISO alfa-2
  "latitude": 41.57,                          // obligatorio, -90..90
  "longitude": 2.26111,                       // obligatorio, -180..180
  "externalPlaceId": "ChIJ…",                 // opcional: place_id de Google Places / Mapbox
  "confirmedNotDuplicate": false,             // opcional, por defecto false (ver flujo)
  "track":  { "discipline": "ROAD_RACING", "name": "Velocidad" },            // obligatorio
  "layout": { "name": "GP", "lengthMeters": 4657, "turns": 14,               // opcional; pasa a ser el default
              "direction": "CLOCKWISE" }
}
```

No envíes el usuario: el backend lo toma del token y, si llega un campo `createdBy`, lo ignora.

| Respuesta | Cuándo | Cuerpo |
|---|---|---|
| **201** | Creado. Cabecera `Location` con la URL del venue. | `{ "outcome": "CREATED", "venue": {…} }` |
| **200** | Ya existía un venue con el mismo `externalPlaceId`. Se devuelve **ese**, aunque se haya enviado `confirmedNotDuplicate: true`. | `{ "outcome": "EXISTING_BY_EXTERNAL_PLACE_ID", "venue": {…} }` |
| **409** `POSSIBLE_DUPLICATE_VENUE` | Hay venues a menos de 2 km con nombre parecido. | `ConflictApiError` con `candidates` (§5) |
| **400** | Validación. | `ApiError` con `details` |

**Flujo recomendado ("¿Es alguno de estos?")**

```
POST /venues (confirmedNotDuplicate: false)
 ├─ 201 → listo, usa venue.id
 ├─ 200 → ya existía (mismo lugar del mapa): usa venue.id, avisa al usuario de que ya estaba en el catálogo
 └─ 409 POSSIBLE_DUPLICATE_VENUE → muestra candidates (nombre, "a 812 m", estado)
       ├─ el usuario elige uno → usa candidate.id (GET /venues/{id} para cargarlo;
       │                          si le falta su pista, añádela con POST /venues/{id}/tracks)
       └─ "No, es otro circuito" → repite el MISMO POST con confirmedNotDuplicate: true → 201
```

Envía `externalPlaceId` siempre que el usuario haya elegido el sitio en el mapa: es la forma más fiable de evitar duplicados.

### 3.5 Añadir una pista a un circuito — `POST /venues/{venueId}/tracks`

```json
{ "discipline": "KARTING", "name": "Karting" }
```

| Respuesta | Cuándo |
|---|---|
| **201** `TrackResponseDTO` | Creada. Un mismo venue puede tener pistas de varias disciplinas, aunque estén pegadas. |
| **409** `DUPLICATE_TRACK` | El venue ya tiene una pista de **esa disciplina** con nombre parecido. `candidates[0]` es la existente: ofrécela. |
| **409** `CATALOG_ITEM_MERGED` | El venue se fusionó: recarga con `GET /venues/{venueId}` y usa el canónico. |
| **404** | El venue no existe. |

### 3.6 Añadir un trazado a una pista — `POST /venues/{venueId}/tracks/{trackId}/layouts`

```json
{ "name": "GP con chicane", "lengthMeters": 4675, "turns": 16, "direction": "CLOCKWISE" }
```

`turns` es opcional. El primer layout de una pista pasa a ser su default.

| Respuesta | Cuándo |
|---|---|
| **201** `LayoutResponseDTO` | Creado. |
| **409** `DUPLICATE_LAYOUT` | Ya existe uno **equivalente**: nombre parecido, **o** longitud a ±25 m con las mismas curvas y el mismo sentido. `candidates[0]` es el existente. |
| **404** | El venue o el track no existen. |

---

## 4. Endpoints de administración (`/admin`, rol `ADMIN`)

| Método y ruta | Devuelve |
|---|---|
| `POST /admin/venues/{venueId}/verify` | `VenueResponseDTO` |
| `POST /admin/tracks/{trackId}/verify` | `VenueResponseDTO` (el venue que lo contiene) |
| `POST /admin/layouts/{layoutId}/verify` | `VenueResponseDTO` |
| `POST /admin/venues/{sourceId}/merge-into/{targetId}` | `VenueResponseDTO` del destino |
| `POST /admin/tracks/{sourceId}/merge-into/{targetId}` | `VenueResponseDTO` del destino (misma disciplina) |
| `POST /admin/layouts/{sourceId}/merge-into/{targetId}` | `VenueResponseDTO` (mismo track) |
| `PUT /admin/venues/{venueId}/tracks/{trackId}/default-layout/{layoutId}` | `TrackResponseDTO` |
| `PATCH /admin/venues/{venueId}` | `VenueResponseDTO`. Cuerpo parcial: `name`, `countryCode`, `latitude` + `longitude` (juntos), `externalPlaceId` |
| `PATCH /admin/venues/{venueId}/tracks/{trackId}` | `TrackResponseDTO`. Cuerpo parcial: `discipline`, `name` |
| `PATCH /admin/venues/{venueId}/tracks/{trackId}/layouts/{layoutId}` | `LayoutResponseDTO`. Cuerpo parcial: `name`, `lengthMeters`, `turns`, `direction` |

En los `PATCH` solo cambian los campos que envíes. `403` si el usuario no es admin; `422` si una fusión no tiene sentido.

---

## 5. Errores

Todos los errores tienen la misma forma que en ridei-identity:

```jsonc
// ApiError
{
  "status": 400,
  "error": "Validation failed",
  "message": "One or more fields are invalid",
  "details": ["Venue name is required", "Latitude must be between -90 and 90"],
  "timestamp": "2026-11-04T10:00:00Z"
}
```

Los **409** añaden `code` y `candidates`:

```jsonc
// ConflictApiError
{
  "status": 409,
  "error": "Conflict",
  "message": "A similar venue already exists nearby",
  "details": [],
  "timestamp": "2026-11-04T10:00:00Z",
  "code": "POSSIBLE_DUPLICATE_VENUE",
  "candidates": [
    {
      "id": "10000000-0000-4000-8000-000000000001",
      "name": "Circuit de Barcelona-Catalunya",
      "distanceMeters": 812,      // null en DUPLICATE_TRACK / DUPLICATE_LAYOUT
      "similarity": 0.733,        // 0..1
      "status": "VERIFIED"
    }
  ]
}
```

| Estado | `code` | Qué hacer en la app |
|---|---|---|
| 400 | — | Mostrar `details` (vienen en inglés; mejor mapearlas a textos propios por campo). |
| 401 | — (sin cuerpo) | Token ausente, caducado **o revocado** (cambio de contraseña). Llama una vez al refresh de identity y reintenta; si el refresh falla, cierra la sesión. |
| 403 | — (sin cuerpo) | Falta el rol admin. |
| 404 | — | El elemento no existe. |
| 409 | `POSSIBLE_DUPLICATE_VENUE` | Flujo "¿Es alguno de estos?" (§3.4). |
| 409 | `DUPLICATE_TRACK` / `DUPLICATE_LAYOUT` | Ofrecer el existente (`candidates[0]`). |
| 409 | `CATALOG_ITEM_MERGED` | Recargar el venue con `GET /venues/{id}` (te redirige al canónico). |
| 409 | `CATALOG_ITEM_ALREADY_EXISTS` / `CONCURRENT_MODIFICATION` | Otra persona escribió a la vez: recargar y volver a intentar. |
| 422 | — | Fusión inválida (admin). |
| 503 | — | ridei-identity no responde para validar el token. **No cierres la sesión:** reintenta con backoff (1 s, 2 s, 4 s…). |
| 500 | — | Error inesperado. |

---

## 6. Ejemplos rápidos

```bash
TOKEN="<accessToken>"
API=https://api.rideiapp.com/api/v1/circuits

curl -H "Authorization: Bearer $TOKEN" "$API/venues?q=montmelo"
curl -H "Authorization: Bearer $TOKEN" "$API/venues/nearby?lat=41.57&lon=2.26&radiusKm=30"
curl -H "Authorization: Bearer $TOKEN" "$API/venues/10000000-0000-4000-8000-000000000001"
curl -X POST -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" "$API/venues" -d '{
  "name": "Circuit d'"'"'Alcarràs", "countryCode": "ES", "latitude": 41.6193, "longitude": 0.3989,
  "track": { "discipline": "MOTOCROSS", "name": "Motocross" } }'
# → 409 POSSIBLE_DUPLICATE_VENUE con el Alcarràs existente: lo correcto es
curl -X POST -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  "$API/venues/10000000-0000-4000-8000-000000000010/tracks" -d '{ "discipline": "MOTOCROSS", "name": "Motocross" }'
```

## 7. Generar el cliente Dart (opcional)

```bash
npx @openapitools/openapi-generator-cli generate -i docs/openapi/ridei-circuits.json -g dart-dio -o ridei_circuits_api
```

Para regenerar el JSON después de cambiar la API:

```bash
./mvnw verify -pl ridei-circuits -Dit.test=OpenApiExportIT -Dtest=skip -Dsurefire.failIfNoSpecifiedTests=false -Dopenapi.export=true
```

Necesita Docker.
