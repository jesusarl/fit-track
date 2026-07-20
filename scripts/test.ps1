# Ejecuta las pruebas unitarias dentro de un contenedor Maven (no requiere Java/Maven local)
Set-Location $PSScriptRoot\..

Write-Host "Ejecutando pruebas en contenedor Maven..." -ForegroundColor Cyan
docker compose --profile tools run --rm maven mvn test -B
exit $LASTEXITCODE
