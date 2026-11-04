#!/usr/bin/env bash
# ridei-db-restore.sh — descifra un dump de ridei-db-backup.sh y lo restaura en una base de
# datos NUEVA del contenedor indicado. Nunca sobrescribe una base de datos existente: para
# recuperar producción se restaura en una BD nueva, se comprueba y se cambia la aplicación a ella
# (o se renombra), ver docs/deployment.md §9.
#
# Uso:
#   ridei-db-restore.sh --identity KEY_FILE --container NAME --user SUPERUSER --into NEW_DB \
#                       [--owner ROLE] [--check-and-drop] DUMP_FILE.dump.age
#
#   --owner ROLE      recuperación real: la BD nueva es de ROLE y se conservan los propietarios
#                     originales (los roles deben existir: restaura antes los "globals").
#
#   --check-and-drop  prueba de restauración: restaura, muestra filas por tabla y borra la BD.
#
# Ejemplo (prueba mensual):
#   rclone copy r2:ridei-backups/<host>/2026/11/04/<stamp>/ridei_circuits-<stamp>.dump.age /root/restore/
#   ridei-db-restore.sh --identity /root/restore/age-key.txt --container ridei-postgis --user postgres \
#       --into restore_check_circuits --check-and-drop /root/restore/ridei_circuits-<stamp>.dump.age

set -Eeuo pipefail
umask 077

usage() { awk 'NR > 1 && /^#/ { sub(/^# ?/, ""); print; next } NR > 1 { exit }' "$0"; exit 2; }
log() { printf '%s [ridei-db-restore] %s\n' "$(date -u +%FT%TZ)" "$*"; }
fail() { log "ERROR: $*"; exit 1; }

IDENTITY="" CONTAINER="" PGUSER="" INTO="" OWNER="" CHECK_AND_DROP=0 DUMP=""
while [[ $# -gt 0 ]]; do
    case "$1" in
        --identity) IDENTITY="$2"; shift 2 ;;
        --container) CONTAINER="$2"; shift 2 ;;
        --user) PGUSER="$2"; shift 2 ;;
        --into) INTO="$2"; shift 2 ;;
        --owner) OWNER="$2"; shift 2 ;;
        --check-and-drop) CHECK_AND_DROP=1; shift ;;
        -h|--help) usage ;;
        -*) fail "unknown option $1" ;;
        *) DUMP="$1"; shift ;;
    esac
done
[[ -n "$IDENTITY" && -n "$CONTAINER" && -n "$PGUSER" && -n "$INTO" && -n "$DUMP" ]] || usage
[[ "$INTO" =~ ^[a-z_][a-z0-9_]*$ ]] || fail "--into must be a plain lowercase identifier"
[[ -r "$IDENTITY" ]] || fail "cannot read age identity '$IDENTITY'"
[[ -r "$DUMP" ]] || fail "cannot read dump '$DUMP'"

psql_in() { docker exec -i "$CONTAINER" psql --username="$PGUSER" --no-psqlrc --set=ON_ERROR_STOP=1 "$@"; }

exists="$(psql_in --dbname=postgres --tuples-only --no-align \
    --command="SELECT 1 FROM pg_database WHERE datname = '${INTO}'")"
[[ -z "$exists" ]] || fail "database '${INTO}' already exists; refusing to overwrite it"

WORK="$(mktemp -d)"
trap 'rm -rf "$WORK"' EXIT

log "decrypting $(basename "$DUMP")"
age --decrypt --identity "$IDENTITY" --output "$WORK/plain.dump" "$DUMP"

log "creating database ${INTO} in ${CONTAINER}"
psql_in --dbname=postgres --command="CREATE DATABASE ${INTO}${OWNER:+ OWNER ${OWNER}}"

log "restoring"
# Con --owner (recuperación real) se conservan los propietarios del dump: las tablas vuelven a
# ser del rol de la aplicación y las extensiones (postgis) del superusuario que restaura. Sin
# --owner (prueba) todo queda del superusuario y los roles originales no tienen por qué existir.
ownership=(--no-owner)
[[ -n "$OWNER" ]] && ownership=()
docker exec -i "$CONTAINER" pg_restore --username="$PGUSER" --dbname="$INTO" "${ownership[@]}" \
    --exit-on-error < "$WORK/plain.dump"

log "rows per table"
psql_in --dbname="$INTO" --command="ANALYZE" > /dev/null
psql_in --dbname="$INTO" --command="
    SELECT schemaname || '.' || relname AS table_name, n_live_tup AS approx_rows
    FROM pg_stat_user_tables
    WHERE schemaname NOT IN ('tiger', 'tiger_data', 'topology')
    ORDER BY schemaname, relname"

if [[ "$CHECK_AND_DROP" == "1" ]]; then
    psql_in --dbname=postgres --command="DROP DATABASE ${INTO}"
    log "restore check OK; database ${INTO} dropped"
else
    log "restored into ${INTO}"
fi
