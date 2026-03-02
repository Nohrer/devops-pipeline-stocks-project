#!/usr/bin/env groovy

def build
def sonar
def version
def services = ['discovery-service', 'gateway-service', 'stock-service']
pipeline{
    agent {label'localhost'}

    stages{

        stage("init"){
            steps{
                script{
                    build = load "jenkins-scripts/build.groovy"
                    sonar = load "jenkins-scripts/sonar.groovy"
                    version = load "jenkins-scripts/version.groovy"


                    version.setProjectName("STOCKS")
                }
            }
        }
        stage('checkout'){
            steps{
                checkout scm
                script{
                    env.APP_VERSION = version.getVersion()
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
        stage("Sonar Scan"){
            steps{
                script {
                    echo "Executing pipeline for branch $BRANCH_NAME"
                    sonar.scan(services)
                    echo "Completed SonarQube scan for ${services}."
                    }
                }
            }
        stage("Quality Gate"){
            steps{
                script{
                    timeout(time: 5, unit: 'MINUTES'){
                        services.each{ service ->
                            def qg = waitForQualityGate()
                            if(qg.status != 'OK'){
                                error "Pipeline aborted due to quality gate failure for ${service}: ${qg.status}"
                            }
                        }
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
                    sh 'ansible-playbook -i ansible/inventory.ini ansible/deploy-java-application.yml --vault-password-file=$VAULT_PASS_FILE'
                }     
            }
        }

    }
    post {
        failure {
            echo 'Pipeline failed. Please check SonarQube report.'
        }
    }
}
