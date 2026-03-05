// Ce service notify l'utilisateurs dev ou admin en cas d'erreur via gmail

def ADMIN_EMAIL = "jenkins-service@sii-maroc.com"

def getDevEmail(){
    try{
        def gitEmail = sh(
            script: "git log -1 --format='%ae'",
            returnStdout: true
        ).trim()
        
        if(gitEmail){
            echo "Dev Email resolved From GIT"
            return gitEmail
        }
    } catch(e){
        echo "Failed to get Email from GIT"
    }
    return ADMIN_EMAIL
}

def messageToDev(String stageName){
    return """
    <html>
        <body>
            <h2>${stageName} Failure</h2>
            <p>A failure was detected in a stage you triggered. Please review and fix.</p>
            <table style="border-collapse:collapse; width:100%;">
                <tr><td><b>Failed Stage</b></td><td>${stageName}</td></tr>
                <tr><td><b>Branch</b></td><td>${env.GIT_BRANCH ?: 'N/A'}</td></tr>
                <tr><td><b>Commit</b></td><td>${env.GIT_COMMIT ?: 'N/A'}</td></tr>
                <tr><td><b>Logs</b></td><td><a href="${env.BUILD_URL}">${env.BUILD_URL}</a></td></tr>
            </table>
        </body>
    </html>
    """
}

def messageToAdmin(String stageName, String errorMessage){
    return """
    <html>
        <body>
            <h2>Pipeline Error</h2>
            <p>An Error was detected in the pipeline.</p>
            <table style="border-collapse:collapse; width:100%;">
                <tr><td><b>Failed Stage</b></td><td>${stageName}</td></tr>
                <tr><td><b>Error Message</b></td><td>${errorMessage}</td></tr>
                <tr><td><b></b>Branch</td><td>${env.GIT_BRANCH ?: 'N/A'}</td></tr>
                <tr><td><b></b>Commit</td><td>${env.GIT_COMMIT ?: 'N/A'}</td></tr>
                <tr><td><b></b>Logs</td><td><a href="${env.BUILD_URL}">${env.BUILD_URL}</a></td></tr>
            </table>
        </body>
    </html>
    """
}
// cette fonction envoi des emails il faut configurer le serveur via jenkins system apres install plugin
def sendNotification(String reciever,String subject,String body){
    emailext(
        to: reciever,
        subject: subject,
        body: body,
        mimeType: 'text/html'
    )
    echo "Notification sent to: ${reciever}"
}

def notifyDev(String stageName){
    def to = getDevEmail()
    def subject = "Pipeline Failed at ${stageName} - ${env.JOB_NAME} #${env.BUILD_NUMBER}"
    def body = messageToDev(stageName)
    sendNotification(to, subject, body)
    currentBuild.description = 'dev-notified'
}

def notifyAdmin(String stageName, String errorMessage){
    def subject = "Pipeline Error at ${stageName} - ${env.JOB_NAME} #${env.BUILD_NUMBER}"
    def body = messageToAdmin(stageName, errorMessage)
    sendNotification(ADMIN_EMAIL, subject, body)
}

return this