# Testing

## Strategy

The project applies a **unit-test-first** approach to the domain and application layers. All unit tests are fast, isolated, and have no external dependencies — no database, no Spring context, no HTTP stack.

Tests are written with **JUnit 5** and **Mockito**.

---

## Test structure

```
ridei-identity/src/test/java/com/ridei/identity/
├── domain/
│   └── model/
│       ├── EmailTest.java          ← Value Object validation
│       ├── UsernameTest.java       ← Value Object validation
│       └── UserTest.java          ← Aggregate root invariants
└── application/
    └── RegisterUserServiceTest.java ← Use Case orchestration
```

---

## Domain model tests

### `EmailTest`

File: `domain/model/EmailTest.java`

Tests the `Email` value object's validation logic in isolation.

| Scenario | Expected outcome |
|---|---|
| Valid email (`user@example.com`) | Object created successfully |
| Missing `@` | `IllegalArgumentException` |
| Missing domain (no `.` after `@`) | `IllegalArgumentException` |
| Contains whitespace | `IllegalArgumentException` |
| Empty string | `IllegalArgumentException` |
| `null` | `IllegalArgumentException` |

Uses `@ParameterizedTest` with `@ValueSource` to test multiple invalid cases concisely.

---

### `UsernameTest`

File: `domain/model/UsernameTest.java`

Tests the `Username` value object's validation logic.

| Scenario | Expected outcome |
|---|---|
| Valid username (`@crazyRider69`) | Object created successfully |
| No leading `@` | `IllegalArgumentException` |
| Part after `@` shorter than 3 chars | `IllegalArgumentException` |
| Part after `@` longer than 30 chars | `IllegalArgumentException` |
| Contains spaces | `IllegalArgumentException` |
| Contains invalid characters | `IllegalArgumentException` |

Uses `@ParameterizedTest` with `@ValueSource` for the invalid-input cases.

---

### `UserTest`

File: `domain/model/UserTest.java`

Tests the `User` aggregate root's factory method invariants.

| Scenario | Expected outcome |
|---|---|
| Valid registration data | `User` created with `status = PENDING_VERIFICATION` |
| User under 16 years old | `MinimumAgeNotMetException` thrown |
| Password field | Stores the provided hash, not the raw password string |

These tests call `User.register(command, passwordHash)` directly — no mocks, no Spring, no database.

---

## Application layer tests

### `RegisterUserServiceTest`

File: `application/RegisterUserServiceTest.java`

Tests `RegisterUserService` in full isolation. All outbound ports are replaced with Mockito mocks.

**Test setup:**

```java
@Mock UserRepositoryPort userRepository;
@Mock EventPublisherPort eventPublisher;
@Mock PasswordHasherPort passwordHasher;

RegisterUserService service = new RegisterUserService(userRepository, eventPublisher, passwordHasher);
```

**Test cases:**

| Scenario | Mocked behaviour | Expected outcome |
|---|---|---|
| Valid registration | Email/username do not exist; hasher returns a hash | Returns a `UserId`; `save()` called once; `publish()` called once |
| Email already registered | `existsByEmail()` returns `true` | `EmailAlreadyRegisteredException` thrown; `save()` never called |
| Username already taken | `existsByUsername()` returns `true` | `UsernameAlreadyTakenException` thrown; `save()` never called |
| Password hashing | Hasher returns `"hashed"` | The stored `passwordHash` equals `"hashed"` (not the raw password) |

These tests verify:
- The use case orchestration sequence is correct.
- Business rule checks happen before persistence.
- The password is hashed before being passed to the domain aggregate.
- Domain events are published after a successful save.

---

## Running the tests

```bash
# Run all tests
./mvnw test

# Run tests for a specific module
./mvnw test -pl ridei-identity

# Run a specific test class
./mvnw test -pl ridei-identity -Dtest=RegisterUserServiceTest
```

---

## What is NOT tested (and why)

| Area | Reason |
|---|---|
| `UserController` | Integration/slice tests not yet written; the controller is thin and delegates immediately to the use case |
| `UserPersistenceAdapter` | Would require an in-memory or Testcontainers database; planned for a future iteration |
| `BCryptPasswordHasherAdapter` | Trivial delegation to `BCryptPasswordEncoder`; no custom logic |
| `GlobalExceptionHandler` | Best verified with a Spring MVC test slice (`@WebMvcTest`) — planned |

---

## Testing philosophy

- Domain and application tests are pure unit tests — they run in milliseconds with no external processes.
- Infrastructure adapters (persistence, controllers) are best tested with integration or slice tests that start a real Spring context and, for persistence, a real database (e.g. via Testcontainers).
- The hexagonal architecture makes this boundary explicit: domain/application tests never need Spring or Docker; infrastructure tests do.
