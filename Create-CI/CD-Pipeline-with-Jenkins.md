# Demo Project: Create a CI Pipeline with Jenkinsfile

## Overview
This project demonstrates how to create a Continuous Integration (CI) pipeline for a Java Maven application using Jenkins. The pipeline automates the process of building and pushing the application to a repository. It includes the creation of Freestyle, Pipeline, and Multibranch Pipeline jobs, each tailored for different use cases.

## Technologies Used
- **Jenkins**: To manage CI jobs.
- **Docker**: To containerize the build environment.
- **Linux**: For server operation.
- **Git**: To connect with the application's repository.
- **Java & Maven**: For application development and building.

---

## Project Description
This guide includes:
1. Installing build tools (Maven, Node) in Jenkins.
2. Making Docker available on the Jenkins server.
3. Creating Jenkins credentials for a Git repository.
4. Setting up different Jenkins job types:
   - **Freestyle Jobs**: Simpler configurations for specific tasks.
   - **Pipeline Jobs**: Complex workflows managed via `Jenkinsfile`.
   - **Multibranch Pipeline Jobs**: Automatic branch-based pipelines.
5. Automating the following tasks in the pipeline:
   - Connecting to the Git repository.
   - Building a JAR file.
   - Building a Docker image.
   - Pushing the Docker image to a private DockerHub repository.

---

## Prerequisites
- A Jenkins server set up and running.
- Docker installed and configured.
- Access to a Git repository.
- Jenkins credentials for GitHub and DockerHub.

---

## Steps

### 1. Install Build Tools in Jenkins
Install Maven and Node.js tools in the Jenkins container:
```bash
# Access the Jenkins container
sudo docker exec -it jenkins bash

# Install Maven
apt update && apt install -y maven

# Install Node.js (if required for frontend builds)
apt install -y nodejs npm
```
Verify the installations:
```bash
mvn -v
node -v
```

---

### 2. Make Docker Available in Jenkins
To allow Jenkins to build and push Docker images:
#### Option 1: On Jenkins Server
1. Install Docker on the Jenkins server (if not already installed):
   ```bash
   sudo apt install -y docker.io
   ```
2. Add the Jenkins user to the Docker group to grant permissions:
   ```bash
   sudo usermod -aG docker jenkins
   ```
3. Restart the Jenkins server:
   ```bash
   sudo systemctl restart jenkins
   ```
4. Test Docker in Jenkins:
   ```bash
   docker --version
   ```

#### Option 2: Within the Jenkins Docker Container
If Jenkins is running as a Docker container:
1. Run the Jenkins container with Docker socket mounted:
   ```bash
   docker run -d \
     --name jenkins \
     -p 8080:8080 -p 50000:50000 \
     -v /var/jenkins_home:/var/jenkins_home \
     -v /var/run/docker.sock:/var/run/docker.sock \
     jenkins/jenkins:lts
   ```
2. Install Docker CLI inside the Jenkins container:
   ```bash
   sudo docker exec -it jenkins bash
   apt update && apt install -y docker.io
   ```
3. Verify Docker access from within Jenkins:
   ```bash
   docker --version
   ```

---

### 3. Create Jenkins Credentials
Add credentials in Jenkins for GitHub and DockerHub:
- **GitHub Credentials**:
  1. Go to Jenkins Dashboard > Manage Jenkins > Credentials.
  2. Add a new credential (username/password or personal access token).
- **DockerHub Credentials**:
  1. Add a new credential for DockerHub with username/password.

---

### 4. Create Jenkins Jobs

#### **Freestyle Job**
1. **Purpose**: Quick tasks like building the project or running tests.
2. **Steps**:
   - Create a new Freestyle Job in Jenkins.
   - Under **Source Code Management**, select Git and provide the repository URL and credentials.
   - Add a build step:
     ```bash
     mvn clean package
     ```
   - Save and build.

#### **Pipeline Job**
1. **Purpose**: Custom workflows defined in a `Jenkinsfile`.
2. **Steps**:
   - Create a new Pipeline Job.
   - Define the pipeline script (or point to a `Jenkinsfile` in the Git repo):
     ```groovy
     pipeline {
         agent any
         stages {
             stage('Checkout') {
                 steps {
                     git credentialsId: 'github-credentials-id', url: 'https://github.com/<your-repo>.git'
                 }
             }
             stage('Build JAR') {
                 steps {
                     sh 'mvn clean package'
                 }
             }
             stage('Build Docker Image') {
                 steps {
                     sh "docker build -t your-dockerhub-user/demo-app:$BUILD_NUMBER ."
                 }
             }
             stage('Push to DockerHub') {
                 steps {
                     withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials-id', usernameVariable: 'USER', passwordVariable: 'PASS')]) {
                         sh 'echo $PASS | docker login -u $USER --password-stdin'
                         sh "docker push your-dockerhub-user/demo-app:$BUILD_NUMBER"
                     }
                 }
             }
         }
     }
     ```
   - Save and build.

#### **Multibranch Pipeline Job**
1. **Purpose**: Automates pipelines for multiple branches.
2. **Steps**:
   - Create a new Multibranch Pipeline Job.
   - Under **Branch Sources**, select Git and provide the repository URL and credentials.
   - Add the `Jenkinsfile` to your repository's root.
   - Configure the branch discovery strategy.
   - Save and let Jenkins automatically detect branches and run jobs.

---

## Best Practices
- Use **Pipeline Jobs** for complex workflows.
- Use **Multibranch Pipeline Jobs** for repositories with multiple active branches.
- Regularly update tools and dependencies.
- Store sensitive information (e.g., credentials) securely in Jenkins.

---

## Conclusion
This project illustrates how to create and manage CI pipelines in Jenkins for Java Maven applications. By leveraging Freestyle, Pipeline, and Multibranch Pipeline jobs, you can efficiently automate tasks like building, testing, and deploying your applications.
