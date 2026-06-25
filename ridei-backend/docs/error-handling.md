# Error Handling

The project uses a layered error handling strategy where each layer is responsible for a specific class of errors. All errors are ultimately translated into a uniform `ApiError` JSON response by a single centralised handler.

---

## Error response model — `ApiError`

Every error response, regardless of origin, uses the following structure:

```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Email already registered: rider@example.com",
  "details": [],
  "timestamp": "2025-06-25T12:00:00.000000Z"
}
```

| Field | Type | Notes |
|---|---|---|
| `status` | `int` | The HTTP status code |
| `error` | `string` | A short category label |
| `message` | `string` | A human-readable explanation |
| `details` | `string[]` | Only populated for validation errors (`400`) |
| `timestamp` | `string` | ISO-8601 UTC timestamp, formatted by Jackson |

---

## Exception hierarchy

```
RuntimeException
├── IllegalArgumentException              ← Value Object validation failure
└── (domain exceptions)
    ├── EmailAlreadyRegisteredException   ← Business rule: email uniqueness
    ├── UsernameAlreadyTakenException     ← Business rule: username uniqueness
    └── MinimumAgeNotMetException         ← Business rule: user must be ≥ 16

MethodArgumentNotValidException           ← Spring: Bean Validation (@Valid) failure
Exception                                 ← Catch-all for unexpected errors
```

---

## Validation layers and error sources

Errors can originate from four distinct layers, each handled differently:

### Layer 1 — HTTP request validation (`@Valid`)

**Where:** Spring MVC, before the controller method executes.

**When:** Any `@NotBlank`, `@Size`, `@Email`, `@Pattern`, `@AssertTrue`, `@Past`, `@NotNull` constraint on `RegisterRequestDTO` fails.

**Handler:** `GlobalExceptionHandler.handleValidationErrors(MethodArgumentNotValidException)`

**HTTP status:** `400 Bad Request`

**Response shape:**
```json
{
  "status": 400,
  "error": "Validation failed",
  "message": "One or more fields are invalid",
  "details": [
    "Email is required",
    "Password must be at least 8 characters"
  ],
  "timestamp": "..."
}
```

All field errors are collected into the `details` array. The client receives all validation failures in a single response.

---

### Layer 2 — Value Object validation (`IllegalArgumentException`)

**Where:** `RegisterRequestDTO.toCommand()` — when constructing domain Value Objects from raw strings.

**When:** A value object constructor rejects the input (e.g. `new Email(null)`, `new PhoneNumber("abc")`).

**Handler:** `GlobalExceptionHandler.handleIllegalArgument(IllegalArgumentException)`

**HTTP status:** `400 Bad Request`

**Response shape:**
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "<value object rejection message>",
  "details": [],
  "timestamp": "..."
}
```

---

### Layer 3 — Domain rule violations (domain exceptions)

**Where:** `RegisterUserService` and `User.register()`.

**When:** Business rules are violated after passing all structural validation.

| Exception | Trigger | Status |
|---|---|---|
| `EmailAlreadyRegisteredException` | Email exists in repository | `409 Conflict` |
| `UsernameAlreadyTakenException` | Username exists in repository | `409 Conflict` |
| `MinimumAgeNotMetException` | User is younger than 16 years | `422 Unprocessable Entity` |

**Handlers:** Dedicated `@ExceptionHandler` methods in `GlobalExceptionHandler`.

---

### Layer 4 — Unexpected errors (catch-all)

**Where:** Anywhere in the stack.

**When:** Any `Exception` not covered by the specific handlers above (e.g. a database connection failure, a null pointer, an unexpected runtime exception).

**Handler:** `GlobalExceptionHandler.handleUnexpected(Exception)`

**HTTP status:** `500 Internal Server Error`

**Response shape:**
```json
{
  "status": 500,
  "error": "Internal Server Error",
  "message": "An unexpected error occurred",
  "details": [],
  "timestamp": "..."
}
```

The full stack trace is logged at `ERROR` level via SLF4J. Internal details are never exposed in the response body.

---

## HTTP status code mapping

| HTTP Status | Scenario |
|---|---|
| `201 Created` | Successful registration |
| `400 Bad Request` | Bean Validation failure, Value Object validation failure |
| `409 Conflict` | Email or username already taken |
| `422 Unprocessable Entity` | Business rule violation (minimum age) |
| `500 Internal Server Error` | Any unexpected, unhandled exception |

---

## `GlobalExceptionHandler`

File: `infrastructure/adapter/in/rest/GlobalExceptionHandler.java`

The single `@RestControllerAdvice` class that handles all exception-to-response translation. Its responsibilities:

1. Collect all field errors from `MethodArgumentNotValidException` and format them into the `details` list.
2. Map each domain exception to the appropriate HTTP status and error message.
3. Log unexpected exceptions at `ERROR` level before returning a safe 500 response.

This centralised approach ensures that:
- No business logic needs to know about HTTP status codes.
- All error responses share the same `ApiError` structure.
- No stack trace or internal state leaks to the client.
