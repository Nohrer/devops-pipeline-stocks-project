#!/usr/bin/env groovy

def projectName

def setProjectName(String pn = ""){
    projectName = pn
}
def getVersion(){
    def buildNumber = env.BUILD_NUMBER ?: "1"

    def currentTag = sh(
        script: "git describe --exact-match --tags HEAD 2>/dev/null || echo ''",
        returnStdout: true
    ).trim()

    if (currentTag){
        echo "Tag Found ${currentTag}"
        return "${currentTag}.${buildNumber}"
    }    

    echo "No tag Found, Generating new one"

    def dateVersion = sh(
                        script: "date +%y%j",
                        returnStdout: true
                    ).trim()

    def latestTag = sh(
        script: "git describe --tags --abbrev=0 2>/dev/null || echo '${projectName}1.0.${dateVersion}'",
        returnStdout: true
    ).trim()

    echo "Latest tag: ${latestTag}"

    def parts = latestTag.tokenize('-.')

    def newTag
    if(parts>size() >= 3){
        def major = parts[1]
        def minor = parts[2]
        def patch = dateVersion

        newTag = "${projectName}.${major}.${minor}.${patch}"
    }
    

    echo "Generated new tag: ${newTag}"

    pushTagtoGit(newTag)

    return "${newTag}.${buildNumber}"

}

def pushTagtoGit(String tag){
    sh """
        git tag ${tag}
        git push origin ${tag}
    """
}

return this