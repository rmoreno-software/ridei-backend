#!/usr/bin/env bash
# ridei-db-backup.sh — copia diaria cifrada de las bases de datos de Ridei a Cloudflare R2.
#
# Por cada base de datos de BACKUP_TARGETS:
#   1. pg_dump en formato custom, ejecutado DENTRO de su contenedor (misma versión que el servidor);
#   2. comprueba que el archivo es legible (pg_restore --list);
#   3. lo cifra con age para las claves públicas de BACKUP_AGE_RECIPIENTS_FILE y borra el claro.
# Además guarda los roles de cada contenedor (pg_dumpall --globals-only), cifrados igual, y un
# SHA256SUMS de los ficheros cifrados. Después sube todo a R2 con rclone y poda las copias locales.
#
# Pensado para ejecutarse como root desde ridei-db-backup.service (acceso a docker). Cualquier
# fallo termina con código distinto de cero, así la unidad queda "failed" y aparece en el journal.
# Ver docs/deployment.md §9.

set -Eeuo pipefail
umask 077

: "${BACKUP_TARGETS:?BACKUP_TARGETS is required (\"container:superuser:database ...\")}"
: "${BACKUP_AGE_RECIPIENTS_FILE:?BACKUP_AGE_RECIPIENTS_FILE is required (age public key file)}"
BACKUP_DIR="${BACKUP_DIR:-/var/backups/ridei}"
BACKUP_LOCAL_RETENTION_DAYS="${BACKUP_LOCAL_RETENTION_DAYS:-7}"
BACKUP_R2_REMOTE="${BACKUP_R2_REMOTE:-r2}"
BACKUP_R2_BUCKET="${BACKUP_R2_BUCKET:-ridei-backups}"
BACKUP_SKIP_UPLOAD="${BACKUP_SKIP_UPLOAD:-0}"
BACKUP_HEARTBEAT_URL="${BACKUP_HEARTBEAT_URL:-}"

STAMP="$(date -u +%Y%m%dT%H%M%SZ)"
DAY_PREFIX="$(date -u +%Y/%m/%d)"
HOST="$(hostname)"; HOST="${HOST%%.*}"

log() { printf '%s [ridei-db-backup] %s\n' "$(date -u +%FT%TZ)" "$*"; }
fail() { log "ERROR: $*"; exit 1; }
trap 'log "ERROR: failed at line ${LINENO}: ${BASH_COMMAND}"' ERR

require_tools() {
    local tool
    for tool in docker age sha256sum; do
        command -v "$tool" >/dev/null || fail "'$tool' is not installed"
    done
    if [[ "$BACKUP_SKIP_UPLOAD" != "1" ]]; then
        command -v rclone >/dev/null || fail "'rclone' is not installed"
    fi
    [[ -s "$BACKUP_AGE_RECIPIENTS_FILE" ]] || fail "age recipients file '$BACKUP_AGE_RECIPIENTS_FILE' is missing or empty"
}

# Cifra $1 en $2 y borra el fichero en claro.
encrypt() {
    age --encrypt --recipients-file "$BACKUP_AGE_RECIPIENTS_FILE" --output "$2" "$1"
    rm -f "$1"
}

dump_database() {
    local container="$1" user="$2" database="$3"
    local plain="$WORK/${database}.dump"

    log "dumping ${database} from ${container}"
    docker exec "$container" pg_dump --username="$user" --dbname="$database" --format=custom --compress=9 > "$plain"
    [[ -s "$plain" ]] || fail "empty dump for ${database}"
    docker exec -i "$container" pg_restore --list < "$plain" > /dev/null \
        || fail "dump of ${database} is not a readable archive"

    encrypt "$plain" "$OUT/${database}-${STAMP}.dump.age"
}

dump_globals() {
    local container="$1" user="$2"
    local plain="$WORK/${container}-globals.sql"

    log "dumping roles of ${container}"
    docker exec "$container" pg_dumpall --username="$user" --globals-only > "$plain"
    encrypt "$plain" "$OUT/${container}-globals-${STAMP}.sql.age"
}

upload() {
    local destination="${BACKUP_R2_REMOTE}:${BACKUP_R2_BUCKET}/${HOST}/${DAY_PREFIX}/${STAMP}"
    log "uploading to ${destination}"
    rclone copy "$OUT" "$destination" --s3-no-check-bucket --checksum --retries 3
    rclone check "$OUT" "$destination" --one-way --s3-no-check-bucket \
        || fail "uploaded files do not match the local ones"
}

prune_local_copies() {
    find "$BACKUP_DIR" -mindepth 1 -maxdepth 1 -type d -name '20*' \
        -mtime +"$BACKUP_LOCAL_RETENTION_DAYS" -exec rm -rf {} +
}

main() {
    require_tools
    mkdir -p "$BACKUP_DIR"
    OUT="${BACKUP_DIR}/${STAMP}"
    WORK="$(mktemp -d "${BACKUP_DIR}/.work.XXXXXX")"
    trap 'rm -rf "$WORK"' EXIT
    mkdir -p "$OUT"

    local target container user database
    declare -A globals_done=()
    for target in $BACKUP_TARGETS; do
        IFS=: read -r container user database <<< "$target"
        [[ -n "$container" && -n "$user" && -n "$database" ]] || fail "malformed target '${target}'"
        dump_database "$container" "$user" "$database"
        if [[ -z "${globals_done[$container]:-}" ]]; then
            dump_globals "$container" "$user"
            globals_done[$container]=1
        fi
    done

    (cd "$OUT" && sha256sum -- *.age > SHA256SUMS)

    if [[ "$BACKUP_SKIP_UPLOAD" == "1" ]]; then
        log "BACKUP_SKIP_UPLOAD=1: keeping the backup only in ${OUT}"
    else
        upload
    fi

    prune_local_copies
    log "backup ${STAMP} completed: $(ls "$OUT" | wc -l) files, $(du -sh "$OUT" | cut -f1)"

    if [[ -n "$BACKUP_HEARTBEAT_URL" ]]; then
        curl -fsS --max-time 10 --retry 3 "$BACKUP_HEARTBEAT_URL" > /dev/null || log "WARNING: heartbeat ping failed"
    fi
}

main "$@"
