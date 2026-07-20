#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

echo "Construyendo e iniciando Jenkins..."
docker compose -f docker-compose.jenkins.yml up --build -d

echo ""
echo "Esperando a que Jenkins arranque..."
for _ in $(seq 1 60); do
  if curl -sf http://localhost:8081/login > /dev/null; then
    echo ""
    echo "Jenkins listo: http://localhost:8081"
    echo ""
    echo "Password inicial de administrador:"
    docker exec fittrack-jenkins cat /var/jenkins_home/secrets/initialAdminPassword
    echo ""
    exit 0
  fi
  sleep 3
done

echo "Jenkins aun esta arrancando. Revisa: docker compose -f docker-compose.jenkins.yml logs -f"
