Actúa como un Arquitecto de Software y Especialista en DevOps. Necesito crear la estructura base para un proyecto de Computación en la Nube llamado "FitTrack". 

El objetivo principal es la automatización y el despliegue continuo (CI/CD). Por ahora, genera la estructura del proyecto y los archivos iniciales basados en este stack:

STACK TECNOLÓGICO:
- Backend: Java 17, Spring Boot 3.x, Maven.
- Base de Datos: PostgreSQL.
- Contenedores: Docker y Docker Compose (para desarrollo local).
- Infraestructura como Código: Terraform (configuración básica para AWS: VPC, EC2 para la app y RDS para la base de datos).
- CI/CD: Jenkinsfile.

REQUERIMIENTOS FUNCIONALES (Backend):
1. Entidad 'User' (id, nombre, email).
2. Entidad 'Workout' (id, tipo_ejercicio [RUNNING, CYCLING], distancia_km, tiempo_minutos, fecha, ritmo_promedio, usuario_id).
3. Lógica de negocio: Al guardar un Workout, calcula automáticamente el 'ritmo_promedio' (tiempo / distancia).
4. Endpoints REST:
   - POST /api/users
   - POST /api/workouts
   - GET /api/workouts/user/{userId} (que incluya estadísticas acumuladas: distancia total y tiempo total).

REQUERIMIENTOS DE AUTOMATIZACIÓN (DevOps):
1. Genera el 'Dockerfile' multi-stage para compilar con Maven y ejecutar la app con una imagen liviana de Java.
2. Genera un 'docker-compose.yml' local que levante la app y la base de datos PostgreSQL conectadas.
3. Genera un 'Jenkinsfile' con 4 etapas claras: Checkout, Test (mvn test), Build Image (Docker build), y un placeholder para Deploy.
4. Genera una estructura básica de carpetas de Terraform ('/terraform') con main.tf, variables.tf y outputs.tf configurando un proveedor de AWS básico.

Por favor, crea la estructura de directorios y los archivos clave para empezar. Incluye pruebas unitarias básicas para la lógica del cálculo de ritmo.