#!/usr/bin/env groovy

def backend(List<String> services){
    echo "Building  backend ${services}"
    sh "mvn versions:set -DnewVersion=\${APP_VERSION} -DprocessAllModules"
    sh "mvn clean package -DskipTests"
    services.each{service ->
        echo "Processing artifact for ${service}"
        sh """
        [ -f ${service}/target/${service}-\${APP_VERSION}.jar ] || mv ${service}/target/${service}-*.jar ${service}/target/${service}-\${APP_VERSION}.jar
        """
    }    
}

def verifyJars(List<String> services){
    services.each{service ->
        def jarFile = "${service}/target/${service}-${APP_VERSION}.jar"
        if(fileExists(jarFile)){

            echo "verifying ${jarFile}"
            
            sh "test -s ${jarFile} && echo 'File exists and is not empty'"
            sh "jar tf ${jarFile} > /dev/null && echo 'Jar structure is valid'"
        }
    }
}

def frontend(){
    sh 'echo "Building frontend"'
    sh 'npm ci'
    sh 'npm run build'
    sh "tar -czf frontend-${APP_VERSION}.tar.gz build/"
    sh "mv frontend-${APP_VERSION}.tar.gz ../frontend-${APP_VERSION}.tar.gz"
}
return this