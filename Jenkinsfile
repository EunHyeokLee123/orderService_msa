// 자주 사용되는 필요한 변수는 전역으로 선언하는 것도 가능
// ECR credential helper 이름
def ecrLoginHelper = "docker-credential-ecr-login"


// 젠킨스 파일의 선언형 파이프라인 정의부 시작 (그루비 언어)
pipeline {
    agent any // 젠킨스 서버가 여러개 일때, 어느 젠킨스 서버에서나 실행이 가능
    environment{
        SERVICE_DIRS="config-service,discovery-service,gateway-service,ordering-service,user-service,product-service"
        ECR_URL="891612549514.dkr.ecr.ap-northeast-2.amazonaws.com/"
        REGION="ap-northeast-2"
    }
    stages {
    // 각 작업 단위별로 stage로 나누어서 작성이 가능함. ()에 제목을 붙일 수 있음.
        stage('Pull Codes from Github') {
            steps {
                checkout scm // 젠킨스와 연결된 소스 컨트롤 매니저, (git 등)에서 코드를 가져오는 명령어
            }
        }
        stage('Detect Changes') {
            steps {
                script {
                    // git rev-list --count HEAD
                    // rev-list : 특정 브랜치나 커밋을 기준으로 모든 이전 커밋 목록을 나열 -> HEAD 커밋을 기준으로
                    // --count : 목록을 출력하지 말고, 커밋 개수만 숫자로 반환
                    def commitCount = sh(script:"git rev-list --count HEAD", returnStdout: true)
                                      .trim()
                                      .toInteger()

                    def changedServices = []
                    def serviceDirs = env.SERVICE_DIRS.split(",")

                    // 최초 커밋이라면, 모든 서비스를 빌드
                    if (commitCount == 1) {
                        echo "Initial commit Detected. All services will be built."
                        changedServices = serviceDirs
                    }
                    else {
                        // 변경된 파일을 감지해보자! git 명령어를 통해서!
                        // returnStdout: true -> 결과를 출력하지 말고, 변수에 문자열로 넣어달라
                        def changedFiles = sh(script:"git diff --name-only HEAD~1 HEAD", returnStdout: true)
                                            .trim()
                                            .split("\n") // 변경된 파일을 줄 단위로 분리
                        // 변경된 파일 출력
                        // [user-service/src/main/resources/application.yml,
                        // user-service/src/main/java/com/playdata/userservice/controller/UserController.java,
                        // ordering-service/src/main/resources/application.yml]
                        echo "ChangedFiles: ${changedFiles}"


                        serviceDirs.each{ service ->
                            if (changedFiles.any {it.startsWith(service + "/")}) {
                                changedServices.add(service)
                            }
                        }
                    }
                    // 변경된 서비스 이름을 모아놓은 리스트를 다른 스테이지에서도
                    // 사용하기 위해 환경변수로 선언
                    // join() : 지정한 문자열을 구분자로 하여 리스트 요소를 하나의 문자열로 리턴, 중복 제거
                    env.CHANGED_SERVICES = changedServices.join(",")
                    if(env.CHANGED_SERVICES == "") {
                        echo "No changes Detected. Skipping Build Stage"
                        // 성공 상태로 파이프라인 종료
                        currentBuild.result = 'SUCCESS'
                    }
                }
            }
        }
        stage('Build changed Services') {
            // CHANGED_SERVICES가 빈 문자열이 아니라면 아래의 steps를 실행하겠다.
            // 이 스테이지는 빌드되어야 할 서비스가 존재할 때만 실행될 스테이지다!
            when {
                expression { env.CHANGED_SERVICES != "" }
            }
            steps {
                script {
                // 환경 변수 불러오기
                    def changedServices = env.CHANGED_SERVICES.split(",")
                    changedServices.each { service ->
                        sh """
                         echo "Building ${service}"
                         cd ${service}
                         chmod +x gradlew
                         ./gradlew clean build -x test
                         ls -al ./build/libs
                         cd ..
                        """
                    }
                }
            }
        }

         stage('Build Docker Image & Push to AWS ECR') {
            when {
                expression { env.CHANGED_SERVICES != "" }
            }
            steps {
                script {
                    // Jenkins에 저장된 credentials를 사용하여 AWS 자격증명을 설정.
                    withAws(region: "${REGION}", credentials:"aws-key"){
                        def changedServices = env.CHANGED_SERVICES.split(",")
                           changedServices.each {service ->
                           sh """
                           # ECR에 이미지를 push하기 위해 인증 정보를 대신 검증 해주는 도구 다운로드.
                           # /user/local/bin/ 경로에 다운로드한 해당 파일을 이동 및 실행할 수 있는 권한 부여

                           curl -O https://amazon-ecr-credential-helper-releases.s3.us-east-2.amazonaws.com/0.4.0/linux-amd64/${ecrLoginHelper}
                           chmod +x ${ecrLoginHelper}
                           mv ${ecrLoginHelper} /usr/local/bin/


                           # Docker에게 push 명령을 내리면 지정된 URL로 push할 수 있게 설정.
                           # 자동으로 로그인 도구를 쓰게 설정

                           echo '{"credHelpers": {"${ECR_URL}/${service}": "ecr-login"}}' > ~/.docker/config.json

                           echo "${ECR_URL}/${service}"

                           docker build -t ${service}:latest ${service}
                           docker tag ${service}:latest ${ECR_URL}/${service}:latest
                           docker push ${ECR_URL}/${service}:latest
                           """
                        }
                    }
                }
            }
         }
        }
    }
