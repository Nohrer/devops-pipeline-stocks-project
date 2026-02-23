pipeline{
    agent any

    stages{
        stage('checkout'){
            steps{
                checkout scm
            }
        }

        stage('Generate Version'){
            steps{
                script{
                    def dateVersion = sh(
                        script: "date +%y%j",
                        returnStdout: true
                    ).trim()
                    env.APP_VERSION = "STOCKS-1.0.${dateVersion}.${env.BUILD_NUMBER}"
                }
            }
        }
        stage('build'){
            steps{
                script{
                    def stockServiceChanged = sh(script: 'git diff --name-only HEAD-1 | grep -q "stock-service/"',returnStatus: true) == 0
                    def gatewayServiceChanged = sh(script: 'git diff --name-only HEAD-1 | grep -q "gateway-service/"',returnStatus: true) == 0
                    def discoveryServiceChanged = sh(script: 'git diff --name-only HEAD-1 | grep -q "discovery-service/"',returnStatus: true) == 0
                    def frontendChanged = sh(script: 'git diff --name-only HEAD-1 | grep -q "frontend/"',returnStatus: true) == 0


                    sh 'echo "Building  backend services"'
                        sh 'mvn versions:set -DnewVersion=${APP_VERSION} -DprocessAllModules'
                        sh 'mvn clean package -DskipTests'
                        sh '''
                        [ -f stock-service/target/stock-service-${APP_VERSION}.jar ] || mv stock-service/target/stock-service-*.jar stock-service/target/stock-service-${APP_VERSION}.jar
                        [ -f gateway-service/target/gateway-service-${APP_VERSION}.jar ] || mv gateway-service/target/gateway-service-*.jar gateway-service/target/gateway-service-${APP_VERSION}.jar
                        [ -f discovery-service/target/discovery-service-${APP_VERSION}.jar ] || mv discovery-service/target/discovery-service-*.jar discovery-service/target/discovery-service-${APP_VERSION}.jar
                        '''

                    // if(frontendChanged){
                        dir('frontend'){
                            sh 'echo "Building frontend"'
                            sh 'npm install'
                            sh 'npm run build'
                            sh 'tar -czf frontend-${APP_VERSION}.tar.gz build/'
                            sh ' mv frontend-${APP_VERSION}.tar.gz ../frontend-${APP_VERSION}.tar.gz'
                        }
                    // }
                }                      
            }
        }
        stage("Sonnar Scan"){
            steps{
                script {
                    def services = ['discovery-service', 'gateway-service', 'stock-service']
                    
                    services.each { service ->
                        echo "Scanning ${service} with SonarQube..."
                        
                        dir(service) {
                            withSonarQubeEnv('sonar-server') {
                                sh """
                                mvn sonar:sonar \
                                -Dsonar.projectKey=${service} \
                                -Dsonar.projectName=${service} \
                                -Dsonar.projectVersion=${APP_VERSION}
                                """
                            }
                            
                            // Publish to Jenkins dashboard
                            dependencyCheckPublisher pattern: 'target/dependency-check-report.xml'
                        }
                        echo "Completed SonarQube scan for ${service}."
                    }
                }
            }
        }
        // stage('deploy to nexus'){
        //     steps{
        //         script{
        //             withCredentials([usernamePassword(credentialsId: 'nexus-credentials', usernameVariable: 'NEXUS_USER', passwordVariable: 'NEXUS_PASSWORD')]){
        //                 sh """
        //                 echo "Uploading stock-service with version: ${APP_VERSION}"
        //                 curl -v -u \$NEXUS_USER:\$NEXUS_PASSWORD --upload-file stock-service/target/stock-service-${APP_VERSION}.jar \
        //                 http://localhost:5050/repository/stockApp-releases/org/sid/stock-service/${APP_VERSION}/stock-service-${APP_VERSION}.jar
                        
        //                 echo "Uploading gateway-service"
        //                 curl -v -u \$NEXUS_USER:\$NEXUS_PASSWORD --upload-file gateway-service/target/gateway-service-${APP_VERSION}.jar \
        //                 http://localhost:5050/repository/stockApp-releases/org/sid/gateway-service/${APP_VERSION}/gateway-service-${APP_VERSION}.jar
                        
        //                 echo "Uploading discovery-service"
        //                 curl -v -u \$NEXUS_USER:\$NEXUS_PASSWORD --upload-file discovery-service/target/discovery-service-${APP_VERSION}.jar \
        //                 http://localhost:5050/repository/stockApp-releases/org/sid/discovery-service/${APP_VERSION}/discovery-service-${APP_VERSION}.jar
                        
        //                 echo "Uploading frontend"
        //                 curl -v -u \$NEXUS_USER:\$NEXUS_PASSWORD --upload-file frontend-${APP_VERSION}.tar.gz \
        //                 http://localhost:5050/repository/stockApp-releases/org/sid/frontend/${APP_VERSION}/frontend-${APP_VERSION}.tar.gz
        //                 """
        //             }
        //         }
        //     }
        // }
        // stage('deploy'){
        //     steps{
        //         withCredentials([file(credentialsId: 'ansible_vault_password', variable: 'VAULT_PASS_FILE')]){
        //             sh 'ansible-playbook -i hosts deploy-apache.yml --vault-password-file=$VAULT_PASS_FILE'
        //         }     
        //     }
        // }

    }
    post {
        always {
            cleanWs()
        }
        failure {
            echo 'Pipeline failed. Please check SonarQube report.'
        }
    }
}