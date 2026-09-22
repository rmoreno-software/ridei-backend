# Deployment

This document describes how the Ridei backend services (`ridei-identity` and `ridei-garage`) are deployed to the Ubuntu server, how the process is managed, and — most importantly — how to add or change environment variables safely.

Each service is an independent Spring Boot process with its own systemd unit, its own Postgres database, and its own `/etc/<service-name>/` config directory. They are not coupled at the process level — the only thing they share is the JWT public key used to verify tokens issued by `ridei-identity` (see [§5](#5-jwt-signing-keys-es256-shared-across-services)).

---

## 1. Runtime overview

| Concern | `ridei-identity` | `ridei-garage` |
|---|---|---|
| Unit name | `ridei-identity` *(renamed from `spring-app` on 2026-09-22)* | `ridei-garage` |
| Unit file | `/etc/systemd/system/ridei-identity.service` | `/etc/systemd/system/ridei-garage.service` |
| Override (env wiring) | `/etc/systemd/system/ridei-identity.service.d/override.conf` | `/etc/systemd/system/ridei-garage.service.d/override.conf` |
| Runs as user | `spring` | `spring` |
| Working directory | `/root/ridei-backend/source/ridei-backend/ridei-backend` | `/root/ridei-backend/source/ridei-backend/ridei-backend` |
| Jar | `ridei-identity/target/ridei-identity-0.0.1-SNAPSHOT.jar` | `ridei-garage/target/ridei-garage-0.0.1-SNAPSHOT.jar` |
| Active Spring profile | `prod` (set via `Environment=` in the base unit) | `prod` (set via `Environment=` in the base unit) |
| Port | `8081` | `8082` |
| Database | `ridei_identity` (Postgres, Docker container `ridei-postgres`) | `ridei_garage` (same Postgres container, separate DB + role, no shared schema or FK with identity — see [database.md](database.md)) |

Configuration is layered the same way locally and on the server, for both services — only the profile and the source of the secret values change:

| Profile | File (committed to git) | Where secrets live |
|---|---|---|
| Default | `application.yml` | Placeholders only (`${VAR_NAME}`), no real values |
| `local` | `application-local.yml` | Real values written directly in the file (git-ignored) |
| `prod` | *(none yet — uses `application.yml` defaults)* | `/etc/<service-name>/*.env` on the server, injected as OS environment variables |

---

## 2. Deploying a new version

Both services live in the same monorepo and are built from the same checkout with Maven's `-pl` (project list) flag. Build only what changed, or omit `-pl` to rebuild everything.

```bash
ssh <user>@<server>

cd /root/ridei-backend/source/ridei-backend/ridei-backend
git pull

# identity only
./mvnw clean package -pl ridei-identity -am -DskipTests
sudo systemctl restart ridei-identity

# garage only
./mvnw clean package -pl ridei-garage -am -DskipTests
sudo systemctl restart ridei-garage
```

`-am` ("also make") also rebuilds whatever the target module depends on in the reactor — harmless to always include even when there's no cross-module dependency.

Verify each service came back up cleanly:

```bash
sudo journalctl -u ridei-identity -n 50 --no-pager
sudo journalctl -u ridei-garage -n 50 --no-pager
```

Look for:
```
The following 1 profile is active: "prod"
...
Started IdentityServiceApplication in X.XXX seconds
```
(or `Started GarageServiceApplication ...` for garage)

If the process fails to start, `journalctl` will show the stack trace immediately — check [Troubleshooting](#6-troubleshooting) below before assuming it's a code bug. In practice, almost every failure during garage's first deployment was a config/wiring mistake, not a code bug — see the troubleshooting table.

---

## 3. Environment variables

Environment variables are stored in two plain files per service, split by sensitivity, and wired into each systemd unit via `EnvironmentFile=`. This means **changing a value never requires touching the unit file, rebuilding, or running `daemon-reload`** — just edit the file and restart the service.

| File | Purpose |
|---|---|
| `/etc/<service-name>/app.env` | Non-secret runtime configuration (feature flags, non-sensitive endpoints) |
| `/etc/<service-name>/secrets.env` | Credentials and secrets |

### 3.1 `ridei-identity`

| Variable | File | Notes |
|---|---|---|
| `JWT_PRIVATE_KEY` | `secrets.env` | Base64 PKCS8-encoded EC private key (P-256). Only identity holds this. Replaces the old `JWT_SECRET` (HS256) — see [§5](#5-jwt-signing-keys-es256-shared-across-services). |
| `JWT_PUBLIC_KEY` | `secrets.env` | Base64 X.509-encoded EC public key, matching the private key above. Also copied into `ridei-garage`'s `secrets.env`. |
| `R2_ACCESS_KEY` / `R2_SECRET_KEY` / etc. | `secrets.env` | Cloudflare R2 credentials for profile picture uploads. |
| `GOOGLE_CLIENT_ID` | `app.env` | Not secret, but kept alongside other config. |
| *(DB credentials)* | `secrets.env` | Whatever names `application.yml` currently references for the datasource. |

### 3.2 `ridei-garage`

| Variable | File | Notes |
|---|---|---|
| `GARAGE_DB_URL` | `app.env` | e.g. `jdbc:postgresql://localhost:5432/ridei_garage`. Not secret (no credentials in the URL itself). |
| `GARAGE_DB_USERNAME` | `secrets.env` | Dedicated Postgres role `ridei_garage`, scoped to only the `ridei_garage` database. |
| `GARAGE_DB_PASSWORD` | `secrets.env` | **Use only alphanumeric characters.** systemd's `EnvironmentFile` format treats `#` as a comment start and can mishandle `$`, quotes, backslashes, or spaces in values — a password with any of those caused a false "password authentication failed" during garage's first deployment even though the password itself, checked in isolation, was fine. |
| `JWT_PUBLIC_KEY` | `secrets.env` | **Must be byte-for-byte the same value** as `ridei-identity`'s `JWT_PUBLIC_KEY` — garage only verifies tokens, it never issues them. |

Both files use plain `KEY=value` lines (systemd `EnvironmentFile` format — no `export`, no quotes unless the value contains spaces, `#` for comments).

They are wired into each service via a systemd override, **not** the main unit file:

```bash
sudo systemctl cat ridei-identity   # or ridei-garage
```
```ini
# /etc/systemd/system/ridei-identity.service.d/override.conf
[Service]
EnvironmentFile=/etc/ridei-identity/app.env
EnvironmentFile=/etc/ridei-identity/secrets.env
```

If `systemctl cat` doesn't show an override section at all, the `EnvironmentFile=` lines were never saved — this happened during garage's first deployment (the `systemctl edit` session was closed without content actually landing in `[Service]`), and it silently manifests as every placeholder in `application.yml` failing to resolve, e.g. `Driver org.postgresql.Driver claims to not accept jdbcUrl, ${GARAGE_DB_URL}` — the literal, un-substituted string being passed to the driver.

Permissions — only the `spring` user (and root) can read them:

```bash
sudo chown spring:spring /etc/<service-name>/*.env
sudo chmod 600 /etc/<service-name>/*.env
```

### 3.3 Adding a new environment variable — step by step

Say you're adding a new integration that needs `SOME_NEW_API_KEY` to `ridei-identity` (same steps apply to `ridei-garage`, substitute the service name).

1. **Reference it in `application.yml`** (committed to git — never put the real value here):
   ```yaml
   some:
     integration:
       api-key: ${SOME_NEW_API_KEY}
   ```
   Add a default (`${SOME_NEW_API_KEY:some-safe-dev-default}`) only if a non-secret fallback makes sense for local dev. Never give a real secret a default — if it's missing, the app should fail to start rather than silently run with a wrong/insecure value (this already bit us once with `JWT_SECRET`, see [Troubleshooting](#6-troubleshooting)).

2. **Add the real value on the server**, in whichever file matches its sensitivity:
   ```bash
   sudo nano /etc/ridei-identity/secrets.env   # or app.env if it's not sensitive
   ```
   ```ini
   SOME_NEW_API_KEY=the-real-value
   ```

3. **Restart the service** — no `daemon-reload` needed, you only touched a data file, not the unit:
   ```bash
   sudo systemctl restart ridei-identity
   ```

4. **Verify it resolved correctly**:
   ```bash
   PID=$(systemctl show ridei-identity -p MainPID --value)
   sudo cat /proc/$PID/environ | tr '\0' '\n' | grep SOME_NEW_API_KEY
   ```
   and confirm no `Could not resolve placeholder 'SOME_NEW_API_KEY'` error in:
   ```bash
   sudo journalctl -u ridei-identity -n 80 --no-pager
   ```

You do **not** need to touch the systemd unit or its override for this — that part only changes if you introduce an entirely new `.env` file (a new scope), not for individual variables inside an existing one.

### 3.4 Adding a whole new scope (a new `.env` file)

Only needed if you want to separate a new category of config (e.g. per-integration files instead of one shared `secrets.env`):

```bash
sudo touch /etc/ridei-identity/new-scope.env
sudo chown spring:spring /etc/ridei-identity/new-scope.env
sudo chmod 600 /etc/ridei-identity/new-scope.env
sudo systemctl edit ridei-identity
```
Add a new line inside `[Service]`:
```ini
EnvironmentFile=/etc/ridei-identity/new-scope.env
```
This time, since the **unit definition itself** changed, run:
```bash
sudo systemctl daemon-reload
sudo systemctl restart ridei-identity
```

### 3.5 First deployment of a brand-new service (what we did for `ridei-garage`)

1. Build: `./mvnw clean package -pl ridei-garage -am -DskipTests`.
2. Create its Postgres database and a dedicated, least-privilege role (see [database.md](database.md)) — never reuse `ridei-identity`'s database role.
3. Create `/etc/ridei-garage/app.env` and `secrets.env`, `chmod 600`, `chown spring:spring`.
4. Write the unit file at `/etc/systemd/system/ridei-garage.service` — **double-check the `ExecStart` path includes the full jar filename with its version and `.jar` extension** (`ridei-garage-0.0.1-SNAPSHOT.jar`, not just `ridei-garage`); a truncated path fails with `Error: Unable to access jarfile ...` and systemd will loop-restart it indefinitely.
5. `sudo systemctl edit ridei-garage` to wire the two `EnvironmentFile=` lines — then confirm with `systemctl cat` that they actually landed (see §3.2 above).
6. `sudo systemctl daemon-reload && sudo systemctl enable --now ridei-garage`.
7. Add an `ingress` rule for it in the Cloudflare Tunnel config (see [§7](#7-cloudflare-tunnel-ingress)) and restart `cloudflared`.
8. Verify end-to-end with a real access token issued by `ridei-identity` (see [§7](#7-cloudflare-tunnel-ingress)).

---

## 4. Checking what's currently configured

```bash
# Which profile is active + which files are wired in
sudo systemctl cat ridei-identity   # or ridei-garage

# Only vars set via Environment= directly in the unit (NOT EnvironmentFile contents)
sudo systemctl show ridei-identity -p Environment

# The full, real environment the JVM actually received (the reliable check)
PID=$(systemctl show ridei-identity -p MainPID --value)
sudo cat /proc/$PID/environ | tr '\0' '\n' | sort

# Live logs
sudo journalctl -u ridei-identity -f
```

---

## 5. JWT signing keys (ES256, shared across services)

As of 2026-09-22, tokens are signed with **ES256 (asymmetric, EC P-256)** instead of the original shared-secret HS256. This closes the design flaw where any service holding the signing secret could *also* forge tokens — now only `ridei-identity` holds the private key; `ridei-garage` (and any future service) holds only the public key and can verify but never issue tokens.

- `ridei-identity`: holds both `JWT_PRIVATE_KEY` and `JWT_PUBLIC_KEY` in `secrets.env`.
- `ridei-garage`: holds only `JWT_PUBLIC_KEY` (must match identity's byte-for-byte) in `secrets.env`.
- Production keys are **never** the same as local dev keys — generate a dedicated pair per environment.
- Rotating the key pair invalidates every previously-issued token immediately (both services fail closed on signature mismatch) — every logged-in user is forced to re-login. Fine while there are no real users yet; once there are, plan a maintenance window.

### Generating a new key pair

```bash
openssl ecparam -name prime256v1 -genkey -noout -out prod_private.pem
openssl pkcs8 -topk8 -nocrypt -in prod_private.pem -outform DER | base64 -w0 > prod_private_b64.txt
openssl ec -in prod_private.pem -pubout -outform DER | base64 -w0 > prod_public_b64.txt
```

Before pasting into `secrets.env`, sanity-check the output does **not** start with `-----BEGIN` — that means the PEM headers leaked into the value instead of the pure Base64-encoded DER bytes (this happened once and produced `Illegal base64 character 2d`, `2d` being the hex code for `-`):

```bash
head -c 20 prod_private_b64.txt; echo
head -c 20 prod_public_b64.txt; echo
```

Then:
1. Update `JWT_PRIVATE_KEY` and `JWT_PUBLIC_KEY` in `/etc/ridei-identity/secrets.env`.
2. Update `JWT_PUBLIC_KEY` (same value) in `/etc/ridei-garage/secrets.env`.
3. Restart both services.
4. Securely delete the temporary key files: `shred -u prod_private.pem prod_private_b64.txt prod_public_b64.txt`.

---

## 6. Troubleshooting

| Symptom in `journalctl` | Cause | Fix |
|---|---|---|
| `No active profile set, falling back to 1 default profile: "default"` | `SPRING_PROFILES_ACTIVE` not reaching the process | Check `systemctl cat <service>` for `Environment=`/`EnvironmentFile=`; if you just edited the override, run `daemon-reload` then `restart` |
| `Could not resolve placeholder 'X_VAR' in value "${X_VAR}"` | Variable referenced in `application.yml` isn't set anywhere the process can see, and has no default | Add it to `app.env`/`secrets.env`, `restart` (no `daemon-reload` needed for file content changes). If the placeholder is echoed back literally into a driver error (e.g. `claims to not accept jdbcUrl, ${GARAGE_DB_URL}`), the `EnvironmentFile=` lines likely aren't linked at all — check with `systemctl cat` |
| `Error: Unable to access jarfile /path/to/module/target/<name>` (no version, no `.jar`) | `ExecStart` path is missing the jar's version suffix and extension | Run `ls target/*.jar` to get the exact filename and fix `ExecStart` in the `.service` file, then `daemon-reload` + `restart` |
| `FATAL: password authentication failed for user "X"` | The DB password in `secrets.env` doesn't actually match Postgres, **or** it contains a character (`#`, `$`, quotes, spaces) that `EnvironmentFile` parsing mangled | Test the exact credential path from the host (not `docker exec`, which may hit a `trust`-authenticated local rule and give a false positive): `PGPASSWORD='...' psql -h 127.0.0.1 -p 5432 -U <user> -d <db>`. If that also fails, reset the password to something purely alphanumeric with `ALTER USER ... WITH PASSWORD '...'` and update `secrets.env` to match exactly |
| `Illegal base64 character 2d` when loading `JWT_PRIVATE_KEY`/`JWT_PUBLIC_KEY` | The value in `secrets.env` includes the PEM `-----BEGIN...-----`/`-----END...-----` header/footer instead of the raw Base64 DER | Regenerate with `openssl ... \| base64 -w0` (never paste the `.pem` file contents directly) and confirm with `head -c 20` that it doesn't start with `-----BEGIN` |
| `Unit ridei-identity.service not found` (or similar) right after a rename | Systemd doesn't know the unit under its old *or* assumed name | `systemctl list-units --type=service --all \| grep -i ridei` to find the actual current name, or `systemctl cat <old-name>` to locate the underlying `.service` file before renaming |
| App starts fine but a feature silently uses a wrong/default value | The placeholder has a fallback (`${VAR:default}`) and the real var was never set | Grep `application.yml` for the property, confirm the fallback isn't a placeholder secret left over from development (this happened with `JWT_SECRET`) |
| `systemctl show <service> -p Environment` looks incomplete | Expected — that property only reflects `Environment=` lines in the unit, not `EnvironmentFile=` contents | Use the `/proc/$PID/environ` check instead |
| Editing an `.env` file has no effect | Forgot to restart, or edited the wrong file/path (double-check you're not editing `ridei-identity`'s file when you meant `ridei-garage`'s) | `systemctl cat <service>` to confirm the exact file paths wired in, then `systemctl restart <service>` |
| Cloudflare returns `502 Bad Gateway` for a route that used to work, or a brand-new route | `cloudflared` can't reach the origin: the local service isn't running, is bound to the wrong port, or the `ingress` rule in `config.yml` is missing/misordered/wrong port | First confirm the service answers locally: `curl -i http://localhost:<port>/...` (expect `401`, not a connection error). If that works, check `/etc/cloudflared/config.yml` ingress order (a rule after the catch-all `service: http_status:404` is never reached) and `sudo systemctl restart cloudflared` |
| A new service's endpoint returns `500`/`401` with the **other** service's exact error shape (e.g. calling `ridei-garage` but the JSON has a `timestamp` field and the `ridei-identity`-only typo `"An unexpected error ocurred"`) | The request isn't reaching the service you think it is — `cloudflared` is routing it to the wrong origin, almost always because the *running* `config.yml` still has the old rules | Check `sudo journalctl -u cloudflared -n 30` for the request and read its `ingressRule=`/`originService=` fields — this tells you definitively which rule matched and which port it went to. Then confirm you're editing `/etc/cloudflared/config.yml`, not `~/.cloudflared/config.yml` (see [§7](#7-cloudflare-tunnel-ingress)) |
| `cloudflared` log: `dial tcp [::1]:8081: connect: connection refused` | The origin service (identity or garage) wasn't listening on that port at that moment — usually mid-restart, or it crashed on startup | Check `systemctl status <service>` for that exact time window; if it's not transient, this is really a "service won't start" problem, not a tunnel problem — go through the rest of this table for the service's own logs |

---

## 7. Cloudflare Tunnel ingress

Both services are exposed exclusively through the existing `cloudflared` tunnel — no inbound port is opened on the server's firewall.

### ⚠️ The config file that matters is `/etc/cloudflared/config.yml`, not `~/.cloudflared/config.yml`

These are two **different files**. The systemd unit's `ExecStart` (check with `systemctl cat cloudflared` or `systemctl status cloudflared`) pins the config path explicitly:
```
ExecStart=/usr/bin/cloudflared --no-autoupdate --config /etc/cloudflared/config.yml tunnel run
```
Editing `~/.cloudflared/config.yml` (i.e. `/root/.cloudflared/config.yml`) has **no effect on the running service** — it's silently ignored. This bit us once: garage's ingress rules were added to the wrong file, `cloudflared` kept running its old single-rule config, and every request to `/api/v1/garage/*` was routed to `ridei-identity` instead (see the troubleshooting row below for how that surfaced). Always confirm the actual path with `systemctl cat cloudflared` before editing, and edit `/etc/cloudflared/config.yml` directly.

### Current ingress rules

`ridei-identity`'s existing endpoints don't use an `/api/v1/identity/*` prefix (they're `/api/v1/users/...`, `/api/v1/auth/...`, etc.), so a path-only split between the two services needs a fallback rule for identity's un-prefixed routes, evaluated after the more specific `/api/v1/garage/*` rule but before the catch-all:

```yaml
tunnel: ridei-api
credentials-file: /etc/cloudflared/<tunnel-id>.json

ingress:
  - hostname: api.rideiapp.com
    path: /api/v1/identity/*
    service: http://localhost:8081
  - hostname: api.rideiapp.com
    path: /api/v1/garage/*
    service: http://localhost:8082
  - hostname: api.rideiapp.com
    service: http://localhost:8081
  - service: http_status:404
```

Rules are evaluated **top to bottom, first match wins** — the catch-all (`service: http_status:404`) must always be **last**, and the unqualified identity fallback must come after the garage-specific rule (otherwise it would swallow every garage request too, matching on hostname alone).

```bash
sudo systemctl restart cloudflared
sudo journalctl -u cloudflared -n 30 --no-pager
```

### End-to-end verification after adding a new service

1. Log in against `ridei-identity` in production to get a real access token.
2. `GET https://api.rideiapp.com/api/v1/garage/motorbikes` with `Authorization: Bearer <token>` → expect `200` (empty array for a new user).
3. Same request without a token, or with an old HS256-signed token → expect `401`.
4. Check `sudo journalctl -u cloudflared -n 30 --no-pager` for the request and confirm `originService=http://localhost:8082` (not `:8081`) — this is the definitive way to know which ingress rule actually matched, more reliable than guessing from the response body alone.

---

## 8. Security notes

- `secrets.env` and `app.env` live outside the git repository (`/etc/<service-name>/`) and are never committed. `application-local.yml` is git-ignored for the same reason — real credentials should never reach git history.
- Keep `.env` files at `chmod 600`, owned by the `spring` user that runs the process.
- If a secret is ever pasted somewhere outside these files (chat, ticket, shared doc), rotate it — assume it's compromised.
- Never give a real secret a default value in `application.yml`. A missing required variable should crash the app loudly at startup, not silently fall back to an insecure default.
- Each service's database role is scoped to only that service's database (`REVOKE CONNECT ... FROM PUBLIC` at creation time) — `ridei-garage` cannot read `ridei-identity`'s data at the database level, matching the hexagonal/DDD boundary at the process level.

---

## 9. Pending — known gaps

- **No database backups (added 2026-09-15, still open).** There is currently no automated backup of either production Postgres database (`ridei_identity` or, as of this deployment, `ridei_garage`) — if the server fails or data is deleted by mistake, it's unrecoverable. Decided to defer implementation for now; **must be resolved before relying on this for real user data.**
  Planned approach when picked back up: a daily `pg_dump` cron job (covering both databases) uploading a compressed dump to a dedicated Cloudflare R2 bucket (`ridei-backups`, separate from the profile-pictures bucket, with its own scoped API token and an object lifecycle rule to auto-expire old dumps), plus enabling Hetzner's automatic server snapshots as a coarser secondary safety net. Test the restore procedure once set up — an untested backup isn't a real backup.
- **Rate limiting** was designed twice and cancelled both times by explicit decision — not implemented.
- **GDPR account-deletion endpoint** was flagged during the production-readiness audit but never implemented.
- **`ridei-garage` only has `POST`/`GET /api/v1/garage/motorbikes` so far** — get single motorbike, edit, retire/dispose, and photo upload (reusing the R2 presign pattern from `ridei-identity`) are the natural next steps but haven't been requested yet.
