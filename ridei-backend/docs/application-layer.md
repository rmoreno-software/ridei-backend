# Application Layer

The application layer sits between the domain and the infrastructure. Its sole responsibility is **orchestrating domain objects to fulfill use cases**. It contains no business rules of its own — all validation and logic lives in the domain.

Package root: `com.ridei.identity.application`

---

## Commands

Commands are **immutable Java Records** that carry the data required to invoke a use case. They are constructed by inbound adapters (e.g. the REST controller) and passed into application services.

### `RegisterUserCommand`

File: `application/RegisterUserCommand.java`

Carries all the data needed to register a new user. Fields are already typed as domain Value Objects — the conversion from raw strings happens in `RegisterRequestDTO.toCommand()`.

| Field | Type | Description |
|---|---|---|
| `email` | `Email` | Validated email value object |
| `password` | `String` | Raw password (hashed by the service before storage) |
| `username` | `Username` | Validated username value object |
| `firstName` | `String` | Given name |
| `lastName` | `String` | Family name |
| `gender` | `Gender` | Optional gender enum |
| `phoneNumber` | `PhoneNumber` | Validated phone number value object |
| `dateOfBirth` | `LocalDate` | Date of birth (used for age check in the domain) |
| `countryCode` | `String` | ISO 3166-1 alpha-2 country code |
| `identityDocument` | `IdentityDocument` | Document type and number |
| `role` | `UserRole` | `RIDER` or `ORGANIZER` |
| `termsAccepted` | `boolean` | Must be `true` |

Because `RegisterUserCommand` is a Java Record, it is inherently immutable — there are no setters and all fields are set at construction time.

---

## Application Services

Application services implement the inbound port interfaces defined in the domain. They coordinate domain objects, outbound ports, and domain events.

### `RegisterUserService`

File: `application/RegisterUserService.java`

Implements: `RegisterUserUseCase`

#### Dependencies (injected via constructor)

| Dependency | Type | Role |
|---|---|---|
| `userRepository` | `UserRepositoryPort` | Persists the user and checks uniqueness |
| `eventPublisher` | `EventPublisherPort` | Publishes the `UserRegisteredEvent` |
| `passwordHasher` | `PasswordHasherPort` | Hashes the raw password before storage |

#### Execution flow

```
register(RegisterUserCommand command)
│
├── 1. existsByEmail(command.email())
│        └── throws EmailAlreadyRegisteredException if true
│
├── 2. existsByUsername(command.username())
│        └── throws UsernameAlreadyTakenException if true
│
├── 3. passwordHasher.hash(command.password())
│        └── returns BCrypt hash string
│
├── 4. User.register(command, passwordHash)
│        └── enforces age ≥ 16, sets status = PENDING_VERIFICATION
│        └── throws MinimumAgeNotMetException if age check fails
│
├── 5. userRepository.save(user)
│
├── 6. eventPublisher.publish(new UserRegisteredEvent(...))
│
└── 7. return user.getId()
```

#### Return value

Returns the `UserId` of the newly created user. The inbound adapter (controller) extracts the UUID string and includes it in the HTTP response.

#### Error conditions

| Condition | Exception thrown | HTTP status |
|---|---|---|
| Email already registered | `EmailAlreadyRegisteredException` | `409 Conflict` |
| Username already taken | `UsernameAlreadyTakenException` | `409 Conflict` |
| User is under 16 years old | `MinimumAgeNotMetException` | `422 Unprocessable Entity` |

#### No Spring annotations

`RegisterUserService` has no `@Service` or any other Spring annotation. It is a plain Java class. Spring's dependency injection is configured manually in `BeanConfig`, which keeps the application layer free of framework coupling.
