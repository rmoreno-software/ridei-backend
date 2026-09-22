# Architecture Overview

## Architectural style

The project follows **Hexagonal Architecture** (also known as *Ports & Adapters*, introduced by Alistair Cockburn) combined with the tactical patterns of **Domain-Driven Design (DDD)**.

The central rule is the **Dependency Rule**: source code dependencies always point inward. The domain knows nothing about Spring, JPA, HTTP, or any other technology. Infrastructure adapters know about the domain, never the other way around.

```
┌─────────────────────────────────────────────────────────────┐
│                        Infrastructure                        │
│  ┌──────────────────────────────────────────────────────┐   │
│  │                    Application                        │   │
│  │  ┌──────────────────────────────────────────────┐    │   │
│  │  │                   Domain                      │    │   │
│  │  │   Entities · Value Objects · Domain Events    │    │   │
│  │  │   Domain Exceptions · Port Interfaces         │    │   │
│  │  └──────────────────────────────────────────────┘    │   │
│  │   Use Cases (Application Services) · Commands        │   │
│  └──────────────────────────────────────────────────────┘   │
│   REST Controllers · JPA Adapters · Security · Messaging     │
└─────────────────────────────────────────────────────────────┘
```

---

## The three layers

### Domain layer

The innermost ring. Contains pure business logic with **no framework dependencies**. Everything here is plain Java.

- **Aggregate root** — `User`: the central domain object.
- **Value objects** — `Email`, `Username`, `PhoneNumber`, `UserId`, `IdentityDocument`: immutable objects that carry validated domain concepts.
- **Enums** — `UserRole`, `AccountStatus`, `Gender`, `DocumentType`: closed sets of domain-meaningful values.
- **Domain exceptions** — thrown when business rules are violated.
- **Domain events** — facts that happened inside the domain (`UserRegisteredEvent`).
- **Ports** — Java interfaces that the domain defines and that external layers must implement.

### Application layer

Orchestrates domain objects to fulfill use cases. Has no business logic of its own — it delegates to the domain.

- **Application services** — implement inbound port interfaces (e.g. `RegisterUserService`).
- **Commands** — immutable Java Records that carry the data needed to execute a use case (e.g. `RegisterUserCommand`).

### Infrastructure layer

The outermost ring. Adapts the application to the real world: HTTP, databases, password hashing, event publishing.

- **Inbound adapters** — `UserController` receives HTTP requests and calls use cases through inbound ports.
- **Outbound adapters** — `UserPersistenceAdapter`, `BCryptPasswordHasherAdapter`, `LogEventPublisherAdapter` implement outbound ports.
- **Configuration** — Spring `@Configuration` classes that wire everything together (`BeanConfig`, `SecurityConfig`).

---

## Ports & Adapters

### Inbound ports (driven by the outside world)

```
[HTTP Request]
      │
      ▼
 UserController  ──calls──▶  RegisterUserUseCase (port)
                                      │
                                      ▼
                             RegisterUserService (implementation)
```

The controller only depends on the **port interface** (`RegisterUserUseCase`). It never references the concrete `RegisterUserService`.

### Outbound ports (the domain calls out)

```
RegisterUserService  ──calls──▶  UserRepositoryPort (port)
                                         │
                                         ▼
                              UserPersistenceAdapter (implementation)
                                         │
                                         ▼
                                  UserJpaRepository
                                         │
                                         ▼
                                    PostgreSQL
```

The application service depends only on the **port interface**. The concrete adapter is injected at startup via Spring's DI container.

---

## Package structure

```
com.ridei.identity
├── IdentityServiceApplication.java        ← Spring Boot entry point
│
├── domain/
│   ├── model/                             ← Entities, Value Objects, Enums
│   ├── event/                             ← Domain Events
│   ├── exception/                         ← Domain Exceptions
│   └── port/
│       ├── in/                            ← Inbound Port interfaces (Use Cases)
│       └── out/                           ← Outbound Port interfaces
│
├── application/
│   ├── RegisterUserCommand.java           ← Command Record
│   └── RegisterUserService.java           ← Use Case implementation
│
└── infrastructure/
    ├── adapter/
    │   ├── in/
    │   │   └── rest/                      ← REST Controllers, DTOs, GlobalExceptionHandler
    │   └── out/
    │       ├── persistence/               ← JPA Entities, Repository, Adapter
    │       ├── security/                  ← BCrypt password hasher
    │       └── messaging/                 ← Event publisher adapter
    └── config/                            ← Spring @Configuration classes
```

---

## Monorepo structure

The repository is a **Maven multi-module project**. The root `pom.xml` acts as a parent, centralising dependency versions and build plugin configuration. Each microservice is an independent child module.

```
ridei-backend/           ← Parent POM (com.ridei:ridei-backend)
├── pom.xml
├── docker-compose.yml
├── ridei-identity/      ← Child module (identity & auth microservice)
│   └── pom.xml
└── ridei-garage/        ← Child module (motorbike garage microservice)
    └── pom.xml
```

`ridei-garage` (added 2026-09) was the first bounded context split out this way — the pattern for future microservices (e.g. `ridei-events`, `ridei-results`) is now proven: its own child module, its own package root (`com.ridei.garage`), its own database, and its own copy of `JwtAuthenticationFilter`/`SecurityConfig` rather than a shared library — see [§ Multi-service concerns](#multi-service-concerns) below for why.

---

## Multi-service concerns

Splitting a bounded context into its own microservice (`ridei-garage`) raised two questions that don't come up in a single-service setup:

### No shared code module between bounded contexts

`ridei-garage` does **not** depend on `ridei-identity` as a Maven module, even though both need, for example, JWT validation logic. Each service has its own minimal, independent copy (`com.ridei.garage.infrastructure.config.JwtAuthenticationFilter` duplicates the shape of `com.ridei.identity.infrastructure.config.JwtAuthenticationFilter`, but is not the same class or a shared dependency). This is a deliberate DDD choice, not an oversight: a shared "common" module becomes a hidden coupling point between contexts that are supposed to evolve independently, and it tends to accumulate concepts that only make sense from one context's point of view. The small amount of duplication is the cheaper cost.

### Cross-service authentication without a shared secret

`ridei-identity` issues JWTs; `ridei-garage` (and any future service) only needs to verify them, never issue its own. Rather than share a symmetric secret (HS256) — which would let a compromised or buggy service in `ridei-garage` forge tokens as if it were `ridei-identity` — tokens are signed with **ES256 (asymmetric)**. Only `ridei-identity` holds the private key; every other service holds only the public key, wired in via its own `${jwt.public-key}` property. See [deployment.md § JWT signing keys](deployment.md#5-jwt-signing-keys-es256-shared-across-services) for the operational side.

### Cross-database references without foreign keys

`ridei-garage`'s `motorbikes.owner_id` refers to a user that lives in `ridei_identity`'s database — a separate Postgres database, potentially a separate host in the future. There is no foreign key, no cross-database query, and no shared table. Ownership is established purely from the authenticated JWT subject at the API boundary (`OwnerId.of(authentication.getName())`), the same anti-IDOR pattern used everywhere in `ridei-identity` (`UserId.of(authentication.getName())`). See [database.md § ridei-garage database](database.md#2-ridei-garage-database) for the full rationale.

---

## Key design decisions

| Decision | Rationale |
|---|---|
| Hexagonal Architecture | Keeps the domain testable in isolation; infrastructure can be swapped without touching business logic |
| Domain-Driven Design | Models the business language explicitly (Value Objects, Aggregate Roots, Domain Events) |
| Immutable Value Objects | Prevent invalid state from propagating through the system |
| Ports defined in the domain | The domain dictates what it needs; infrastructure adapts — not the other way around |
| No Spring annotations in domain/application | Zero framework coupling in business logic |
| Static factory methods on aggregates | Enforce invariants at construction time (`User.register()`, `User.reconstitute()`) |
| Commands as Java Records | Immutable, concise, no setter surface |
| Flyway for migrations | Schema changes are versioned, reproducible, and auditable |
| Asymmetric JWT signing (ES256) across services | Only the issuing service (`ridei-identity`) can forge tokens; every verifying service holds only a public key |
| No shared code module between microservices | Keeps bounded contexts independently deployable and evolvable, at the cost of a small amount of duplication |
| Ownership by JWT subject, not foreign key, across service boundaries | Cross-database references are impossible/inappropriate between independent microservices; the API boundary is the trust boundary |
