# Material de apoyo para el informe — FitTrack

> Este documento **no es el informe**. Es un banco de información técnica y conceptual, organizado según la estructura pedida, para que puedas redactar el informe con facilidad.
>
> Repo: `https://github.com/jesusarl/fit-track`  
> Proyecto académico de Computación en la Nube / DevOps.

---

## 1. Introducción — material usable

### Qué es FitTrack
- Aplicación web para registrar usuarios y entrenamientos de **running** y **ciclismo**.
- Calcula automáticamente el **ritmo promedio** (minutos por km = tiempo / distancia).
- Expone una **API REST** y una **interfaz web** usable en el navegador.
- El foco del proyecto no es solo la app, sino la **automatización**: contenedores, CI/CD y despliegue en la nube.

### Contexto académico / objetivo del proyecto
- Demostrar un ciclo de vida de software en la nube:
  - desarrollo local reproducible,
  - integración continua (pruebas + empaquetado),
  - entrega/despliegue continuo hacia un entorno cloud.
- Stack original pedido: Java 17, Spring Boot 3, PostgreSQL, Docker, Terraform (AWS), Jenkins.
- Ajuste práctico: el despliegue cloud se realizó en **Render** (alternativa a AWS por dificultades para crear cuenta), manteniendo Terraform AWS en el repo como diseño de IaC.

### Conceptos clave a mencionar en la introducción
| Concepto | Definición breve |
|----------|------------------|
| Contenedor | Empaqueta app + dependencias para ejecutar igual en cualquier máquina |
| CI (Continuous Integration) | Automatizar build y tests en cada cambio de código |
| CD (Continuous Delivery/Deployment) | Automatizar (o disparar) el despliegue tras validar el código |
| IaC (Infrastructure as Code) | Definir infraestructura en archivos versionados (Terraform, `render.yaml`) |
| Pipeline | Secuencia de etapas automatizadas (Checkout → Test → Build → Deploy) |

### Entregables / artefactos del proyecto
- Código fuente Spring Boot + UI estática
- `Dockerfile` multi-stage
- `docker-compose.yml` (desarrollo local)
- `Jenkinsfile` + Jenkins en Docker
- `render.yaml` (Blueprint cloud)
- `/terraform` (diseño AWS: VPC, EC2, RDS)
- Pruebas unitarias del cálculo de ritmo
- `README.md` de uso para el equipo

---

## 2. El problema y la propuesta — material usable

### Problema
1. **Desarrollo inconsistente entre compañeros**: distintos SO, versiones de Java/Maven/PostgreSQL → “en mi máquina funciona”.
2. **Proceso manual de build/test/deploy**: propenso a errores, lento, difícil de auditar.
3. **Falta de entorno cloud** para demostrar la app fuera del localhost.
4. **Necesidad académica** de aplicar prácticas de Computación en la Nube: contenedores, CI/CD, IaC.

### Propuesta
Construir **FitTrack** como sistema completo donde:

1. La app (API + UI + BD) corre de forma **reproducible con Docker**.
2. **Jenkins** ejecuta un pipeline que valida el código y construye la imagen.
3. Si las pruebas pasan, Jenkins **dispara el despliegue en Render** mediante un Deploy Hook.
4. Existe además una definición Terraform para AWS (diseño / alternativa futura).

### Alcance funcional (negocio)
- Usuarios: `id`, `nombre`, `email`
- Entrenamientos: tipo (`RUNNING` / `CYCLING`), distancia (km), tiempo (min), fecha, ritmo promedio, usuario asociado
- Ritmo = `tiempo_minutos / distancia_km` (calculado al guardar)
- Consulta de historial por usuario con **distancia total** y **tiempo total**

### Alcance no funcional (DevOps)
- Un solo requisito de entorno local: Docker Desktop
- Pipeline CI/CD con 4 etapas
- Despliegue cloud con PostgreSQL gestionado
- Documentación para el equipo

---

## 3. Análisis del problema — material usable

### Causas del problema “en mi máquina funciona”
- Dependencias locales no versionadas (JDK, Maven, Postgres)
- Configuración distinta de puertos/credenciales
- Ausencia de un entorno idéntico entre desarrollo y CI

### Riesgos de un flujo manual
- Olvidar correr tests antes de publicar
- Desplegar código roto a producción
- No saber qué versión está en la nube
- Difícil colaboración en equipo

### Requisitos derivados del análisis

**Funcionales**
- CRUD básico de usuarios y workouts vía API
- UI para probar sin Postman
- Estadísticas acumuladas por usuario

**No funcionales**
- Portabilidad (Docker)
- Automatización (Jenkins)
- Disponibilidad demo en internet (Render)
- Trazabilidad (GitHub + logs de pipeline + eventos de deploy)

### Alternativas evaluadas (para argumentar en el informe)

| Alternativa | Pros | Contras | Decisión |
|-------------|------|---------|----------|
| Solo local (sin cloud) | Simple | No demuestra despliegue real | Insuficiente |
| AWS + Terraform + EC2/RDS | Alineado al enunciado original | Barreras de registro/cuenta | Diseñado, no aplicado |
| LocalStack (simular AWS) | Practica Terraform sin cuenta | No es un deploy público real | No elegido |
| **Render (PaaS)** | Cuenta fácil, Docker + Postgres, URL pública | Plan free con límites | **Elegido para deploy** |
| Railway / Fly.io | Similar a Render | Menos alineado a lo ya preparado | Descartado |

### Restricciones
- Plan free de Render: sleep tras inactividad; Postgres free con caducidad (~30 días)
- Jenkins local (Docker) en la máquina del desarrollador, no un Jenkins cloud compartido
- Secrets (Deploy Hook) fuera del repositorio

---

## 4. Diseño de la solución — material usable

### Arquitectura lógica

```text
[ Navegador / Cliente ]
          |
          v
   FitTrack App (Spring Boot)
   - UI estática (/)
   - API REST (/api/...)
   - Actuator health (/actuator/health)
          |
          v
   PostgreSQL
```

### Arquitectura de entornos

```text
DESARROLLO LOCAL
  Docker Compose: app + db
  Scripts: up / down / test

CI (máquina del equipo)
  Jenkins (contenedor)
    → clona GitHub
    → mvn test
    → docker build
    → POST Deploy Hook

PRODUCCIÓN / DEMO
  Render
    - Web Service (Docker)
    - PostgreSQL managed
```

### Flujo CI/CD completo (detalle — sección clave del informe)

```text
1. Desarrollador hace commit/push a GitHub (rama main)
2. Jenkins detecta el cambio automáticamente (pollSCM cada ~2 min)
   - Alternativa avanzada: webhook GitHub→Jenkins (requiere URL pública/túnel)
   - "Build Now" manual solo hace falta la primera vez o para forzar un build
3. Jenkins Pipeline (Jenkinsfile):
   a. CHECKOUT  → descarga el código del SCM (GitHub)
   b. TEST      → `mvn test` (valida lógica, p.ej. ritmo)
   c. BUILD IMAGE → `docker build` genera imagen `fit-track:<BUILD_NUMBER>`
   d. DEPLOY    → si existe credencial `render-deploy-hook`:
                  `curl -X POST <URL_DEL_HOOK>`
4. Render recibe el hook y:
   - clona/construye desde el repo (Dockerfile)
   - despliega nueva versión del web service
   - mantiene conexión a PostgreSQL via variables de entorno
5. Usuario final abre la URL pública de Render y usa la UI
```

**Estado del automatismo**
- Auto-Deploy de Render: desactivado (correcto; evita deploy sin pasar por tests).
- Trigger Jenkins: `pollSCM` en el `Jenkinsfile` (no requiere Build Now en cada push).
- Deploy Render: solo vía Deploy Hook cuando el pipeline llega a Deploy con éxito.

**Por qué el Deploy Hook y no solo Auto-Deploy de Render**
- Auto-Deploy despliega en cada push **sin** garantizar que Jenkins haya pasado tests.
- El hook desde Jenkins permite la política: **solo desplegar si Test (y Build) tuvieron éxito**.
- Recomendación de diseño: desactivar Auto-Deploy en Render para evitar doble deploy.

### Componentes y responsabilidades

| Componente | Responsabilidad | Tecnología |
|------------|-----------------|------------|
| `FitTrackApplication` | Arranque Spring Boot | Java 17 |
| Controllers | Exponer endpoints HTTP | Spring Web |
| Services | Lógica de negocio (ritmo, agregados) | Spring |
| Repositories | Persistencia | Spring Data JPA |
| UI (`static/`) | Experiencia de usuario | HTML/CSS/JS |
| PostgreSQL | Almacenamiento | Postgres 16 |
| Docker | Empaquetado y runtime | Dockerfile multi-stage |
| Docker Compose | Orquestación local app+db | Compose |
| Jenkins | Orquestación CI/CD | Jenkins LTS + Pipeline |
| Render | Hosting cloud + DB | Blueprint `render.yaml` |
| Terraform | Diseño IaC AWS | `main.tf`, `variables.tf`, `outputs.tf` |

### Diseño de datos

**User**
- `id` (PK)
- `nombre`
- `email` (único)

**Workout**
- `id` (PK)
- `tipo_ejercicio` (enum: RUNNING, CYCLING)
- `distancia_km`
- `tiempo_minutos`
- `fecha`
- `ritmo_promedio` (derivado)
- `usuario_id` (FK → User)

### Diseño de API

| Método | Endpoint | Entrada | Salida |
|--------|----------|---------|--------|
| POST | `/api/users` | `{nombre, email}` | usuario creado |
| GET | `/api/users` | — | lista de usuarios |
| POST | `/api/workouts` | `{tipoEjercicio, distanciaKm, tiempoMinutos, fecha, usuarioId}` | workout con ritmo |
| GET | `/api/workouts/user/{userId}` | path id | workouts + `distanciaTotalKm` + `tiempoTotalMinutos` |

### Diseño del contenedor (Dockerfile multi-stage)
1. **Stage build**: imagen `maven:3.9-eclipse-temurin-17` → `mvn package`
2. **Stage run**: imagen `eclipse-temurin:17-jre-alpine` → solo el JAR + `curl` (healthcheck)
3. Beneficio: imagen final más liviana (sin Maven ni código fuente)

### Diseño Jenkins
- Jenkins corre en Docker (`docker-compose.jenkins.yml`)
- Imagen custom (`ci/Dockerfile.jenkins`): Jenkins LTS JDK17 + Docker CLI + Maven
- Usa el socket Docker del host para construir imágenes
- Credencial secreta: `render-deploy-hook` (URL del Deploy Hook; no se sube a Git)

### Diseño Render (`render.yaml`)
- Database `fittrack-db` (plan free, Postgres 16)
- Web service `fittrack-app` (runtime docker, plan free)
- Health check: `/actuator/health`
- Env vars `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` inyectadas desde la DB
- `JAVA_TOOL_OPTIONS` para limitar memoria en plan free (512 MB)

### Diseño Terraform AWS (no aplicado en producción del equipo)
- VPC + subnet pública + IGW
- Security groups (app :8080, DB :5432 solo desde app)
- EC2 para la aplicación
- RDS PostgreSQL
- Outputs: IPs, endpoints, IDs
- Motivo de no aplicar: bloqueos/problemas al crear cuenta AWS

---

## 5. Implementación — material usable

### Estructura del repositorio (resumen)
```text
fit-track/
├── src/main/java/com/fittrack/   # app
├── src/main/resources/static/    # UI
├── src/test/                     # tests
├── ci/Dockerfile.jenkins
├── docker-compose.yml
├── docker-compose.jenkins.yml
├── Dockerfile
├── Jenkinsfile
├── render.yaml
├── terraform/
├── scripts/
└── README.md
```

### Backend — capas implementadas
- `model`: `User`, `Workout`, `ExerciseType`
- `repository`: JPA repositories
- `dto`: requests/responses (records)
- `service`: `UserService`, `WorkoutService.calculateRitmoPromedio(...)`
- `controller`: `UserController`, `WorkoutController`
- `exception`: `GlobalExceptionHandler`
- Config: `application.yml` con `DB_*` y `PORT`

### UI
- SPA ligera sin framework: `index.html`, `css/app.css`, `js/app.js`
- Consume la API del mismo origen
- Paneles: inicio, nuevo usuario, registrar entreno, estadísticas

### Local con Docker Compose
- Servicio `db`: Postgres 16 Alpine, healthcheck
- Servicio `app`: build del Dockerfile, depende de db healthy
- Servicio `maven` (profile `tools`): para tests sin instalar Maven
- Scripts PowerShell/Bash: `up`, `down`, `test`, `logs`, `jenkins-up`, `jenkins-down`

### Jenkins — implementación del pipeline
Archivo: `Jenkinsfile`

| Etapa | Qué hace | Herramienta |
|-------|----------|-------------|
| Checkout | Obtiene código del repo Git | Plugin Git / SCM |
| Test | Ejecuta pruebas unitarias | Maven (`mvn test -B`) |
| Build Image | Construye imagen Docker etiquetada con `#build` | Docker Pipeline |
| Deploy | `POST` al Deploy Hook de Render | `curl` + credencial Jenkins |

Comportamiento del Deploy:
- Si existe credencial `render-deploy-hook` → dispara Render y registra éxito en el log
- Si no existe → no rompe el pipeline; omite deploy y muestra instrucciones

### Cómo se conectó Jenkins con Render (pasos reales realizados)
1. En Render: Service → Settings → **Deploy Hook** → crear y copiar URL
2. En Jenkins: Credentials → Secret text → ID `render-deploy-hook`
3. (Recomendado) Desactivar Auto-Deploy en Render
4. Ejecutar **Build Now**
5. Verificar:
   - Console Output: mensaje “Deploy a Render solicitado correctamente”
   - Dashboard Render: aparece un deploy nuevo

### Render — implementación del deploy
- Blueprint desde `render.yaml` o servicio creado desde el repo
- Build: Docker multi-stage en la nube de Render
- Runtime: contenedor escuchando `PORT` (8080 configurado)
- Persistencia: PostgreSQL managed, no en el filesystem del web service

### Terraform
- Código listo en `/terraform`
- No se ejecutó `terraform apply` por falta de cuenta AWS operable
- Queda como evidencia de diseño IaC multi-cloud / alternativa

### Control de versiones
- GitHub: `jesusarl/fit-track`
- Rama principal: `main`
- CI consume el `Jenkinsfile` desde SCM

---

## 6. Pruebas – Ejecución — material usable

### 6.1 Pruebas unitarias
- Archivo: `WorkoutServiceTest`
- Casos:
  - Ritmo correcto para varias combinaciones tiempo/distancia (parametrizado)
  - Error si distancia = 0
  - Error si distancia negativa
- Ejecución local:
  - `.\scripts\test.ps1` o `docker compose --profile tools run --rm maven mvn test -B`
- Ejecución en CI: etapa Test de Jenkins

### 6.2 Pruebas funcionales locales (UI / API)
1. `docker compose up --build -d`
2. Abrir http://localhost:8080
3. Crear usuario
4. Registrar entrenamiento (ej. 5 km, 30 min → ritmo 6.0)
5. Ver estadísticas (distancia total, tiempo total, historial)
6. Health: http://localhost:8080/actuator/health → `{"status":"UP"}`

### 6.3 Pruebas del pipeline Jenkins
1. Arrancar Jenkins: `.\scripts\jenkins-up.ps1` → http://localhost:8081
2. Job Pipeline from SCM apuntando al repo
3. **Build Now**
4. Criterios de éxito:
   - Etapas en verde
   - Reportes JUnit disponibles (surefire)
   - Imagen Docker `fit-track:<n>` creada localmente
   - Log Deploy con confirmación del hook (si hay credencial)

### 6.4 Pruebas de despliegue en Render
1. Abrir URL pública del servicio (ej. `https://fittrack-app.onrender.com`)
2. Repetir flujo UI: usuario → workout → estadísticas
3. Tras un Build Jenkins exitoso, comprobar en Render → Events/Deploys un deploy nuevo
4. Nota: en plan free, cold start puede tardar 30–60 s

### 6.5 Evidencias sugeridas para el informe (capturas)
- UI local y UI en Render
- Console Output de Jenkins (Test, Build Image, Deploy)
- Evento de deploy en Render
- Resultado de `mvn test` / JUnit
- Diagrama del flujo CI/CD (puedes redibujar el de la sección 4)
- Fragmentos relevantes: `Jenkinsfile`, `Dockerfile`, `render.yaml`

### 6.6 Resultados observados (hechos del proyecto)
- Pipeline Jenkins: **SUCCESS** (Checkout, Test, Build Image, Deploy)
- Integración Jenkins→Render: confirmada (mensaje en log + deploy nuevo en Render)
- App usable por navegador en local y en cloud

---

## 7. Conclusiones — ideas y hechos para redactar

### Logros
- Se construyó una aplicación completa (API + UI + BD) orientada a un caso de uso claro (FitTrack).
- Se eliminó la dependencia de instalar Java/Maven/Postgres localmente mediante Docker.
- Se implementó un pipeline CI/CD real con Jenkins (4 etapas).
- Se logró despliegue en la nube (Render) y se cerró el ciclo Deploy desde Jenkins con Deploy Hook.
- Se documentó el uso para el equipo (`README.md`).
- Se dejó preparado Terraform para AWS como diseño de infraestructura alternativa.

### Aprendizajes
- CI/CD no es solo “subir código”: es **validar antes de publicar**.
- Los contenedores unifican entornos de desarrollo, CI y (parcialmente) producción.
- PaaS (Render) acelera el deploy cuando IaaS (AWS) no está disponible.
- Los secretos (hooks, passwords) deben vivir fuera del repositorio (Jenkins Credentials / env de la plataforma).

### Limitaciones
- Render free: sleep, límites de recursos, Postgres temporal
- Jenkins no está alojado en la nube (corre en Docker local)
- Terraform AWS no se aplicó en un entorno real
- No hay autenticación de usuarios en la app (fuera del alcance inicial)

### Trabajo futuro (para cerrar el informe)
- Webhook GitHub → Jenkins (build automático sin “Build Now” manual)
- Cuenta AWS + `terraform apply` + deploy a EC2/RDS
- Ambientes staging/producción separados
- Monitoreo y alertas
- Autenticación y autorización

### Conclusión central (tesis del informe)
FitTrack demuestra que es posible combinar una aplicación de negocio sencilla con prácticas modernas de Computación en la Nube: **contenedores para reproducibilidad**, **Jenkins para integración continua** y **despliegue automatizado en un proveedor cloud (Render)**, manteniendo además un diseño de infraestructura como código (Terraform/AWS y Blueprint de Render).

---

## Anexo A — Flujo paso a paso “qué sucede” (para explicar en defensa/oral)

1. **Escribes código** (por ejemplo un cambio en el cálculo o en la UI).
2. **Lo subes a GitHub** (`git push` a `main`).
3. **Jenkins toma ese código** (Checkout desde SCM).
4. **Maven corre los tests** dentro del agente Jenkins. Si fallan, el pipeline se detiene: **no debería desplegarse** nada nuevo vía el hook.
5. Si los tests pasan, Jenkins **construye una imagen Docker** de la app (empaquetado reproducible).
6. Jenkins lee la credencial secreta del Deploy Hook y hace un **HTTP POST** a Render.
7. Render interpreta eso como “haz un nuevo deploy”: construye desde el Dockerfile del repo y actualiza el servicio web.
8. La app en Render sigue usando el PostgreSQL gestionado (variables `DB_*`).
9. Un usuario entra por HTTPS a la URL de Render y usa la interfaz; la UI llama a `/api/...` en el mismo host.

Herramientas en esa cadena: **Git/GitHub, Jenkins, Maven, JUnit, Docker, curl, Render, PostgreSQL, Spring Boot**.

---

## Anexo B — Glosario rápido

| Término | Significado en este proyecto |
|---------|------------------------------|
| Deploy Hook | URL secreta de Render que al recibir POST inicia un deploy |
| Blueprint | Definición IaC de Render (`render.yaml`) |
| Multi-stage build | Dockerfile con etapa de compilación y etapa de ejecución |
| Agent | Máquina/contenedor donde Jenkins ejecuta el pipeline |
| SCM | Source Code Management (aquí, GitHub) |
| Health check | Endpoint que indica si la app está viva (`/actuator/health`) |
| Ritmo promedio | `tiempo_minutos / distancia_km` |

---

## Anexo C — Referencias de archivos del repo

| Tema | Archivo |
|------|---------|
| Pipeline CI/CD | `Jenkinsfile` |
| Deploy cloud | `render.yaml` |
| Contenedor app | `Dockerfile` |
| Local app+db | `docker-compose.yml` |
| Jenkins local | `docker-compose.jenkins.yml`, `ci/Dockerfile.jenkins` |
| IaC AWS | `terraform/main.tf`, `variables.tf`, `outputs.tf` |
| Lógica ritmo | `src/main/java/com/fittrack/service/WorkoutService.java` |
| Tests ritmo | `src/test/java/com/fittrack/service/WorkoutServiceTest.java` |
| Guía de uso | `README.md` |
| Enunciado inicial | `INITIAL-PROMPT.md` |
