#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

echo "Deteniendo FitTrack..."
docker compose down
echo "Contenedores detenidos."
