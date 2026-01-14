set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SCHEMA_FILE="${ROOT_DIR}/src/main/resources/schema.sql"

if [ -f "${ROOT_DIR}/.env" ]; then
  set -a
  . "${ROOT_DIR}/.env"
  set +a
fi

DB_NAME="${DB_NAME:-hooliguns}"
DB_USER="${DB_USER:-hooliguns}"
DB_PASS="${DB_PASS:-hooliguns}"
ADMIN_USER="${HOOLIGUNS_ADMIN_USERNAME:-s000000}"
ADMIN_PASS="${HOOLIGUNS_ADMIN_PASSWORD:-admin}"
IMMORTAL_USER="${HOOLIGUNS_IMMORTAL_USERNAME:-s999999}"
IMMORTAL_PASS="${HOOLIGUNS_IMMORTAL_PASSWORD:-immortal}"

compose() {
  docker compose -f "${ROOT_DIR}/docker-compose.yml" "$@"
}

compose up -d postgres mongo

compose exec -T \
  -e PGPASSWORD="${DB_PASS}" \
  postgres psql -U "${DB_USER}" -d "${DB_NAME}" <<'SQL'
DROP SCHEMA public CASCADE;
CREATE SCHEMA public;
GRANT ALL ON SCHEMA public TO public;
SQL

cat "${SCHEMA_FILE}" | compose exec -T \
  -e PGPASSWORD="${DB_PASS}" \
  postgres psql -U "${DB_USER}" -d "${DB_NAME}"

compose exec -T \
  -e PGPASSWORD="${DB_PASS}" \
  postgres psql -U "${DB_USER}" -d "${DB_NAME}" \
  -v admin_user="${ADMIN_USER}" \
  -v admin_pass="${ADMIN_PASS}" \
  -v immortal_user="${IMMORTAL_USER}" \
  -v immortal_pass="${IMMORTAL_PASS}" <<'SQL'
CREATE EXTENSION IF NOT EXISTS pgcrypto;

INSERT INTO users (id, username, password, display_name, role, created_at)
VALUES (:'admin_user', :'admin_user', crypt(:'admin_pass', gen_salt('bf')), 'Administrator', 'ADMIN', now())
ON CONFLICT (id) DO UPDATE
SET username = EXCLUDED.username,
    password = EXCLUDED.password,
    display_name = EXCLUDED.display_name,
    role = EXCLUDED.role,
    created_at = EXCLUDED.created_at;

INSERT INTO users (id, username, password, display_name, role, created_at)
VALUES (:'immortal_user', :'immortal_user', crypt(:'immortal_pass', gen_salt('bf')), 'Immortal', 'IMMORTAL', now())
ON CONFLICT (id) DO UPDATE
SET username = EXCLUDED.username,
    password = EXCLUDED.password,
    display_name = EXCLUDED.display_name,
    role = EXCLUDED.role,
    created_at = EXCLUDED.created_at;
SQL

compose exec -T mongo mongosh "${DB_NAME}" --quiet --eval "db.dropDatabase()"
