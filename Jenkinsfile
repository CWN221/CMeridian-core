pipeline {
    agent any

    options {
        timestamps()
        timeout(time: 30, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '20'))
        disableConcurrentBuilds()
    }

    environment {
        REGISTRY        = 'docker.io'
        IMAGE_NAMESPACE = "${env.DOCKER_HUB_USERNAME ?: 'cmeridian-bank'}"
        IMAGE_NAME      = 'meridian-core-backend'
        IMAGE_TAG       = "${env.BUILD_NUMBER}-${env.GIT_COMMIT.take(7)}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                sh 'git rev-parse --short HEAD'
                sh 'java -version'
                sh 'mvn -version'
                sh 'docker version'
                sh 'docker compose version'
            }
        }

        stage('Backend build and unit tests') {
            steps {
                dir('backend') {
                    sh 'mvn -B clean compile'
                    sh 'mvn -B test'
                }
            }
            post {
                always {
                    junit allowEmptyResults: true,
                        testResults: 'backend/target/surefire-reports/*.xml'
                    archiveArtifacts allowEmptyArchive: true,
                        artifacts: 'backend/target/*.jar',
                        fingerprint: true
                }
            }
        }

        stage('Build Docker image') {
            steps {
                script {
                    def image = "${REGISTRY}/${IMAGE_NAMESPACE}/${IMAGE_NAME}:${IMAGE_TAG}"
                    def latest = "${REGISTRY}/${IMAGE_NAMESPACE}/${IMAGE_NAME}:latest"
                    sh "docker build -t ${image} backend/"
                    sh "docker tag ${image} ${latest}"
                }
            }
        }

        stage('Push Docker image') {
            when {
                branch 'main'
            }
            steps {
                script {
                    if (env.DOCKER_HUB_USERNAME?.trim()) {
                        withCredentials([usernamePassword(
                                credentialsId: 'docker-hub-credentials',
                                usernameVariable: 'DOCKER_USER',
                                passwordVariable: 'DOCKER_PASS')]) {
                            def image = "${REGISTRY}/${IMAGE_NAMESPACE}/${IMAGE_NAME}:${IMAGE_TAG}"
                            def latest = "${REGISTRY}/${IMAGE_NAMESPACE}/${IMAGE_NAME}:latest"
                            sh "echo \$DOCKER_PASS | docker login -u \$DOCKER_USER --password-stdin ${REGISTRY}"
                            sh "docker push ${image}"
                            sh "docker push ${latest}"
                        }
                    } else {
                        echo 'DOCKER_HUB_USERNAME not set, skipping push'
                    }
                }
            }
        }
    }

    post {
        success {
            echo "Build #${env.BUILD_NUMBER} succeeded on ${env.BRANCH_NAME}"
        }
        failure {
            echo "Build #${env.BUILD_NUMBER} FAILED on ${env.BRANCH_NAME}"
        }
    }
}