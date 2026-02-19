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

    }
}