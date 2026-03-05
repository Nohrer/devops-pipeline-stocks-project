#!/usr/bin/env groovy

def build
def sonar
def version
def notify
def nexus

// Services should be named the same as folder name of each service
def services = ['discovery-service', 'gateway-service', 'stock-service']
pipeline{
    agent { label 'localhost' }

    stages{

        stage("init"){
            steps{
                script{
                    build = load "jenkins-scripts/build.groovy"
                    sonar = load "jenkins-scripts/sonar.groovy"
                    version = load "jenkins-scripts/version.groovy"
                    notify = load "jenkins-scripts/notificationService.groovy"
                    nexus = load "jenkins-scripts/nexus.groovy"

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
                    try{
                        build.backend(services)
                    }catch(e){
                        notify.notifyDev("backend build")
                        throw e
                    }
                    try{
                        dir('frontend'){
                        build.frontend()
                    }
                    }catch(e){
                        notify.notifyDev("frontend build")
                        throw e
                    }
                    }
                    
                }                      
            }
            
        stage('verify jars'){
            steps{
                script{
                    try{
                        build.verifyJars(services)
                    }catch(e){
                        notify.notifyDev("verify jars")
                        throw e
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

        // We should configure weebhooks in sonarqube to trigger this stage to send status
        // stage("Quality Gate"){
        //     steps{
        //         script{
        //             timeout(time: 5, unit: 'MINUTES'){
        //                 def qg = waitForQualityGate()
        //                 if(qg.status != 'OK'){
        //                     notify.notifyDev("Quality Gate")
        //                     error "Quality gate failed: ${qg.status}"
        //                 }
        //                 }
        //             }
        //         }
        //     }
        
        stage('Upload to nexus'){
            steps{
                script{
                    nexus.uploadBackend(services)
                    nexus.uploadFrontEnd()
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
                    sh "ansible-playbook -i ansible/inventory.ini ansible/deploy-java-application.yml --vault-password-file=$VAULT_PASS_FILE -e APP_VERSION=${APP_VERSION}"
                }     
            }
        }

    post {
        failure {
            echo 'Pipeline failed. Please check SonarQube report.'
            script{
                if(currentBuild.description?.contains('notified-dev') == false){
                    notify.notifyAdmin('Pipeline', currentBuild.description ?: 'Unexpected error')  
                }
            }
        }
    }
    }
}
