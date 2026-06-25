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
└── ridei-identity/      ← Child module (identity microservice)
    └── pom.xml
```

Future microservices (e.g. `ridei-events`, `ridei-results`) are added as new child modules under the same parent.

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
