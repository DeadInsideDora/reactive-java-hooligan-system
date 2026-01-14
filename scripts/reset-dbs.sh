set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SCHEMA_FILE="${ROOT_DIR}/src/main/resources/schema.sql"

DB_NAME="${DB_NAME:-hooliguns}"
DB_USER="${DB_USER:-hooliguns}"
DB_PASS="${DB_PASS:-hooliguns}"

compose() {
  docker compose -f "${ROOT_DIR}/docker-compose.yml" "$@"
}

compose up -d postgres mongo

cat "${SCHEMA_FILE}" | compose exec -T \
  -e PGPASSWORD="${DB_PASS}" \
  postgres psql -U "${DB_USER}" -d "${DB_NAME}"

compose exec -T mongo mongo "${DB_NAME}" --eval "db.dropDatabase()"

