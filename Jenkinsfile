pipeline {
    agent any

    // 12-6에서 등록한 GitHub PAT를 환경변수로 주입.
    // 로그에는 자동으로 ****로 마스킹됨.
    environment {
        GITHUB_TOKEN = credentials('github-pat')
    }

    stages {
        // ① GitHub API 접근이 잘 되는지 토큰 검증 (선택, 디버깅용)
        stage('Verify GitHub Token') {
            steps {
                sh 'curl -s -H "Authorization: token $GITHUB_TOKEN" https://api.github.com/user | head -5'
            }
        }

        // ② 저장소에서 최신 코드 받아오기
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        // ③ 빌드 + 컨테이너 재시작
        stage('Build & Deploy') {
            steps {
                sh 'docker compose down || true'
                sh 'docker compose up -d --build'
            }
        }

        // ④ 헬스 체크
        stage('Health Check') {
            steps {
                sh 'sleep 20'
                sh 'docker compose ps'
            }
        }
    }

    post {
        success {
            echo '✅ 배포 성공!'
        }
        failure {
            echo '❌ 배포 실패. 로그 확인 필요.'
        }
    }
}
