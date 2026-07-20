# Detiene Jenkins
Set-Location $PSScriptRoot\..

Write-Host "Deteniendo Jenkins..." -ForegroundColor Cyan
docker compose -f docker-compose.jenkins.yml down
Write-Host "Jenkins detenido. (Los datos quedan en el volumen jenkins_home)" -ForegroundColor Green
