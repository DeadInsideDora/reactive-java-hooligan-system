#!/usr/bin/env bash
set -euo pipefail

DB_NAME="${DB_NAME:-hooliguns}"
DB_USER="${DB_USER:-hooliguns}"
DB_PASS="${DB_PASS:-hooliguns}"
POSTGRES_CONTAINER="${POSTGRES_CONTAINER:-reactive-java-hooligan-system-postgres-1}"

if ! docker ps --format '{{.Names}}' | grep -q "^${POSTGRES_CONTAINER}\$"; then
  echo "Container ${POSTGRES_CONTAINER} not found. Set POSTGRES_CONTAINER to the correct name."
  echo "Running containers:"
  docker ps --format '  {{.Names}}'
  exit 1;
fi

echo "Listing tables in ${DB_NAME}..."
docker exec -e PGPASSWORD="${DB_PASS}" -i "${POSTGRES_CONTAINER}" \
  psql -U "${DB_USER}" -d "${DB_NAME}" -c '\dt'

echo
echo "Opening interactive psql shell. Type \\q to exit."
docker exec -e PGPASSWORD="${DB_PASS}" -it "${POSTGRES_CONTAINER}" \
  psql -U "${DB_USER}" -d "${DB_NAME}"
