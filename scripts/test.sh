#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

echo "Ejecutando pruebas en contenedor Maven..."
docker compose --profile tools run --rm maven mvn test -B
