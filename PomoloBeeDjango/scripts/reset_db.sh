#!/bin/bash

# ─── Accept Password As Argument ──────────────────────────────
if [ -z "$1" ]; then
  echo "❌ ERROR: You must provide a password for superuser 'pomobee'."
  echo "Usage: ./scripts/reset_db.sh <superuser_password>"
  exit 1
fi

SUPERUSER_PASSWORD="$1"

echo "🔐 Superuser 'pomobee' will be assigned the password: $SUPERUSER_PASSWORD"

# ─── Reset PostgreSQL Schema ───────────────────────────────────
echo "🧨 Dropping and recreating schema 'public'..."
sudo -u postgres psql -d pomolobee <<EOF
DROP SCHEMA public CASCADE;
CREATE SCHEMA public;
ALTER SCHEMA public OWNER TO pomolo_user;
GRANT ALL ON SCHEMA public TO pomolo_user;
GRANT ALL ON SCHEMA public TO public;
EOF

# ─── Migrate Django ────────────────────────────────────────────
echo "⚙️ Rebuilding Django DB schema..."
rm -rf core/migrations/*
find . -path "*/__pycache__/*" -delete

source venv/bin/activate
python manage.py makemigrations core
python manage.py migrate

# ─── Load Superuser Fixture (ID=1) ─────────────────────────────
echo "👤 Loading superuser fixture..."
python manage.py loaddata core/fixtures/initial_superuser.json

# ─── Set Superuser Password ────────────────────────────────────
echo "🔑 Updating 'pomobee' password..."
python manage.py shell <<EOF
from django.contrib.auth.models import User
u = User.objects.get(username='pomobee')
u.set_password('$SUPERUSER_PASSWORD')
u.save()
print('✅ Password updated successfully.')
EOF

# ─── Load Other Fixtures ───────────────────────────────────────
echo "🌱 Loading initial data..."
python manage.py loaddata core/fixtures/initial_farms.json
python manage.py loaddata core/fixtures/initial_fields.json
python manage.py loaddata core/fixtures/initial_fruits.json
python manage.py loaddata core/fixtures/initial_raws.json

echo "✅ PomoloBee DB fully reset & initialized."
