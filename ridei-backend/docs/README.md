# Ridei Backend — Documentation

Welcome to the Ridei Backend documentation. This index links to all available documentation for the project.

## Contents

| Document | Description |
|---|---|
| [Getting Started](getting-started.md) | Local environment setup, prerequisites, and first run |
| [Architecture Overview](architecture.md) | Hexagonal Architecture, DDD, module structure |
| [Domain Layer](domain-layer.md) | Entities, Value Objects, Domain Events, Ports, Exceptions |
| [Application Layer](application-layer.md) | Use Cases, Commands, Application Services |
| [Infrastructure Layer](infrastructure-layer.md) | REST adapters, Persistence, Security, Messaging, Configuration |
| [API Reference](api-reference.md) | REST endpoints, request/response shapes, HTTP status codes |
| [Error Handling](error-handling.md) | Error model, exception hierarchy, HTTP status mapping |
| [Database](database.md) | Schema, migrations strategy (Flyway), table definitions |
| [Testing](testing.md) | Test strategy, unit tests, coverage goals |

---

## Project at a Glance

**Ridei** is a platform for motorcycle racing events. This repository contains the backend monorepo, built as a collection of Spring Boot microservices.

### Current modules

| Module | Description | Port |
|---|---|---|
| `ridei-identity` | User registration and identity management | `8081` |

### Tech stack

| Concern | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.3.5 |
| Persistence | Spring Data JPA + PostgreSQL 16 |
| Migrations | Flyway 10 |
| Build | Maven (multi-module) |
| Infrastructure | Docker / Docker Compose |
| Boilerplate reduction | Lombok 1.18.34 |
| Object mapping | MapStruct 1.5.5 |

### Architectural style

The project applies **Hexagonal Architecture (Ports & Adapters)** combined with **Domain-Driven Design (DDD)**. The domain is completely isolated from frameworks and infrastructure — all coupling flows inward, toward the domain, never outward.
