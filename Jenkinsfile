
// 젠킨스 파일의 선언형 파이프라인 정의부 시작 (그루비 언어)
pipeline {
    agent any // 젠킨스 서버가 여러개 일때, 어느 젠킨스 서버에서나 실행이 가능
    stages {
    // 각 작업 단위별로 stage로 나누어서 작성이 가능함. ()에 제목을 붙일 수 있음.
        stage('Pull Codes from Github') {
            steps {
                checkout scm // 젠킨스와 연결된 소스 컨트롤 매니저, (git 등)에서 코드를 가져오는 명령어
            }
        }
        stage('Build Codes by Gradle') {
            steps {
                script {
                    sh """
                    echo "Build Stage start!"
                    """
                }
            }
        }
    }
}