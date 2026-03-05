// URL STRUCTURE VARS
def nexus_url = "http://localhost:8081/repository/"
def release_folder = "stockApp-release/"
def snapshots_folder ="stockApp-snapshots/"
def frontend_service = "frontend"

// CRED VAR

def nexus_credential = "nexus-stocks-cred"

//FUNCTIONS

def upload_folder = (env.BRANCH_NAME == "main") ? release_folder : snapshots_folder

def uploadBackend(List<String> services){
    withCredentials([usernamePassword(credentialsId: this.nexus_credential, usernameVariable: 'NEXUS_USER', passwordVariable: 'NEXUS_PASSWORD')]){
        services.each{service -> 
            echo "Uploading ${service} with version: ${APP_VERSION}"
            try{
            sh """
                curl -v -u \$NEXUS_USER:\$NEXUS_PASSWORD --upload-file ${service}/target/${service}-${APP_VERSION}.jar \
                ${nexus_url}${upload_folder}${service}/${service}-${APP_VERSION}.jar

            """
            }
            catch(e){
                echo "Failed to upload ${service} to Nexus. Error: ${e.getMessage()}"
                throw e
            }
    }
    }
}

def uploadFrontEnd(){
    withCredentials([usernamePassword(credentialsId: this.nexus_credential, usernameVariable: 'NEXUS_USER', passwordVariable: 'NEXUS_PASSWORD')]){
    echo "Uploading ${frontend_service} with version: ${APP_VERSION}"
    try{
        sh """
            curl -v -u \$NEXUS_USER:\$NEXUS_PASSWORD --upload-file ${frontend_service}-${APP_VERSION}.tar.gz \
            ${nexus_url}${upload_folder}${frontend_service}/${frontend_service}-${APP_VERSION}.tar.gz
        """
    }
    }catch(e){
        echo "Failed to upload ${frontend_service} to Nexus. Error: ${e.getMessage()}"
        throw e
    }
}

return this