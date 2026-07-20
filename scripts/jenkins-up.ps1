# Levanta Jenkins en Docker (UI en http://localhost:8081)
Set-Location $PSScriptRoot\..

Write-Host "Construyendo e iniciando Jenkins..." -ForegroundColor Cyan
docker compose -f docker-compose.jenkins.yml up --build -d

Write-Host ""
Write-Host "Esperando a que Jenkins arranque (puede tardar 1-2 minutos la primera vez)..." -ForegroundColor Yellow

$ready = $false
for ($i = 1; $i -le 60; $i++) {
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8081/login" -UseBasicParsing -TimeoutSec 3
        if ($response.StatusCode -eq 200) {
            $ready = $true
            break
        }
    } catch {
        Start-Sleep -Seconds 3
    }
}

if (-not $ready) {
    Write-Host "Jenkins aun esta arrancando. Revisa: docker compose -f docker-compose.jenkins.yml logs -f" -ForegroundColor Yellow
    exit 0
}

Write-Host ""
Write-Host "Jenkins listo: http://localhost:8081" -ForegroundColor Green
Write-Host ""
Write-Host "Password inicial de administrador:" -ForegroundColor Cyan
docker exec fittrack-jenkins cat /var/jenkins_home/secrets/initialAdminPassword
Write-Host ""
Write-Host "Copialo y pegalo en la pantalla de setup de Jenkins." -ForegroundColor Yellow
