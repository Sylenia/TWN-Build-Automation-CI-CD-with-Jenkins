# Demo Project: Dynamically Increment Application Version in Jenkins Pipeline

## Overview
This project demonstrates how to dynamically increment the application version in a Jenkins pipeline. Version control and incrementation are critical in modern CI/CD workflows for ensuring traceability, consistency, and deployment integrity. By automating this process, you can standardize versioning while maintaining efficiency.

## Technologies Used
- **Jenkins**: To manage and execute the pipeline.
- **Docker**: For containerization.
- **GitLab/GitHub**: As the source code repository.
- **Git**: For version control.
- **Java & Maven**: For building the demo application.

---

## Project Description
This guide includes the following steps:
1. **Increment Patch Version**: Automatically increment the patch version in the `pom.xml` file.
2. **Build Application and Clean Old Artifacts**: Compile the Java application and remove outdated build artifacts.
3. **Build Docker Image with Dynamic Tag**: Build a Docker image using the dynamically updated version as the tag.
4. **Push Docker Image to Private Repository**: Push the tagged image to DockerHub.
5. **Commit Version Update Back to Git Repository**: Commit the updated version back to the repository to ensure consistency.
6. **Prevent Trigger Loop**: Configure the pipeline to avoid triggering itself on CI build commits.

---

## Steps

### 1. Increment Patch Version
1. Add a step in the pipeline to parse and increment the patch version:
   ```groovy
   sh 'mvn build-helper:parse-version versions:set -DnewVersion=${parsedVersion.majorVersion}.${parsedVersion.minorVersion}.${parsedVersion.nextIncrementalVersion} versions:commit'
   ```
2. Use Groovy or a regex to extract the updated version from the `pom.xml`:
   ```groovy
   def matcher = readFile('pom.xml') =~ '<version>(.+)</version>'
   def version = matcher[0][1]
   env.APP_VERSION = version
   ```

### 2. Build Application and Clean Old Artifacts
1. Use Maven to clean old artifacts and build the application:
   ```groovy
   sh 'mvn clean package'
   ```
2. Ensure the `target/` directory is cleared to prevent stale builds.

### 3. Build Docker Image with Dynamic Tag
1. Use the dynamic version for the Docker image tag:
   ```groovy
   sh "docker build -t your-dockerhub-user/demo-app:${APP_VERSION}-${BUILD_NUMBER} ."
   ```

### 4. Push Docker Image to Private Repository
1. Authenticate with DockerHub:
   ```groovy
   withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials-id', usernameVariable: 'USER', passwordVariable: 'PASS')]) {
       sh 'echo $PASS | docker login -u $USER --password-stdin'
   }
   ```
2. Push the Docker image:
   ```groovy
   sh "docker push your-dockerhub-user/demo-app:${APP_VERSION}-${BUILD_NUMBER}"
   ```

### 5. Commit Version Update Back to Git Repository
1. Add and commit the updated `pom.xml` to Git:
   ```groovy
   sh 'git add pom.xml'
   sh 'git commit -m "Bump version to ${APP_VERSION}-${BUILD_NUMBER}"'
   ```
2. Push the commit:
   ```groovy
   sh 'git push origin main'
   ```

### 6. Prevent Trigger Loop
1. Configure the pipeline to skip builds triggered by CI commits:
   - Add a conditional check in the pipeline to verify the commit message:
     ```groovy
     if (!currentBuild.rawBuild.getCause(hudson.triggers.SCMTrigger.SCMTriggerCause)) {
         echo 'Triggered by SCM change'
     } else {
         echo 'Skipping CI-triggered build'
         return
     }
     ```
2. Alternatively, use Git tags or branches to segregate CI commits.

---

## Best Practices
1. **Version Control**:
   - Maintain semantic versioning (`major.minor.patch`) for consistency.
   - Automate version updates to reduce manual errors.
2. **Pipeline Efficiency**:
   - Clean old artifacts to optimize build time and resource usage.
   - Use dynamic tags for Docker images to avoid overwriting existing builds.
3. **Prevent Trigger Loops**:
   - Use commit message checks or dedicated branches to isolate CI commits.
   - Ensure the webhook or SCM trigger excludes specific commit patterns.

---

## Conclusion
This project showcases the importance of dynamic version control in modern CI/CD workflows. By automating patch version incrementation, artifact cleanup, Docker image tagging, and repository updates, the pipeline ensures consistency and efficiency. The preventive measures against trigger loops further enhance the robustness of the CI/CD process.
