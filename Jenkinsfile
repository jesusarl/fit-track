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
                echo "Placeholder Deploy: imagen ${IMAGE_NAME}:${IMAGE_TAG} lista localmente."
                echo 'Siguiente paso del proyecto: Terraform (AWS) + push a registry + despliegue en EC2.'
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
