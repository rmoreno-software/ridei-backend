# API Reference

This document covers the `ridei-identity` registration endpoint in detail, plus authentication and the `ridei-garage` API added this week. It does **not** yet cover every `ridei-identity` endpoint (login, onboarding, password reset, email verification, profile picture upload) — those exist and work but haven't been written up here yet.

Base URLs:
- `ridei-identity`: `http://localhost:8081` (local), `https://api.rideiapp.com/api/v1/identity` (prod, via Cloudflare Tunnel)
- `ridei-garage`: `http://localhost:8082` (local), `https://api.rideiapp.com/api/v1/garage` (prod, via Cloudflare Tunnel)

All responses use `Content-Type: application/json`.

---

## Authentication

`ridei-identity` issues JSON Web Tokens signed with **ES256** (asymmetric EC P-256) on successful login. `ridei-garage`, and any future service, verifies those tokens using only the corresponding public key — it never issues tokens of its own. See [architecture.md § Multi-service concerns](architecture.md#multi-service-concerns) for why this is asymmetric rather than a shared secret.

Send the access token on every protected request:
```
Authorization: Bearer <access-token>
```

Two token types are issued, distinguished by a `type` claim (`access` / `refresh`) so one can never be used in place of the other:

| Token | Lifetime | Claims | Used for |
|---|---|---|---|
| Access token | short-lived | `sub` (userId), `role`, `type=access`, `tv` (token version) | `Authorization: Bearer` header on every protected request |
| Refresh token | long-lived | `sub` (userId), `type=refresh`, `tv` | Exchanged for a new access token via the refresh endpoint |

`tv` (token version) is bumped every time the user changes their password, which immediately invalidates every previously-issued token for that account — both services check it against the current value stored on the user before trusting a token.

The `/api/v1/users/register` endpoint (below) is **publicly accessible** and requires no credentials. All other `ridei-identity` and `ridei-garage` endpoints require a valid access token.

---

## `ridei-identity` endpoints

### `POST /api/v1/users/register`

Registers a new user account.

**Request body**

```json
{
  "email": "rider@example.com",
  "password": "securePassword123",
  "username": "@johnDoe",
  "firstName": "John",
  "lastName": "Doe",
  "gender": "MALE",
  "phoneNumber": "+34612345678",
  "dateOfBirth": "1990-05-15",
  "countryCode": "ES",
  "documentType": "NATIONAL_ID",
  "documentNumber": "12345678A",
  "role": "RIDER",
  "termsAccepted": true
}
```

**Field reference**

| Field | Type | Required | Constraints |
|---|---|---|---|
| `email` | `string` | Yes | Valid email format |
| `password` | `string` | Yes | Minimum 8 characters |
| `username` | `string` | Yes | `^@[a-zA-Z0-9_.]{3,30}$` |
| `firstName` | `string` | Yes | Non-blank |
| `lastName` | `string` | Yes | Non-blank |
| `gender` | `string` | No | `MALE`, `FEMALE`, `OTHER` |
| `phoneNumber` | `string` | Yes | E.164 format (e.g. `+34612345678`) |
| `dateOfBirth` | `string` (ISO date) | Yes | Must be in the past; user must be ≥ 16 years old |
| `countryCode` | `string` | Yes | ISO 3166-1 alpha-2 (exactly 2 characters, e.g. `ES`) |
| `documentType` | `string` | Yes | `NATIONAL_ID`, `PASSPORT`, `RESIDENCE_PERMIT`, `DRIVER_LICENSE` |
| `documentNumber` | `string` | Yes | Non-blank |
| `role` | `string` | Yes | `RIDER`, `ORGANIZER` |
| `termsAccepted` | `boolean` | Yes | Must be `true` |

---

**Responses**

#### `201 Created` — Registration successful

```json
{
  "userId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "message": "User registered successfully. Please verify your email."
}
```

| Field | Type | Description |
|---|---|---|
| `userId` | `string` (UUID) | The ID of the newly created user |
| `message` | `string` | Confirmation message |

---

#### `400 Bad Request` — Validation error

Returned when one or more request fields fail Bean Validation constraints.

```json
{
  "status": 400,
  "error": "Validation failed",
  "message": "One or more fields are invalid",
  "details": [
    "Email is required",
    "Password must be at least 8 characters",
    "Username must start with @ followed by 3-30 alphanumeric characters"
  ],
  "timestamp": "2025-01-15T10:30:00.123456Z"
}
```

Also returned when a value object constructor rejects the input (e.g. invalid phone number format that passes DTO validation but fails domain validation).

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid phone number format",
  "details": [],
  "timestamp": "2025-01-15T10:30:00.123456Z"
}
```

---

#### `409 Conflict` — Email already registered

```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Email already registered: rider@example.com",
  "details": [],
  "timestamp": "2025-01-15T10:30:00.123456Z"
}
```

---

#### `409 Conflict` — Username already taken

```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Username already taken: @johnDoe",
  "details": [],
  "timestamp": "2025-01-15T10:30:00.123456Z"
}
```

---

#### `422 Unprocessable Entity` — Minimum age not met

```json
{
  "status": 422,
  "error": "Unprocessable Entity",
  "message": "User must be at least 16 years old",
  "details": [],
  "timestamp": "2025-01-15T10:30:00.123456Z"
}
```

---

#### `500 Internal Server Error` — Unexpected error

```json
{
  "status": 500,
  "error": "Internal Server Error",
  "message": "An unexpected error occurred",
  "details": [],
  "timestamp": "2025-01-15T10:30:00.123456Z"
}
```

The full stack trace is logged server-side at `ERROR` level. The response body intentionally does not expose internal details.

---

## Error response schema

All error responses share the `ApiError` schema:

| Field | Type | Description |
|---|---|---|
| `status` | `integer` | HTTP status code |
| `error` | `string` | Short error category |
| `message` | `string` | Human-readable error description |
| `details` | `string[]` | Field-level messages (populated only for validation errors) |
| `timestamp` | `string` (ISO-8601) | UTC timestamp of the error |

---

## Example: full registration request (cURL)

```bash
curl -X POST http://localhost:8081/api/v1/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "rider@example.com",
    "password": "securePassword123",
    "username": "@johnDoe",
    "firstName": "John",
    "lastName": "Doe",
    "gender": "MALE",
    "phoneNumber": "+34612345678",
    "dateOfBirth": "1990-05-15",
    "countryCode": "ES",
    "documentType": "NATIONAL_ID",
    "documentNumber": "12345678A",
    "role": "RIDER",
    "termsAccepted": true
  }'
```

---

## `ridei-garage` endpoints

All endpoints below require `Authorization: Bearer <access-token>`. The owning user is always taken from the token's `sub` claim — it is never accepted as a request field, so one user can never register or list another user's motorbikes by manipulating the request body.

### `POST /api/v1/garage/motorbikes`

Registers a motorbike owned by the authenticated user.

**Request body**

```json
{
  "brand": "Honda",
  "model": "CB500F",
  "year": 2021,
  "displacementCc": 471,
  "weightKg": 192.50,
  "acquisitionDate": "2021-06-15"
}
```

**Field reference**

| Field | Type | Required | Constraints |
|---|---|---|---|
| `brand` | `string` | Yes | Non-blank, max 100 characters |
| `model` | `string` | Yes | Non-blank, max 100 characters |
| `year` | `integer` | Yes | ≥ 1885 (year the first motorbike was built) |
| `displacementCc` | `integer` | No | Positive if present |
| `weightKg` | `number` | No | Positive if present |
| `acquisitionDate` | `string` (ISO date) | No | Must not be in the future |

**Responses**

#### `201 Created`

```json
{
  "id": "3f2504e0-4f89-11d3-9a0c-0305e82c3301",
  "brand": "Honda",
  "model": "CB500F",
  "year": 2021,
  "displacementCc": 471,
  "weightKg": 192.50,
  "acquisitionDate": "2021-06-15",
  "disposalDate": null,
  "photoUrl": null,
  "createdAt": "2026-09-20T10:15:30Z"
}
```

#### `400 Bad Request` — validation error (DTO or domain)

```json
{
  "status": 400,
  "error": "Validation failed",
  "message": "One or more fields are invalid",
  "details": [
    "Brand is required",
    "Year must be 1885 or later"
  ]
}
```

Domain-level rejections (e.g. a value object constructor throwing `IllegalArgumentException`) return the same `400` shape with an empty `details` array and the domain message in `message`.

#### `401 Unauthorized` — missing/invalid/expired token, or a refresh token used as an access token

No body; enforced by `SecurityConfig`/`JwtAuthenticationFilter` before the request reaches the controller.

---

### `GET /api/v1/garage/motorbikes`

Lists every motorbike owned by the authenticated user, most recently created first.

**Responses**

#### `200 OK`

```json
[
  {
    "id": "3f2504e0-4f89-11d3-9a0c-0305e82c3301",
    "brand": "Honda",
    "model": "CB500F",
    "year": 2021,
    "displacementCc": 471,
    "weightKg": 192.50,
    "acquisitionDate": "2021-06-15",
    "disposalDate": null,
    "photoUrl": null,
    "createdAt": "2026-09-20T10:15:30Z"
  }
]
```

An empty array (`[]`) is returned for a user with no motorbikes yet — never a `404`.

`disposalDate` non-null indicates the motorbike has been retired/sold (`Motorbike.isRetired()` in the domain); there is currently no endpoint to set it (see [deployment.md § Pending](deployment.md#9-pending--known-gaps)).

#### `401 Unauthorized`

Same as above.

---

## `ridei-garage` error response schema

Same `ApiError` shape as `ridei-identity`, minus the `timestamp` field:

| Field | Type | Description |
|---|---|---|
| `status` | `integer` | HTTP status code |
| `error` | `string` | Short error category |
| `message` | `string` | Human-readable error description |
| `details` | `string[]` | Field-level messages (populated only for validation errors) |
