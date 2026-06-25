# Infrastructure Layer

The infrastructure layer adapts the application to external systems: HTTP, the database, password hashing, and event publishing. All adapters implement port interfaces defined in the domain, so the domain is never aware of the technologies used.

Package root: `com.ridei.identity.infrastructure`

---

## Inbound Adapters — REST

These adapters receive HTTP requests and translate them into use case invocations.

### `UserController`

File: `infrastructure/adapter/in/rest/UserController.java`

Spring `@RestController` mounted at `/api/v1/users`.

#### Endpoints

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/v1/users/register` | Register a new user |

See the [API Reference](api-reference.md) for full request/response details.

The controller:
1. Receives a `RegisterRequestDTO` body validated with `@Valid`.
2. Calls `RegisterRequestDTO.toCommand()` to build a `RegisterUserCommand` with domain Value Objects.
3. Calls `registerUserUseCase.register(command)`.
4. Returns a `RegisterResponseDTO` with the new user's ID and a confirmation message.

The controller depends only on the `RegisterUserUseCase` **interface** — never on `RegisterUserService` directly.

---

### `RegisterRequestDTO`

File: `infrastructure/adapter/in/rest/RegisterRequestDTO.java`

Carries the raw JSON body of a registration request. All fields are validated with Jakarta Bean Validation annotations before the controller method body executes.

| Field | Constraints |
|---|---|
| `email` | `@NotBlank`, `@Email` |
| `password` | `@NotBlank`, `@Size(min = 8)` |
| `username` | `@NotBlank`, `@Pattern(^@[a-zA-Z0-9_.]{3,30}$)` |
| `firstName` | `@NotBlank` |
| `lastName` | `@NotBlank` |
| `gender` | Optional |
| `phoneNumber` | `@NotBlank` |
| `dateOfBirth` | `@NotNull`, `@Past` |
| `countryCode` | `@NotBlank`, `@Size(min=2, max=2)` |
| `documentType` | `@NotNull` |
| `documentNumber` | `@NotBlank` |
| `role` | `@NotNull` |
| `termsAccepted` | `@AssertTrue` |

The `toCommand()` method converts raw strings to domain Value Objects (`Email`, `Username`, `PhoneNumber`, `IdentityDocument`). If any value object constructor throws an `IllegalArgumentException` (e.g. invalid phone number format), it is caught by `GlobalExceptionHandler` and returned as a `400 Bad Request`.

---

### `RegisterResponseDTO`

File: `infrastructure/adapter/in/rest/RegisterResponseDTO.java`

The JSON body returned after a successful registration.

| Field | Type | Example value |
|---|---|---|
| `userId` | `String` | `"3fa85f64-5717-4562-b3fc-2c963f66afa6"` |
| `message` | `String` | `"User registered successfully. Please verify your email."` |

---

### `GlobalExceptionHandler`

File: `infrastructure/adapter/in/rest/GlobalExceptionHandler.java`

A Spring `@RestControllerAdvice` that intercepts exceptions thrown anywhere in the request handling chain and converts them into a consistent `ApiError` JSON response.

See [Error Handling](error-handling.md) for the full exception-to-HTTP mapping.

---

### `ApiError`

File: `infrastructure/adapter/in/rest/exception/ApiError.java`

Standardised error response body returned on every error.

| Field | Type | Description |
|---|---|---|
| `status` | `int` | HTTP status code (e.g. `400`, `409`) |
| `error` | `String` | Short error type (e.g. `"Conflict"`, `"Validation failed"`) |
| `message` | `String` | Human-readable description of the error |
| `details` | `List<String>` | Field-level validation messages (only for `400` validation errors) |
| `timestamp` | `String` | ISO-8601 UTC timestamp of when the error occurred |

---

## Outbound Adapters — Persistence

### `UserJpaEntity`

File: `infrastructure/adapter/out/persistence/UserJpaEntity.java`

The JPA `@Entity` that maps to the `users` table. It is a flat representation of the `User` aggregate — value objects are stored as their primitive equivalents (e.g. `Email` → `String`), and enums are stored as strings.

It also holds `@OneToOne` relationships (with `CascadeType.ALL` and `orphanRemoval = true`) to:
- `RiderProfileJpaEntity` (table: `rider_profiles`)
- `OrganizerAccountJpaEntity` (table: `organizer_accounts`)

Two conversion methods keep the JPA entity decoupled from the domain:

| Method | Direction | Description |
|---|---|---|
| `fromDomain(User)` | Domain → JPA | Converts a `User` aggregate to a `UserJpaEntity` for persistence |
| `toDomain()` | JPA → Domain | Reconstructs a `User` aggregate by calling `User.reconstitute(...)` |

---

### `RiderProfileJpaEntity`

File: `infrastructure/adapter/out/persistence/RiderProfileJpaEntity.java`

Maps to the `rider_profiles` table. Holds rider-specific data:

| Column | Description |
|---|---|
| `user_id` | Primary key, foreign key to `users.id` |
| `profile_type` | Type of rider profile |
| `racing_license_number` | Official racing licence number |

---

### `OrganizerAccountJpaEntity`

File: `infrastructure/adapter/out/persistence/OrganizerAccountJpaEntity.java`

Maps to the `organizer_accounts` table. Holds organizer-specific data:

| Column | Description |
|---|---|
| `user_id` | Primary key, foreign key to `users.id` |
| `legal_name` | Registered legal name of the organizer |
| `tax_id` | Tax identification number |
| `iban` | Bank account number for payouts |

---

### `UserJpaRepository`

File: `infrastructure/adapter/out/persistence/UserJpaRepository.java`

Standard Spring Data `JpaRepository<UserJpaEntity, UUID>` with two custom query methods:

```java
boolean existsByEmail(String email);
boolean existsByUsername(String username);
```

Spring Data derives the SQL from the method names — no `@Query` annotation needed.

---

### `UserPersistenceAdapter`

File: `infrastructure/adapter/out/persistence/UserPersistenceAdapter.java`

Implements `UserRepositoryPort`. Bridges the gap between the domain port and the Spring Data repository.

| Port method | Adapter behaviour |
|---|---|
| `save(User)` | Calls `UserJpaEntity.fromDomain(user)` then `jpaRepository.save(entity)` |
| `existsByEmail(Email)` | Extracts the string value and delegates to `jpaRepository.existsByEmail(String)` |
| `existsByUsername(Username)` | Extracts the string value and delegates to `jpaRepository.existsByUsername(String)` |

---

## Outbound Adapters — Security

### `BCryptPasswordHasherAdapter`

File: `infrastructure/adapter/out/security/BCryptPasswordHasherAdapter.java`

Implements `PasswordHasherPort` using Spring Security's `BCryptPasswordEncoder` with the default cost factor (10 rounds).

```java
public String hash(String rawPassword) {
    return encoder.encode(rawPassword);
}
```

The raw password is hashed before being passed to `User.register()`, so the domain aggregate never sees plaintext passwords.

---

## Outbound Adapters — Messaging

### `LogEventPublisherAdapter`

File: `infrastructure/adapter/out/messaging/LogEventPublisherAdapter.java`

Implements `EventPublisherPort` as a **placeholder**. The current implementation logs the event to SLF4J:

```
[EVENT] UserRegistered - userId: <id>, role: <role>, occurredAt: <timestamp>
```

This adapter is designed to be replaced by a real message broker adapter (e.g. Kafka, RabbitMQ, Amazon SQS) without touching the domain or application layers — only the adapter changes, and the port interface remains the same.

---

## Configuration

### `BeanConfig`

File: `infrastructure/config/BeanConfig.java`

A Spring `@Configuration` class that manually wires application services with their dependencies. This keeps the application layer free of Spring annotations.

```java
@Bean
public RegisterUserUseCase registerUserUseCase(
    UserRepositoryPort userRepository,
    EventPublisherPort eventPublisher,
    PasswordHasherPort passwordHasher
) {
    return new RegisterUserService(userRepository, eventPublisher, passwordHasher);
}
```

The controller receives a `RegisterUserUseCase` interface, not the concrete class.

---

### `SecurityConfig`

File: `infrastructure/config/SecurityConfig.java`

Spring Security `@Configuration` that defines the security filter chain:

| Rule | Detail |
|---|---|
| CSRF | Disabled (stateless API; JWT-based auth planned) |
| `/api/v1/users/register` | Publicly accessible — no authentication required |
| All other endpoints | Authentication required |

The security configuration is intentionally minimal at this stage. Authentication/authorisation via JWT tokens is planned for a future iteration.
