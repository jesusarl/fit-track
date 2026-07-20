pipeline {
    agent any

    environment {
        IMAGE_NAME = 'fit-track'
        IMAGE_TAG = "${env.BUILD_NUMBER}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Test') {
            steps {
                sh 'docker compose --profile tools run --rm maven mvn test -B'
            }
        }

        stage('Build Image') {
            steps {
                script {
                    docker.build("${IMAGE_NAME}:${IMAGE_TAG}")
                }
            }
        }

        stage('Deploy') {
            steps {
                echo 'Placeholder: desplegar imagen ${IMAGE_NAME}:${IMAGE_TAG} al entorno objetivo'
                // Ejemplo futuro:
                // sh 'docker push ${DOCKER_REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}'
                // sh 'kubectl set image deployment/fit-track app=${DOCKER_REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}'
            }
        }
    }

    post {
        always {
            cleanWs()
        }
        failure {
            echo 'Pipeline falló. Revisar logs de las etapas anteriores.'
        }
        success {
            echo "Pipeline completado. Imagen: ${IMAGE_NAME}:${IMAGE_TAG}"
        }
    }
}
