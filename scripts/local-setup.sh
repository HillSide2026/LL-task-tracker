#!/usr/bin/env bash
# scripts/local-setup.sh
#
# One-time local development setup.
# Safe to run multiple times — all steps are idempotent.

set -e

echo "→ Adding /etc/hosts entries (skips if already present)..."

add_host() {
  local entry="127.0.0.1 $1"
  if grep -qF "$1" /etc/hosts; then
    echo "  already present: $1"
  else
    echo "$entry" | sudo tee -a /etc/hosts > /dev/null
    echo "  added: $entry"
  fi
}

add_host "matters.localhost"
add_host "auth.localhost"
add_host "corporate.localhost"

echo ""
echo "→ Checking pnpm..."
if command -v pnpm &>/dev/null; then
  echo "  pnpm $(pnpm --version) already installed"
else
  echo "  installing pnpm@10..."
  npm install -g pnpm@10
fi

echo ""
echo "Done. Next steps:"
echo ""
echo "  0. Copy the env template (first time only):"
echo ""
echo "     cp .env.local.example .env.local"
echo ""
echo "  1. Start LL-task-tracker:"
echo ""
echo "     docker compose \\"
echo "       -f docker-compose.yaml \\"
echo "       -f docker-compose.camunda7.yaml \\"
echo "       -f docker-compose.demo-data-loader.camunda7.yaml \\"
echo "       -f docker-compose.portal.yaml \\"
echo "       -f docker-compose.local.yaml \\"
echo "       --env-file .env.local \\"
echo "       up -d --build"
echo ""
echo "  2. Bootstrap (first time only):"
echo ""
echo "     docker compose \\"
echo "       -f docker-compose.yaml \\"
echo "       -f docker-compose.camunda7.yaml \\"
echo "       -f docker-compose.demo-data-loader.camunda7.yaml \\"
echo "       -f docker-compose.portal.yaml \\"
echo "       -f docker-compose.local.yaml \\"
echo "       --env-file .env.local \\"
echo "       run --rm demo-data-loader"
echo ""
echo "  3. Create the ll-corporate Keycloak client (first time only):"
echo "     See ll-corporate/.env.local.example for instructions."
echo ""
echo "  4. Start ll-corporate:"
echo ""
echo "     cd ../ll-corporate"
echo "     cp .env.local.example .env.local     # then fill in AUTH_KEYCLOAK_SECRET"
echo "     pnpm install"
echo "     pnpm dev"
echo ""
echo "  Canonical local URLs:"
echo "    http://matters.localhost      — matter portal"
echo "    http://auth.localhost         — Keycloak admin UI + OIDC"
echo "    http://corporate.localhost    — corporate portal"
echo "    http://localhost:8888         — Traefik dashboard"
