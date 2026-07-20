# Levanta la aplicacion y PostgreSQL en contenedores
Set-Location $PSScriptRoot\..

Write-Host "Construyendo e iniciando FitTrack (app + base de datos)..." -ForegroundColor Cyan
docker compose up --build -d

Write-Host ""
Write-Host "Esperando a que la app este lista..." -ForegroundColor Yellow
$retries = 30
for ($i = 1; $i -le $retries; $i++) {
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -UseBasicParsing -TimeoutSec 3
        if ($response.StatusCode -eq 200) {
            Write-Host "FitTrack listo en http://localhost:8080" -ForegroundColor Green
            Write-Host "Abre el navegador en http://localhost:8080 para usar la aplicacion." -ForegroundColor Cyan
            exit 0
        }
    } catch {
        Start-Sleep -Seconds 2
    }
}

Write-Host "La app aun esta arrancando. Revisa los logs con: docker compose logs -f app" -ForegroundColor Yellow
