#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

echo "Construyendo e iniciando FitTrack (app + base de datos)..."
docker compose up --build -d

echo ""
echo "Esperando a que la app este lista..."
for _ in $(seq 1 30); do
  if curl -sf http://localhost:8080/actuator/health > /dev/null; then
    echo "FitTrack listo en http://localhost:8080"
    echo "Abre el navegador en http://localhost:8080 para usar la aplicacion."
    exit 0
  fi
  sleep 2
done

echo "La app aun esta arrancando. Revisa los logs con: docker compose logs -f app"
