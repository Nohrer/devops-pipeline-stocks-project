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
                    // if(stockServiceChanged || gatewayServiceChanged || discoveryServiceChanged ){
                        sh 'mvn versions:set -DnewVersion=${APP_VERSION} -DprocessAllModules'
                        sh 'mvn clean package -DskipTests'
                        sh '''
                        [ -f stock-service/target/stock-service-${APP_VERSION}.jar ] || mv stock-service/target/stock-service-*.jar stock-service/target/stock-service-${APP_VERSION}.jar
                        [ -f gateway-service/target/gateway-service-${APP_VERSION}.jar ] || mv gateway-service/target/gateway-service-*.jar gateway-service/target/gateway-service-${APP_VERSION}.jar
                        [ -f discovery-service/target/discovery-service-${APP_VERSION}.jar ] || mv discovery-service/target/discovery-service-*.jar discovery-service/target/discovery-service-${APP_VERSION}.jar
                        '''
                    // }

                    // if(frontendChanged){
                        dir('frontend'){
                            sh 'echo "Building frontend"'
                            sh 'npm install'
                            sh 'npm run build'
                            sh 'tar -czf frontend-${env.APP_VERSION}.tar.gz build/'
                            sh ' mv frontend-${env.APP_VERSION}.tar.gz ../frontend-${env.APP_VERSION}.tar.gz'
                        }
                    // }
                }                      
            }
        }
        stage('deploy to nexus'){
            steps{
                script{
                    sh '''
              echo "Current directory: $(pwd)"
              echo "Listing stock-service target:"
              ls -la stock-service/target/ | grep -i jar || echo "No JAR files found"
              echo "Listing gateway-service target:"
              ls -la gateway-service/target/ | grep -i jar || echo "No JAR files found"
              echo "Listing discovery-service target:"
              ls -la discovery-service/target/ | grep -i jar || echo "No JAR files found"
            '''
                    withCredentials([usernamePassword(credentialsId: 'nexus-credentials', usernameVariable: 'NEXUS_USER', passwordVariable: 'NEXUS_PASSWORD')]){
                        sh '''
                          curl -v -u $NEXUS_USER:$NEXUS_PASSWORD --upload-file stock-service/target/stock-service-${APP_VERSION}.jar \
                          http://localhost:5050/repository/releases/org/sid/stock-service/${APP_VERSION}/stock-service-${APP_VERSION}.jar
                        '''
                    }
                }
            }
        }
        // stage('deploy'){
        //     steps{
        //         withCredentials([file(credentialsId: 'ansible_vault_password', variable: 'VAULT_PASS_FILE')]){
        //             sh 'ansible-playbook -i hosts deploy-apache.yml --vault-password-file=$VAULT_PASS_FILE'
        //         }     
        //     }
        // }

    }
}