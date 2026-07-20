# FitTrack

Aplicación de seguimiento de entrenamientos (running / ciclismo) con API REST, interfaz web, CI con Jenkins y despliegue en la nube con Render.

## Stack

| Capa | Tecnología |
|------|------------|
| Backend | Java 17, Spring Boot 3, Maven |
| Base de datos | PostgreSQL |
| UI | HTML / CSS / JS (servida por Spring) |
| Contenedores | Docker, Docker Compose |
| CI | Jenkins (`Jenkinsfile`) |
| Cloud / IaC | Render (`render.yaml`) |

## Requisitos

Solo necesitas **Docker Desktop** (y Git). No hace falta instalar Java, Maven ni PostgreSQL en tu máquina.

## Arranque local (app + base de datos)

```powershell
# Windows
.\scripts\up.ps1
```

```bash
# Linux / macOS
./scripts/up.sh
```

O directamente:

```powershell
docker compose up --build -d
```

- App / UI: http://localhost:8080  
- Health: http://localhost:8080/actuator/health  
- Postgres en el host: puerto `5433` (evita choques con un Postgres local en 5432)

Detener:

```powershell
.\scripts\down.ps1
```

### Pruebas unitarias (en contenedor)

```powershell
.\scripts\test.ps1
```

## Interfaz web

Abre http://localhost:8080 y usa el menú:

1. **Nuevo usuario** — crea un atleta  
2. **Registrar entreno** — distancia, tiempo y fecha (el ritmo se calcula solo)  
3. **Estadísticas** — totales e historial por usuario  

## API REST

| Método | Ruta | Descripción |
|--------|------|-------------|
| `POST` | `/api/users` | Crear usuario |
| `GET` | `/api/users` | Listar usuarios |
| `POST` | `/api/workouts` | Crear entrenamiento |
| `GET` | `/api/workouts/user/{userId}` | Entrenamientos + distancia/tiempo totales |

Ejemplo (PowerShell):

```powershell
Invoke-RestMethod -Uri http://localhost:8080/api/users -Method Post `
  -ContentType "application/json" `
  -Body '{"nombre":"Ana","email":"ana@fittrack.com"}'
```

## Jenkins (CI)

Jenkins corre en Docker con Maven y Docker CLI.

```powershell
.\scripts\jenkins-up.ps1
```

- UI: http://localhost:8081  
- Password inicial (primera vez):

```powershell
docker exec fittrack-jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

### Job Pipeline

1. **New Item** → Pipeline  
2. Definition: **Pipeline script from SCM**  
3. Git URL: `https://github.com/jesusarl/fit-track.git`  
4. Branch: `*/main`  
5. Script Path: `Jenkinsfile`  
6. **Build Now**

Etapas: **Checkout → Test → Build Image → Deploy**

### ¿Se dispara solo al hacer push?

**Sí, con polling** (ya configurado en el `Jenkinsfile`):

- Jenkins revisa GitHub aproximadamente **cada 2 minutos**.
- Si hay commits nuevos en `main`, lanza el build **sin** pulsar Build Now.
- La primera vez (o tras cambiar el `Jenkinsfile`), haz **un** Build Now manual para que Jenkins cargue el trigger.

Flujo automático resultante:

```text
git push → (≤ ~2 min) Jenkins detecta cambios → Test → Build Image → Deploy Hook → Render
```

> Nota: un webhook instantáneo GitHub→Jenkins también es posible, pero necesita que Jenkins sea alcanzable desde internet (p. ej. ngrok). Con Jenkins en `localhost`, el polling es la opción práctica.

### Conectar Deploy de Jenkins → Render

Objetivo: que solo se redeploye en Render **después** de que pasen los tests.

1. En **Render** → tu servicio web (`fittrack-app`) → **Settings** → **Deploy Hook** → Create → copia la URL.  
2. En Render desactiva **Auto-Deploy** del servicio (obligatorio en este proyecto), para que solo despliegue Jenkins tras pasar los tests.  
3. En **Jenkins** → **Manage Jenkins** → **Credentials** → **Add Credentials**:
   - Kind: **Secret text**
   - Secret: la URL del Deploy Hook
   - ID: `render-deploy-hook` (debe ser exactamente este ID)
4. Vuelve a lanzar el job: si Test y Build Image pasan, la etapa Deploy hará `POST` al hook de Render.

Si la credencial no existe, el pipeline **no falla**: omite el deploy y deja un mensaje en el log.

## Render (producción / demo en la nube)

Archivo de infraestructura: [`render.yaml`](render.yaml) (Blueprint).

1. Cuenta en [render.com](https://render.com) (ideal con GitHub).  
2. **New +** → **Blueprint** → repo `fit-track` → branch `main`.  
3. Aplica el Blueprint (web + PostgreSQL free).  
4. Abre la URL pública del servicio (ej. `https://fittrack-app.onrender.com`).

Notas del plan free:

- La web puede “dormirse” tras inactividad; el primer request tarda más.  
- La base free caduca a los ~30 días (útil para demos del curso).

## Estructura del repo

```
fit-track/
├── src/                    # Código Spring Boot + UI estática
├── ci/Dockerfile.jenkins   # Imagen de Jenkins
├── docker-compose.yml      # App + Postgres local
├── docker-compose.jenkins.yml
├── Dockerfile
├── Jenkinsfile
├── render.yaml
└── scripts/                # up / down / test / jenkins-*
```

## Variables de entorno

Copia `.env.example` a `.env` para personalizar puertos locales.  
`.env` no se sube a Git.

| Variable | Uso |
|----------|-----|
| `APP_HOST_PORT` | Puerto local de la app (default 8080) |
| `DB_HOST_PORT` | Puerto local de Postgres (default 5433) |
| `JENKINS_HOST_PORT` | Puerto local de Jenkins (default 8081) |
| `DB_*` | Credenciales de base de datos |

## Flujo DevOps del proyecto

```text
Push a GitHub
    → Jenkins (pollSCM) detecta el cambio
    → Test + Build Image
    → Deploy Hook → Render redespliega
    → App disponible en la URL de Render
```

Localmente el mismo código se prueba con `docker compose up`.
