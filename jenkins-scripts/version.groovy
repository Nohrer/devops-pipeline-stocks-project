#!/usr/bin/env groovy

def getVersion(){
    def currentTag = sh(
        script: "git describe --exact-match --tags HEAD 2>/dev/null || echo ''",
        returnStdout: true
    ).trim()
    if (currentTag){
        echo "Tag Found ${currentTag}"
        return currentTag
    }
    
    echo "No tag Found, Generating new one"

    def latestTag = sh(
        script: "git"
    )
}