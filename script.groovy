def buildJar() {
    echo 'building the application...'
    sh 'mvn build-helper:parse-version versions:set -DnewVersion=\${parsedVersion.majorVersion}.\${parsedVersion.minorVersion}.\${parsedVersion.nextIncrementalVersion} versions:commit'
    def matcher = readFile('pom.xml') =~ '<version>(.+)</version>'
    def version = matcher[0][1]
    env.IMAGE_NAME = "$version-$BUILD_NUMBER"
    sh 'mvn clean package'
}

def buildImage() {
    echo "building the docker image..."
    withCredentials([usernamePassword(credentialsId: 'docker-hub-repo', passwordVariable: 'PASS', usernameVariable: 'USER')]) {
        sh "docker build -t syleniainc/demo-app:$IMAGE_NAME ."
        sh 'echo $PASS | docker login -u $USER --password-stdin'
        sh "docker push syleniainc/demo-app:$IMAGE_NAME"
    }
}

def deployApp() {
    echo 'deploying the application...'
}

def pushPomToGit() {
    echo 'Pushing updated pom.xml to GitHub...'
    
    withCredentials([usernamePassword(credentialsId: 'github-credentials', passwordVariable: 'GIT_PASS', usernameVariable: 'GIT_USER')]) {

        // Configure Git user
        sh """
            git config user.email "jenkins@example.com"
            git config user.name "Jenkins CI"
        """
        
        // Add, commit, and push the updated pom.xml
        sh """
            git add pom.xml
            git commit -m "Bump version to $env.IMAGE_NAME"
            git push https://${GIT_USER}:${GIT_PASS}@github.com/<your-repo-owner>/<your-repo>.git HEAD:branchname
        """
    }
}

return this
