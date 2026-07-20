#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

echo "Deteniendo Jenkins..."
docker compose -f docker-compose.jenkins.yml down
echo "Jenkins detenido. (Los datos quedan en el volumen jenkins_home)"
