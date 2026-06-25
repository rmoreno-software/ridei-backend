# API Reference

Base URL: `http://localhost:8081`

All responses use `Content-Type: application/json`.

---

## Authentication

The `/api/v1/users/register` endpoint is **publicly accessible** and requires no credentials.

All other endpoints require authentication (configured in `SecurityConfig`). Authentication scheme will be documented when JWT support is implemented.

---

## Endpoints

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
