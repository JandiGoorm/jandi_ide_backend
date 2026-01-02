pipeline {
    agent any

    environment {
        GHCR_OWNER = 'kyj0503'
        EC2_HOST = 'ide.yeonjae.kr'
        EC2_USER = 'ubuntu'
        IMAGE_NAME = 'web-ide'
        DOCKER_BUILDKIT = '1'  // BuildKit 활성화
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
                    // HTML 리포트는 build/reports/tests/test/index.html에서 직접 확인 가능
                }
            }
        }

        stage('Build and Push to GHCR') {
            steps {
                script {
                    def fullImageName = "ghcr.io/${env.GHCR_OWNER}/${env.IMAGE_NAME}:${env.BUILD_NUMBER}"
                    def latestImageName = "ghcr.io/${env.GHCR_OWNER}/${env.IMAGE_NAME}:latest"
                    
                    echo "Building Docker image with BuildKit cache: ${fullImageName}"
                    
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

        stage('Deploy to EC2') {
            steps {
                script {
                    def fullImageName = "ghcr.io/${env.GHCR_OWNER}/${env.IMAGE_NAME}:${env.BUILD_NUMBER}"
                    withCredentials([sshUserPrivateKey(credentialsId: 'ec2-ssh-key', keyFileVariable: 'EC2_PRIVATE_KEY')]) {
                        echo "Deploying to EC2 host: ${env.EC2_HOST}"
                        sh """
                            ssh -o StrictHostKeyChecking=no -i \${EC2_PRIVATE_KEY} ${env.EC2_USER}@${env.EC2_HOST} \
                            "bash /home/ubuntu/spring-app/deploy.sh ${fullImageName}"
                        """
                    }
                }
            }
        }
    }

    post {
        always {
            cleanWs()
        }
    }
}
