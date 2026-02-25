#!/user/bin/env groovy

def build
def sonar
def services = ['discovery-service', 'gateway-service', 'stock-service']
pipeline{
    agent localhost

    stages{
        stage("init"){
            steps{
                script{
                    build = load "jenkins-scripts/build.groovy"
                    sonar = load "jenkins-scripts/sonar.groovy"
                }
            }
        }
        stage('checkout'){
            steps{
                checkout scm
            }
        }
        // tags git if tag exist pull if not push
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
                    build.backend(services)

                    dir('frontend'){
                        build.frontend()
                    }
                }                      
            }
        }
        stage("Sonnar Scan"){
            steps{
                script {
                    echo "Executing pipeline for branch $BRANCH_NAME"
                    sonar.scan(services)
                    echo "Completed SonarQube scan for ${service}."
                    }
                }
            }
        }

        stage('deploy to nexus'){
            when{
                    expression{
                        BRANCH_NAME == "main"
                    }
                }
            steps{
                script{
                    withCredentials([usernamePassword(credentialsId: 'nexus-credentials', usernameVariable: 'NEXUS_USER', passwordVariable: 'NEXUS_PASSWORD')]){
                        sh """
                        echo "Executing pipeline for branch $BRANCH_NAME"
                        echo "Uploading stock-service with version: ${APP_VERSION}"
                        curl -v -u \$NEXUS_USER:\$NEXUS_PASSWORD --upload-file stock-service/target/stock-service-${APP_VERSION}.jar \
                        http://localhost:5050/repository/stockApp-releases/org/sid/stock-service/${APP_VERSION}/stock-service-${APP_VERSION}.jar
                        
                        echo "Uploading gateway-service"
                        curl -v -u \$NEXUS_USER:\$NEXUS_PASSWORD --upload-file gateway-service/target/gateway-service-${APP_VERSION}.jar \
                        http://localhost:5050/repository/stockApp-releases/org/sid/gateway-service/${APP_VERSION}/gateway-service-${APP_VERSION}.jar
                        
                        echo "Uploading discovery-service"
                        curl -v -u \$NEXUS_USER:\$NEXUS_PASSWORD --upload-file discovery-service/target/discovery-service-${APP_VERSION}.jar \
                        http://localhost:5050/repository/stockApp-releases/org/sid/discovery-service/${APP_VERSION}/discovery-service-${APP_VERSION}.jar
                        
                        echo "Uploading frontend"
                        curl -v -u \$NEXUS_USER:\$NEXUS_PASSWORD --upload-file frontend-${APP_VERSION}.tar.gz \
                        http://localhost:5050/repository/stockApp-releases/org/sid/frontend/${APP_VERSION}/frontend-${APP_VERSION}.tar.gz
                        
                        echo "Uploading Latest"
                        curl -v -u \$NEXUS_USER:\$NEXUS_PASSWORD --upload-file gateway-service/target/gateway-service-${APP_VERSION}.jar \
                        http://localhost:5050/repository/stockApp-releases/org/sid/gateway-service/latest/gateway-service-latest.jar

                        curl -v -u \$NEXUS_USER:\$NEXUS_PASSWORD --upload-file discovery-service/target/discovery-service-${APP_VERSION}.jar \
                        http://localhost:5050/repository/stockApp-releases/org/sid/discovery-service/latest/discovery-service-latest.jar

                        curl -v -u \$NEXUS_USER:\$NEXUS_PASSWORD --upload-file stock-service/target/stock-service-${APP_VERSION}.jar \
                        http://localhost:5050/repository/stockApp-releases/org/sid/stock-service/latest/stock-service-latest.jar

                        curl -v -u \$NEXUS_USER:\$NEXUS_PASSWORD --upload-file frontend-${APP_VERSION}.tar.gz \
                        http://localhost:5050/repository/stockApp-releases/org/sid/frontend/latest/frontend-latest.tar.gz
                        """
                    }
                }
            }
        }
    // choice environment, choice branch
        stage('deploy'){
             when{
                    expression{
                        BRANCH_NAME == "main"
                    }
                }
            steps{
                withCredentials([file(credentialsId: 'ANSIBLE_VAULT_PASS', variable: 'VAULT_PASS_FILE')]){
                    sh 'ansible-playbook -i inventory.ini deploy-apache.yml --vault-password-file=$VAULT_PASS_FILE'
                }     
            }
        }

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