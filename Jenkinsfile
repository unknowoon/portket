pipeline {
    agent any

    environment {
        APP_NAME = "portket"
        DOCKER_IMAGE_TAG = "${APP_NAME}:latest"
        DEV_CONTAINER_NAME = "portket-dev"
        MAIN_CONTAINER_NAME = "portket-prod"
        DEV_PORT = "8008"
        APP_PORT = "8080"
        CHANGE_TARGET = "${env.BRANCH_NAME}"

    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                script {
                    echo "Current branch: ${env.BRANCH_NAME}"
                    if (CHANGE_TARGET) {
                        echo "Pull Request target branch: ${env.CHANGE_TARGET}"
                    } else {
                        echo "Not a PR build. Might be a branch build."
                    }
                }
            }
        }

        stage('Prepare') {
          steps {
            sh 'chmod +x gradlew'
          }
        }

        stage('Build & Test') {
            steps {
                sh './gradlew clean build'
            }
        }

        stage('Docker Build') {
            steps {
                sh "docker build -t ${DOCKER_IMAGE_TAG} ."
            }
        }

        stage('Deploy') {
//             when {
//                 expression {
//                     return CHANGE_TARGET != null
//                 }
//             }
            steps {
                script {
                    if (CHANGE_TARGET == "dev") {
                        echo "Target branch is dev. Deploying to Staging environment."
                        sh """
                           docker stop ${DEV_CONTAINER_NAME} || true
                           docker rm ${DEV_CONTAINER_NAME} || true
                        """
                        sh """
                           docker run -d --name ${DEV_CONTAINER_NAME} \
                           --network portket \
                           -p ${DEV_PORT}:${APP_PORT} ${DOCKER_IMAGE_TAG}
                        """
                    } else if (CHANGE_TARGET == "main") {
                        echo "Target branch is main. Deploying to Production environment."
                        sh """
                           docker stop ${MAIN_CONTAINER_NAME} || true
                           docker rm ${MAIN_CONTAINER_NAME} || true
                        """
                        sh """
                           docker run -d --name ${MAIN_CONTAINER_NAME} \
                           --network portket \
                           -p ${APP_PORT}:${APP_PORT} ${DOCKER_IMAGE_TAG}
                        """
                    } else {
                        echo "PR target is neither dev nor main. No deployment."
                    }
                }
            }
        }
    }

    post {
        always {
            echo "Pipeline finished. Check above logs for details."
        }
        success {
            echo "Build and (if PR) deploy completed successfully."
        }
        failure {
            echo "Build or deploy failed."
        }
    }
}
