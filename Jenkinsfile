// 자주 사용되는 필요한 변수는 전역으로 선언하는 것도 가능
// ECR credential helper 이름
def ecrLoginHelper = "docker-credential-ecr-login"
// 프라이빗 IPv4 주소
def deployHost = "172.31.7.215"


// 젠킨스 파일의 선언형 파이프라인 정의부 시작 (그루비 언어)
pipeline {
    agent any // 젠킨스 서버가 여러개 일때, 어느 젠킨스 서버에서나 실행이 가능
    environment{
        SERVICE_DIRS="config-service,discovery-service,gateway-service,ordering-service,user-service,product-service"
        ECR_URL="891612549514.dkr.ecr.ap-northeast-2.amazonaws.com"
        REGION="ap-northeast-2"
    }
    stages {
    // 각 작업 단위별로 stage로 나누어서 작성이 가능함. ()에 제목을 붙일 수 있음.
        stage('Pull Codes from Github') {
            steps {
                checkout scm // 젠킨스와 연결된 소스 컨트롤 매니저, (git 등)에서 코드를 가져오는 명령어
            }
        }

        ////

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
                    env.CHANGED_SERVICES = env.SERVICE_DIRS.join(",")
                    if(env.CHANGED_SERVICES == "") {
                        echo "No changes Detected. Skipping Build Stage"
                        // 성공 상태로 파이프라인 종료
                        currentBuild.result = 'SUCCESS'
                    }
                }
            }
        }

        /////

        stage('Build changed Services') {
            // CHANGED_SERVICES가 빈 문자열이 아니라면 아래의 steps를 실행하겠다.
            // 이 스테이지는 빌드되어야 할 서비스가 존재할 때만 실행될 스테이지다!

            steps {
                script {
                // 환경 변수 불러오기
                    def changedServices = env.SERVICE_DIRS.split(",")
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

            ////

         stage('Build Docker Image & Push to AWS ECR') {
             steps {
                 script {
                     withAWS(region: "${REGION}", credentials: "aws-key") {
                         def changedServices = env.SERVICE_DIRS.split(",")
                         changedServices.each { service ->
                             // secret 파일이 필요한 경우만 credentials 호출
                             if (service == "config-service") {
                                 withCredentials([file(credentialsId: 'application-dev.yml', variable: 'DEV_YML')]) {
                                     sh """
                                     # secret 파일 복사
                                     cp \$DEV_YML ${service}/src/main/resources/application-dev.yml

                                     # docker credential helper 설정
                                     curl -O https://amazon-ecr-credential-helper-releases.s3.us-east-2.amazonaws.com/0.4.0/linux-amd64/${ecrLoginHelper}
                                     chmod +x ${ecrLoginHelper}
                                     mv ${ecrLoginHelper} /usr/local/bin/

                                     mkdir -p ~/.docker
                                     echo '{"credHelpers": {"${ECR_URL}": "ecr-login"}}' > ~/.docker/config.json

                                     docker build -t ${service}:latest ${service}
                                     docker tag ${service}:latest ${ECR_URL}/${service}:latest
                                     docker push ${ECR_URL}/${service}:latest

                                     # 보안상, 빌드 이후에는 dev.yml 삭제
                                     rm -f ${service}/src/main/resources/dev.yml
                                     """
                                 }
                             } else {
                                 sh """
                                 curl -O https://amazon-ecr-credential-helper-releases.s3.us-east-2.amazonaws.com/0.4.0/linux-amd64/${ecrLoginHelper}
                                 chmod +x ${ecrLoginHelper}
                                 mv ${ecrLoginHelper} /usr/local/bin/

                                 mkdir -p ~/.docker
                                 echo '{"credHelpers": {"${ECR_URL}": "ecr-login"}}' > ~/.docker/config.json

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

         /////

         stage('Deploy Changed Services to AWS EC2') {

                steps {
                    sshagent(credentials: ["deploy-key"]) {
                        sh """
                        # Jenkins에서 배포 서버로 docker-compose.yml을 복사 후 전송
                        scp -o StrictHostKeyChecking=no docker-compose.yml ubuntu@${deployHost}:/home/ubuntu/docker-compose.yml

                        # Jenkins에서 배포 서버로 직접 접속을 시도
                        # docker-compose 실행하기 위해서
                        ssh -o StrictHostKeyChecking=no ubuntu@${deployHost} '
                        cd /home/ubuntu && \

                        # 배포 서버에서 Jenkins로 로그인 (로그인 만료를 방지하기 위해서)
                        aws ecr get-login-password --region ${REGION} | docker login --username AWS --password-stdin ${ECR_URL} && \

                        # docker-compose를 통해서 변경된 서비스의 이미지만 pull -> 일괄 실행
                        docker-compose pull ${env.CHANGED_SERVICES} && \
                        docker compose up -d ${env.CHANGED_SERVICES} '
                        """
                    }
                }
            }

            /////

        }
    }