pipeline {
    agent any

    environment {
        GHCR_OWNER = 'kyj0503'
        EC2_HOST = 'ide-be.yeonjae.kr'
        EC2_USER = 'ubuntu'
        IMAGE_NAME = 'web-ide'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build and Push to GHCR') {
            steps {
                script {
                    def fullImageName = "ghcr.io/${env.GHCR_OWNER}/${env.IMAGE_NAME}:${env.BUILD_NUMBER}"
                    echo "Building Docker image: ${fullImageName}"
                    docker.build(fullImageName, '.')
                    docker.withRegistry("https://ghcr.io", 'github-token') {
                        echo "Pushing Docker image to GHCR..."
                        docker.image(fullImageName).push()
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
                        // EC2의 spring-app 디렉터리에 있는 배포 스크립트 실행
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