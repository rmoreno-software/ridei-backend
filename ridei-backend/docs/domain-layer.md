# Domain Layer

The domain layer is the heart of the application. It contains pure business logic and has **zero dependencies on frameworks, databases, or HTTP**. Everything in this layer is plain Java.

Package root: `com.ridei.identity.domain`

---

## Aggregate Root — `User`

File: `domain/model/User.java`

`User` is the aggregate root of the Identity bounded context. It encapsulates all data and invariants related to a registered user.

### Fields

| Field | Type | Description |
|---|---|---|
| `id` | `UserId` | Unique identifier (UUID wrapper) |
| `email` | `Email` | Validated email address |
| `passwordHash` | `String` | BCrypt hash — the raw password is never stored |
| `username` | `Username` | Unique handle, must start with `@` |
| `firstName` | `String` | Given name |
| `lastName` | `String` | Family name |
| `gender` | `Gender` | Optional: `MALE`, `FEMALE`, `OTHER` |
| `phoneNumber` | `PhoneNumber` | Validated E.164-style phone number |
| `dateOfBirth` | `LocalDate` | Used for age validation |
| `countryCode` | `String` | ISO 3166-1 alpha-2 country code (e.g. `ES`, `IT`) |
| `identityDocument` | `IdentityDocument` | Document type + number pair |
| `role` | `UserRole` | `RIDER` or `ORGANIZER` |
| `status` | `AccountStatus` | Lifecycle state: `PENDING_VERIFICATION`, `ACTIVE`, `SUSPENDED` |
| `termsAccepted` | `boolean` | Must be `true` for registration to succeed |
| `termsAcceptedAt` | `Instant` | Timestamp of acceptance |
| `createdAt` | `Instant` | Account creation timestamp |

### Factory methods

#### `User.register(RegisterUserCommand, String passwordHash)`

Creates a **new** user from a registration command. This is the only way to create a `User` outside of persistence reconstruction.

Business rules enforced:
- The user must be at least **16 years old**. If `dateOfBirth` is less than 16 years in the past, a `MinimumAgeNotMetException` is thrown.
- `status` is always set to `PENDING_VERIFICATION` — an email verification step is expected downstream.
- `termsAcceptedAt` and `createdAt` are both set to `Instant.now()`.
- A new random `UserId` is generated via `UserId.newId()`.

#### `User.reconstitute(...)`

Reconstructs a `User` from raw persistence values (called by `UserJpaEntity.toDomain()`). This bypasses business rule validation intentionally — the data was already validated when it was first persisted.

---

## Value Objects

Value objects are **immutable** objects that represent a domain concept and carry self-validation in their constructors. Two value objects with the same data are considered equal.

### `UserId`

File: `domain/model/UserId.java`

Wraps a `java.util.UUID`.

| Method | Description |
|---|---|
| `UserId.newId()` | Generates a new random UUID |
| `UserId.of(String)` | Parses a UUID string — throws `IllegalArgumentException` if the format is invalid |
| `value()` | Returns the underlying `UUID` |

---

### `Email`

File: `domain/model/Email.java`

Wraps a validated email string.

Validation rules (applied in constructor):
- Must not be null or blank.
- Must contain `@`.
- Must contain `.` after the `@`.

Throws `IllegalArgumentException` if invalid.

| Method | Description |
|---|---|
| `value()` | Returns the raw email string |

---

### `Username`

File: `domain/model/Username.java`

Represents the user's unique public handle.

Validation rules:
- Must match `^@[a-zA-Z0-9_.]{3,30}$`
- Must start with `@`.
- The part after `@` must be 3–30 characters long and contain only alphanumeric characters, underscores, or dots.

Examples of valid usernames: `@crazyRider69`, `@john.doe`, `@alice_99`

Throws `IllegalArgumentException` if invalid.

---

### `PhoneNumber`

File: `domain/model/PhoneNumber.java`

Represents an international phone number.

Validation rules:
- Must match `^\+?[1-9]\d{6,14}$`
- Optionally starts with `+`.
- 7 to 15 digits total (E.164-compliant range).

Throws `IllegalArgumentException` if invalid.

---

### `IdentityDocument`

File: `domain/model/IdentityDocument.java`

A composite value object holding a `DocumentType` enum and a document number string.

| Component | Type | Description |
|---|---|---|
| `type()` | `DocumentType` | The kind of document |
| `number()` | `String` | The document number |

---

## Enumerations

### `UserRole`

File: `domain/model/UserRole.java`

Defines the two roles a user can hold:

| Value | Description |
|---|---|
| `RIDER` | A motorcycle rider who participates in events |
| `ORGANIZER` | A legal entity that organises and manages events |

Role-specific data is stored in separate tables (`rider_profiles`, `organizer_accounts`).

---

### `AccountStatus`

File: `domain/model/AccountStatus.java`

Lifecycle state of a user account:

| Value | Description |
|---|---|
| `PENDING_VERIFICATION` | Account created; email not yet confirmed |
| `ACTIVE` | Account fully verified and operational |
| `SUSPENDED` | Account temporarily disabled |

New accounts always start as `PENDING_VERIFICATION`.

---

### `Gender`

File: `domain/model/Gender.java`

| Value |
|---|
| `MALE` |
| `FEMALE` |
| `OTHER` |

Optional field — users may omit their gender.

---

### `DocumentType`

File: `domain/model/DocumentType.java`

| Value | Description |
|---|---|
| `NATIONAL_ID` | National identity card |
| `PASSPORT` | Passport |
| `RESIDENCE_PERMIT` | Residence permit |
| `DRIVER_LICENSE` | Driver's licence |

---

## Domain Events

Domain events represent facts that have already occurred inside the domain. They are published after a successful operation to allow other parts of the system to react.

### `UserRegisteredEvent`

File: `domain/event/UserRegisteredEvent.java`

Published by `RegisterUserService` immediately after a user is persisted.

| Field | Type | Description |
|---|---|---|
| `userId` | `UserId` | ID of the newly registered user |
| `role` | `UserRole` | Role the user registered with |
| `occurredAt` | `Instant` | Timestamp when the event was raised |

The current implementation logs the event via `LogEventPublisherAdapter`. The infrastructure is designed to swap this for a real message broker (e.g. Kafka, RabbitMQ) without touching the domain.

---

## Domain Exceptions

These exceptions carry semantic meaning and are mapped to specific HTTP responses by `GlobalExceptionHandler`.

| Exception | Trigger | HTTP Status |
|---|---|---|
| `EmailAlreadyRegisteredException` | An account with the given email already exists | `409 Conflict` |
| `UsernameAlreadyTakenException` | The requested username is already in use | `409 Conflict` |
| `MinimumAgeNotMetException` | The user is younger than 16 years | `422 Unprocessable Entity` |

All three extend `RuntimeException`. They carry a human-readable message that is forwarded directly to the API error response.

---

## Ports

Ports are **Java interfaces** defined in the domain layer. They represent the contracts through which the domain interacts with the outside world. Concrete implementations live in the infrastructure layer.

### Inbound Ports (Use Cases)

The domain defines what operations it exposes to external actors.

#### `RegisterUserUseCase`

File: `domain/port/in/RegisterUserUseCase.java`

```java
public interface RegisterUserUseCase {
    UserId register(RegisterUserCommand command);
}
```

### Outbound Ports

The domain defines what it needs from external systems.

#### `UserRepositoryPort`

File: `domain/port/out/UserRepositoryPort.java`

```java
public interface UserRepositoryPort {
    void save(User user);
    boolean existsByEmail(Email email);
    boolean existsByUsername(Username username);
}
```

#### `PasswordHasherPort`

File: `domain/port/out/PasswordHasherPort.java`

```java
public interface PasswordHasherPort {
    String hash(String rawPassword);
}
```

#### `EventPublisherPort`

File: `domain/port/out/EventPublisherPort.java`

```java
public interface EventPublisherPort {
    void publish(UserRegisteredEvent event);
}
```
