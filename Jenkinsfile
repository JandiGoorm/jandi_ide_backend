pipeline {
    agent any

    environment {
        GHCR_OWNER = 'kyj0503'
        IMAGE_NAME = 'jandi-ide'
        DOCKER_BUILDKIT = '1'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Test') {
            steps {
                script {
                    echo "Running tests..."
                    sh 'CI=true ./gradlew test --no-daemon'
                }
            }
            post {
                always {
                    junit '**/build/test-results/test/*.xml'
                }
            }
        }

        stage('Build and Push to GHCR') {
            steps {
                script {
                    def fullImageName = "ghcr.io/${env.GHCR_OWNER}/${env.IMAGE_NAME}:${env.BUILD_NUMBER}"
                    def latestImageName = "ghcr.io/${env.GHCR_OWNER}/${env.IMAGE_NAME}:latest"
                    
                    echo "Building Docker image with BuildKit cache: ${fullImageName}"
                    
                    // Jenkins 빌드: application.properties.example 복사
                    sh 'cp src/main/resources/application.properties.example src/main/resources/application.properties'
                    
                    // 캐시 재사용을 위해 latest 이미지 pull (없으면 무시)
                    docker.withRegistry("https://ghcr.io", 'github-token') {
                        sh "docker pull ${latestImageName} || true"
                    }
                    
                    // BuildKit 캐시를 활용한 Docker 빌드
                    sh """
                        docker build \
                            --build-arg BUILDKIT_INLINE_CACHE=1 \
                            --cache-from ${latestImageName} \
                            -t ${fullImageName} \
                            -t ${latestImageName} \
                            .
                    """
                    
                    docker.withRegistry("https://ghcr.io", 'github-token') {
                        echo "Pushing Docker images to GHCR..."
                        sh "docker push ${fullImageName}"
                        sh "docker push ${latestImageName}"
                    }
                }
            }
        }

        // 배포는 home-server에서 담당
        stage('Trigger Deploy') {
            steps {
                build job: 'home-server-deploy', wait: false, propagate: false
            }
        }
    }

    post {
        always {
            cleanWs()
        }
        success {
            echo '✅ Build and Push completed successfully!'
        }
        failure {
            echo '❌ Build failed!'
        }
    }
}
