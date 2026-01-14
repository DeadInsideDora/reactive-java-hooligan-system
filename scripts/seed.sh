#!/usr/bin/env bash
set -euo pipefail


if [ -f "${BASH_SOURCE%/*}/../.env" ]; then
  set -a
  . "${BASH_SOURCE%/*}/../.env"
  set +a
fi

API="http://localhost:8080"
ADMIN_USER="${HOOLIGUNS_ADMIN_USERNAME:-s000000}"
ADMIN_PASS="${HOOLIGUNS_ADMIN_PASSWORD:-admin}"
ADMIN_AUTH="${ADMIN_USER}:${ADMIN_PASS}"

create_user() {
  local username="$1" password="$2" display="$3" role="$4" faculty="$5" group="$6"
  curl -sS -X POST "$API/api/users" \
    -u "$ADMIN_AUTH" \
    -H "Content-Type: application/json" \
    -d @- <<EOF
{"username":"$username","password":"$password","displayName":"$display","role":"$role","faculty":"$faculty","groupName":"$group"}
EOF
  echo
}

create_incident() {
  local title="$1" desc="$2" place="$3" dept="$4" type="$5" occurred="$6" offender="$7" punishment="$8"
  curl -sS -X POST "$API/api/incidents" \
    -u "$ADMIN_AUTH" \
    -H "Content-Type: application/json" \
    -d @- <<EOF
{"title":"$title","description":"$desc","place":"$place","department":"$dept","type":"$type","occurredAt":"$occurred","offenderId":"$offender","punishment":"$punishment"}
EOF
  echo
}

echo "Создаю пользователей..."
create_user s336462 dora "dora" STUDENT "ФПИиКТ" "Р4116"
create_user s335094 koban "koban-arseny" STUDENT "ФПИиКТ" "Р4116"
create_user s105395 ksv "КСВ" TEACHER "ФПИиКТ" ""
create_user s105233 nina "nina" TEACHER "ФПИиКТ" ""
create_user s451320 matvei "matvei" STUDENT "ФИТиП" ""
create_user s222222 bob "Боб Патриот" STUDENT "ФБИТ" "К314"
create_user s333333 mary "Mary Jane" STUDENT "ФЭкотехнологий" "Э201"
create_user s444444 ivan "Иван Иваныч" TEACHER "ФПИ" ""
create_user s555555 kate "Kate Flow" STUDENT "ФТИИ" "Т101"

echo "Создаю инциденты..."
create_incident "Ел траву со стены аудитории" "Студент решил попробовать декоративный мох." "1327" "ГК" "OTHER" "2026-01-10T10:30:00Z" s336462 "WARNING"
create_incident "Курил в туалете" "Дымовая завеса на этаже, жаловались студенты." "Туалет 3й этаж" "ГК" "AGGRESSION" "2026-01-09T08:15:00Z" s335094 "REPRIMAND"
create_incident "Матерился матом" "Громко ругался во время пары." "1330" "ГК" "DISRUPTION" "2026-01-11T12:40:00Z" s336462 "NONE"
create_incident "Плагиат" "Работа совпадает с прошлогодней на 90%." "Онлайн" "ГК" "CHEATING" "2026-01-07T18:00:00Z" s451320 "SUSPEND"
create_incident "Подделал медицинскую справку" "Печать наклеена из Word." "Деканат" "Биржа" "CHEATING" "2026-01-05T14:20:00Z" s333333 "EXPULSION"
create_incident "Распитие алкоголя" "Праздновали сдачу проекта прямо в аудитории." "2305/1" "ГК" "OTHER" "2026-01-12T17:10:00Z" s222222 "SUSPEND"
create_incident "Плагиат: реферат из интернета" "Текст из Википедии без ссылок." "Онлайн" "ГК" "CHEATING" "2026-01-13T09:00:00Z" s555555 "WARNING"

echo "Готово."
