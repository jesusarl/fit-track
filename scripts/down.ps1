# Detiene y elimina los contenedores del proyecto
Set-Location $PSScriptRoot\..

Write-Host "Deteniendo FitTrack..." -ForegroundColor Cyan
docker compose down
Write-Host "Contenedores detenidos." -ForegroundColor Green
