pipeline{
    agent any
    stages{
        stage('checkout'){
            steps{
                checkout scm
            }
        }
        stage('build'){
            steps{
                sh 'echo "Building  backend services"'
                sh 'mvn clean package -DskipTests'
                sh 'echo "Building frontend"'
                  dir('frontend'){
                    sh 'npm install'
                    sh 'npm run build'
                    }
            }
        }
        stage('deploy'){
            steps{
                withCredentials([file(credentialsId: 'ansible_vault_password', variable: 'VAULT_PASS_FILE')]){
                    sh 'ansible-playbook -i hosts deploy-apache.yml --vault-password-file=$VAULT_PASS_FILE'
                }     
            }
        }

    }
}