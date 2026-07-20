pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
    }

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
                sh 'mvn test -B'
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Build Image') {
            steps {
                script {
                    docker.build("${IMAGE_NAME}:${IMAGE_TAG}")
                    sh "docker tag ${IMAGE_NAME}:${IMAGE_TAG} ${IMAGE_NAME}:latest"
                }
            }
        }

        stage('Deploy') {
            steps {
                script {
                    // Dispara un nuevo deploy en Render solo si existe la credencial.
                    // ID esperado: render-deploy-hook (Secret text con la URL del Deploy Hook).
                    try {
                        withCredentials([string(credentialsId: 'render-deploy-hook', variable: 'RENDER_DEPLOY_HOOK')]) {
                            echo "Disparando deploy en Render (build ${IMAGE_TAG})..."
                            sh 'curl -fsS -X POST "$RENDER_DEPLOY_HOOK"'
                            echo 'Deploy a Render solicitado correctamente.'
                        }
                    } catch (err) {
                        echo "Omitiendo Deploy a Render: no hay credencial 'render-deploy-hook' o falló el hook."
                        echo 'Pasos: Render → Service → Settings → Deploy Hook → copiar URL.'
                        echo 'Luego en Jenkins: Manage Jenkins → Credentials → Add → Secret text (ID: render-deploy-hook).'
                        echo "Detalle: ${err}"
                    }
                }
            }
        }
    }

    post {
        success {
            echo "Pipeline OK. Imagen: ${IMAGE_NAME}:${IMAGE_TAG}"
        }
        failure {
            echo 'Pipeline falló. Revisa los logs de Test o Build Image.'
        }
    }
}
