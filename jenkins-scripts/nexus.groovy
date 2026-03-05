// URL STRUCTURE VARS
import groovy.transform.Field
@Field String nexus_url = "http://localhost:8081/repository/"
@Field String release_folder = "stockApp-release/"
@Field String snapshots_folder ="stockApp-snapshots/"
@Field String frontend_service = "frontend"

// CRED VAR

@Field String nexus_credential = "nexus-stocks-cred"

//FUNCTIONS

def get_upload_folder(){
    (env.BRANCH_NAME == "main") ? release_folder : snapshots_folder
}
def uploadBackend(List<String> services){
    withCredentials([usernamePassword(credentialsId: nexus_credential, usernameVariable: 'NEXUS_USER', passwordVariable: 'NEXUS_PASSWORD')]){
        services.each{service -> 
            echo "Uploading ${service} with version: ${APP_VERSION}"
            try{
            sh """
                curl -v -u \$NEXUS_USER:\$NEXUS_PASSWORD --upload-file ${service}/target/${service}-${APP_VERSION}.jar \
                ${nexus_url}${get_upload_folder()}${service}/${service}-${APP_VERSION}.jar

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
    withCredentials([usernamePassword(credentialsId: nexus_credential, usernameVariable: 'NEXUS_USER', passwordVariable: 'NEXUS_PASSWORD')]){
    echo "Uploading ${frontend_service} with version: ${APP_VERSION}"
    try{
        sh """
            curl -v -u \$NEXUS_USER:\$NEXUS_PASSWORD --upload-file ${frontend_service}-${APP_VERSION}.tar.gz \
            ${nexus_url}${get_upload_folder()}${frontend_service}/${frontend_service}-${APP_VERSION}.tar.gz
        """
    }
    catch(e){
        echo "Failed to upload ${frontend_service} to Nexus. Error: ${e.getMessage()}"
        throw e
    }
}
}

return this