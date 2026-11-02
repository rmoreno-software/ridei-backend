# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

Ridei is a platform for motorcycle racing events. This repo is a Maven multi-module monorepo of Spring Boot 3.3.5 / Java 21 microservices. Detailed docs live in `docs/` (architecture, API reference, database, deployment, error handling) — consult them before larger changes.

## Commands

Use the Maven wrapper from the repo root (`./mvnw` in Git Bash, `.\mvnw.cmd` in PowerShell).

```bash
docker compose up -d                       # Postgres 16 (container ridei-postgres, db ridei_identity)
./mvnw spring-boot:run -pl ridei-identity -Dspring-boot.run.profiles=local   # port 8081
./mvnw spring-boot:run -pl ridei-garage   -Dspring-boot.run.profiles=local   # port 8082
./mvnw clean package -pl ridei-garage -am -DskipTests   # build one module
./mvnw clean verify                        # build + all tests
./mvnw test -pl ridei-garage -Dtest=MotorbikeTest        # single test class
./mvnw test -pl ridei-garage -Dtest=MotorbikeTest#methodName      # single test method
```

- Running requires the `local` profile: `application-local.yml` (git-ignored) holds real DB credentials, R2 credentials and the ES256 JWT key pair. `application.yml` has only `${ENV_VAR}` placeholders. See `docs/getting-started.md` for generating keys and creating the separate `ridei_garage` database/role (not created by docker-compose).
- There is no linter configured.
- Tests are plain JUnit 5 + AssertJ + Mockito unit tests (no Spring context, no DB) for domain models and application services; see `docs/testing.md`. Coverage is still small — controllers, persistence and R2 adapters are untested. After moving/renaming packages run `./mvnw clean verify`, since stale classes in `target/` can hide broken imports.

## Architecture

### Modules
- `ridei-identity` (`com.ridei.identity`) — registration, login (password + Google), JWT issuing, refresh tokens, email verification, password reset, multi-step onboarding, profile pictures. Also hosts a small `com.ridei.landing` package (waitlist), picked up via `@ComponentScan` in `IdentityServiceApplication`.
- `ridei-garage` (`com.ridei.garage`) — users' motorbikes, brands catalog, motorbike photos.

The services are deliberately independent bounded contexts: **no shared/common module**, no Maven dependency between them, separate databases with **no cross-DB foreign keys**. Duplicated infrastructure (e.g. `JwtAuthenticationFilter`, `SecurityConfig`, `ApiError`, `ImageContentType`, `PresignedUpload`) is intentional — don't extract it into a shared library. New services should follow the `ridei-garage` pattern.

### Hexagonal / DDD layout (both modules)
- `domain/model` — aggregates, value objects (`*Id`, `Email`, `Username`, …), enums. Aggregates use static factories (`register()` / `reconstitute()`-style) to enforce invariants.
- `domain/port/out` — `*Port` interfaces for repositories, storage, email, etc. (owned by the domain).
- `application/port/in` — one `*UseCase` interface per use case. They live in `application`, not `domain`, because their signatures use `*Command` records; the domain must never import from `application` or `infrastructure`.
- `domain/exception` — domain exceptions (plain `RuntimeException`s).
- `application` — `*Service` implementing a use case, plus `*Command` records as input. Services are plain classes (no `@Service`); the only Spring annotation used is `@Transactional`, which every write use case must carry.
- `infrastructure/adapter/in/rest` — controllers, request/response DTOs, `GlobalExceptionHandler`; `infrastructure/adapter/out/*` — `@Component` adapters implementing outbound ports (JPA, Cloudflare R2, SMTP, Google, BCrypt, JWT).
- `infrastructure/config/BeanConfig` — **every application service is wired manually here**. Adding a use case means: UseCase interface in `application/port/in`, Service + Command in `application`, a `@Bean` in `BeanConfig`, then the controller endpoint.

### Cross-cutting conventions
- **Auth**: `ridei-identity` signs JWTs with ES256 (private key); other services only verify with the public key (`jwt.public-key`). Never introduce a shared symmetric secret.
- **Ownership / anti-IDOR**: the current user is always taken from the JWT subject (`UserId.of(authentication.getName())`, `OwnerId.of(...)`), never from request bodies or paths. In garage, load owned aggregates via `OwnedMotorbikes.require(...)`, which returns `MotorbikeNotFoundException` (404) for both missing and not-owned.
- **Errors**: domain exceptions are mapped to HTTP statuses in each module's `GlobalExceptionHandler`, returning `ApiError`. New domain exceptions need a handler entry there. Garage's `ApiError` has no `timestamp` field (unlike identity's) — a known, intentional-for-now divergence.
- **Images**: uploads use a presign → client uploads directly to Cloudflare R2 → confirm flow (`Request…UploadService` / `Confirm…Service`).
- **DB**: Flyway migrations in `<module>/src/main/resources/db/migration/` (`V<n>__description.sql`), Hibernate `ddl-auto: validate` — every entity change needs a new migration; never edit an applied one.
- **i18n (identity)**: user-facing emails/pages use `messages*.properties` (default, `es`, `ca`) and Thymeleaf templates in `templates/`; locale flows through commands (e.g. `RegisterUserCommand.locale`). Keep all three properties files in sync.

### Deployment
Both services run on an Ubuntu server as systemd units (`ridei-identity`, `ridei-garage`) with profile `prod`, secrets injected from `/etc/<service>/*.env`. See `docs/deployment.md` before adding new env vars.
