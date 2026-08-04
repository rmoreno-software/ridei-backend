# Deployment

This document describes how `ridei-identity` is deployed to the Ubuntu server, how the process is managed, and — most importantly — how to add or change environment variables safely.

---

## 1. Runtime overview

| Concern | Value |
|---|---|
| Process manager | `systemd` |
| Unit name | `spring-app` |
| Unit file | `/etc/systemd/system/spring-app.service` |
| Override (env wiring) | `/etc/systemd/system/spring-app.service.d/override.conf` |
| Runs as user | `spring` |
| Working directory | `/root/ridei-backend/source/ridei-backend/ridei-backend` |
| Jar | `ridei-identity/target/ridei-identity-0.0.1-SNAPSHOT.jar` |
| Active Spring profile | `prod` (set via `Environment=` in the base unit) |
| Port | `8081` |

Configuration is layered the same way locally and on the server — only the profile and the source of the secret values change:

| Profile | File (committed to git) | Where secrets live |
|---|---|---|
| Default | `application.yml` | Placeholders only (`${VAR_NAME}`), no real values |
| `local` | `application-local.yml` | Real values written directly in the file (git-ignored) |
| `prod` | *(none yet — uses `application.yml` defaults)* | `/etc/ridei-identity/*.env` on the server, injected as OS environment variables |

---

## 2. Deploying a new version

```bash
ssh <user>@<server>

cd /root/ridei-backend/source/ridei-backend/ridei-backend
git pull

./mvnw clean package -pl ridei-identity -DskipTests

sudo systemctl restart spring-app
```

Verify it came back up cleanly:

```bash
sudo journalctl -u spring-app -n 50 --no-pager
```

Look for:
```
The following 1 profile is active: "prod"
...
Started IdentityServiceApplication in X.XXX seconds
```

If the process fails to start, `journalctl` will show the stack trace immediately — check [Troubleshooting](#5-troubleshooting) below before assuming it's a code bug.

---

## 3. Environment variables

Environment variables are stored in two plain files, split by sensitivity, and wired into the systemd unit via `EnvironmentFile=`. This means **changing a value never requires touching the unit file, rebuilding, or running `daemon-reload`** — just edit the file and restart the service.

| File | Purpose | Example content |
|---|---|---|
| `/etc/ridei-identity/app.env` | Non-secret runtime configuration | feature flags, non-sensitive endpoints |
| `/etc/ridei-identity/secrets.env` | Credentials and secrets | `JWT_SECRET`, `R2_ACCESS_KEY`, `GOOGLE_CLIENT_ID`, etc. |

Both files use plain `KEY=value` lines (systemd `EnvironmentFile` format — no `export`, no quotes unless the value contains spaces, `#` for comments).

They are wired into the service via a systemd override, **not** the main unit file:

```bash
sudo systemctl cat spring-app
```
```ini
# /etc/systemd/system/spring-app.service.d/override.conf
[Service]
EnvironmentFile=/etc/ridei-identity/app.env
EnvironmentFile=/etc/ridei-identity/secrets.env
```

Permissions — only the `spring` user (and root) can read them:

```bash
sudo chown spring:spring /etc/ridei-identity/*.env
sudo chmod 600 /etc/ridei-identity/*.env
```

### 3.1 Adding a new environment variable — step by step

Say you're adding a new integration that needs `SOME_NEW_API_KEY`.

1. **Reference it in `application.yml`** (committed to git — never put the real value here):
   ```yaml
   some:
     integration:
       api-key: ${SOME_NEW_API_KEY}
   ```
   Add a default (`${SOME_NEW_API_KEY:some-safe-dev-default}`) only if a non-secret fallback makes sense for local dev. Never give a real secret a default — if it's missing, the app should fail to start rather than silently run with a wrong/insecure value (this already bit us once with `JWT_SECRET`, see [Troubleshooting](#5-troubleshooting)).

2. **Add the real value on the server**, in whichever file matches its sensitivity:
   ```bash
   sudo nano /etc/ridei-identity/secrets.env   # or app.env if it's not sensitive
   ```
   ```ini
   SOME_NEW_API_KEY=the-real-value
   ```

3. **Restart the service** — no `daemon-reload` needed, you only touched a data file, not the unit:
   ```bash
   sudo systemctl restart spring-app
   ```

4. **Verify it resolved correctly**:
   ```bash
   PID=$(systemctl show spring-app -p MainPID --value)
   sudo cat /proc/$PID/environ | tr '\0' '\n' | grep SOME_NEW_API_KEY
   ```
   and confirm no `Could not resolve placeholder 'SOME_NEW_API_KEY'` error in:
   ```bash
   sudo journalctl -u spring-app -n 80 --no-pager
   ```

You do **not** need to touch the systemd unit or its override for this — that part only changes if you introduce an entirely new `.env` file (a new scope), not for individual variables inside an existing one.

### 3.2 Adding a whole new scope (a new `.env` file)

Only needed if you want to separate a new category of config (e.g. per-integration files instead of one shared `secrets.env`):

```bash
sudo touch /etc/ridei-identity/new-scope.env
sudo chown spring:spring /etc/ridei-identity/new-scope.env
sudo chmod 600 /etc/ridei-identity/new-scope.env
sudo systemctl edit spring-app
```
Add a new line inside `[Service]`:
```ini
EnvironmentFile=/etc/ridei-identity/new-scope.env
```
This time, since the **unit definition itself** changed, run:
```bash
sudo systemctl daemon-reload
sudo systemctl restart spring-app
```

---

## 4. Checking what's currently configured

```bash
# Which profile is active + which files are wired in
sudo systemctl cat spring-app

# Only vars set via Environment= directly in the unit (NOT EnvironmentFile contents)
sudo systemctl show spring-app -p Environment

# The full, real environment the JVM actually received (the reliable check)
PID=$(systemctl show spring-app -p MainPID --value)
sudo cat /proc/$PID/environ | tr '\0' '\n' | sort

# Live logs
sudo journalctl -u spring-app -f
```

---

## 5. Troubleshooting

| Symptom in `journalctl` | Cause | Fix |
|---|---|---|
| `No active profile set, falling back to 1 default profile: "default"` | `SPRING_PROFILES_ACTIVE` not reaching the process | Check `systemctl cat spring-app` for `Environment=`/`EnvironmentFile=`; if you just edited the override, run `daemon-reload` then `restart` |
| `Could not resolve placeholder 'X_VAR' in value "${X_VAR}"` | Variable referenced in `application.yml` isn't set anywhere the process can see, and has no default | Add it to `app.env`/`secrets.env`, `restart` (no `daemon-reload` needed for file content changes) |
| App starts fine but a feature silently uses a wrong/default value | The placeholder has a fallback (`${VAR:default}`) and the real var was never set | Grep `application.yml` for the property, confirm the fallback isn't a placeholder secret left over from development (this happened with `JWT_SECRET`) |
| `systemctl show spring-app -p Environment` looks incomplete | Expected — that property only reflects `Environment=` lines in the unit, not `EnvironmentFile=` contents | Use the `/proc/$PID/environ` check instead |
| Editing an `.env` file has no effect | Forgot to restart, or edited the wrong file/path | `systemctl cat spring-app` to confirm the exact file paths wired in, then `systemctl restart spring-app` |

---

## 6. Security notes

- `secrets.env` and `app.env` live outside the git repository (`/etc/ridei-identity/`) and are never committed. `application-local.yml` is git-ignored for the same reason — real credentials should never reach git history.
- Keep `.env` files at `chmod 600`, owned by the `spring` user that runs the process.
- If a secret is ever pasted somewhere outside these files (chat, ticket, shared doc), rotate it — assume it's compromised.
- Never give a real secret a default value in `application.yml`. A missing required variable should crash the app loudly at startup, not silently fall back to an insecure default.
