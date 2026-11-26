#!/bin/sh
# Loads LankaTrails demo data into the docker-compose Postgres. Safe to re-run.
#
#   sh docker/seed/load-seed.sh
#
# Demo accounts (password = DEMO_USER_PASSWORD from .env):
#   olanka@lankatrails.dev    provider (Olanka Travels, 18 services)
#   tourist@lankatrails.dev   tourist  (trips, bookings, reviews, complaints)
#   tourist2@lankatrails.dev  tourist
# The admin account comes from ADMIN_EMAIL / ADMIN_PASSWORD and is not touched.
set -eu

cd "$(dirname "$0")/../.."
compose="docker compose -f docker-compose.dev.yml"

password=$(sed -n 's/^DEMO_USER_PASSWORD=//p' .env 2>/dev/null | tail -n 1 | tr -d '\r"')
if [ -z "$password" ]; then
  echo "Set DEMO_USER_PASSWORD in .env first (see .env.example)." >&2
  exit 1
fi

# Placeholder images and documents referenced by the seed (served from /uploads)
mkdir -p uploads
cp -R docker/seed/uploads/. uploads/

DEMO_PASSWORD="$password" $compose exec -T -e DEMO_PASSWORD postgres \
  sh -c 'psql -X -q -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -d "$POSTGRES_DB" -v demo_password="$DEMO_PASSWORD"' \
  < docker/seed/seed-demo-data.sql

echo "Demo data loaded."
