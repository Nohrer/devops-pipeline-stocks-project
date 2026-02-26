#!usr/bin/env groovy

def scan(List<String> services){
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
            // quality gate
        }
    }
}

return this