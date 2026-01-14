#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

if [ -f "${ROOT_DIR}/.env" ]; then
  set -a
  # shellcheck disable=SC1091
  . "${ROOT_DIR}/.env"
  set +a
fi

MONGO_URI="${HOOLIGUNS_MONGO_URI:-mongodb://localhost:27017/hooliguns}"
CREATED_BY="${HOOLIGUNS_IMMORTAL_USERNAME:-s999999}"

compose() {
  docker compose -f "${ROOT_DIR}/docker-compose.yml" "$@"
}

echo "Запускаю Mongo..."
compose up -d mongo >/dev/null

ALERTS_JSON=$(cat <<'EOF'
[
  { "message": "Студент в 1327 в куртке!", "createdAt": ISODate("2026-01-14T01:47:35Z") },
  { "message": "Студенты с напитками на 3 парте второго ряда 1330", "createdAt": ISODate("2026-01-14T01:47:33Z") },
  { "message": "1333 - проводится занятие без света", "createdAt": ISODate("2026-01-14T01:47:31Z") },
  { "message": "2305/1 не закрыта дверь после занятия в 17:10", "createdAt": ISODate("2026-01-14T01:46:38Z") },
  { "message": "Не выключили оборудование в 1326", "createdAt": ISODate("2026-01-14T01:46:33Z") },
  { "message": "Последнее предупреждение, студент в 1327 в куртке! Срочно исправить", "createdAt": ISODate("2026-01-14T01:46:32Z") }
]
EOF
)

echo "Загружаю алерты в Mongo (${MONGO_URI})..."
compose exec -T mongo mongosh "${MONGO_URI}" --quiet --eval "
  const createdBy = '${CREATED_BY}';
  const docs = ${ALERTS_JSON}.map(a => ({ ...a, createdById: createdBy }));
  db.alerts.deleteMany({});
  db.alerts.insertMany(docs);
  db.alerts.find().sort({ createdAt: -1 }).forEach(d => print(d.createdAt.toISOString(), '|', d.message));
"

echo "Готово."
