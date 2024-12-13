# Demo Project: Configure Webhook to Trigger CI Pipeline Automatically

## Overview
This project demonstrates how to configure a webhook to automatically trigger a Jenkins CI pipeline whenever a change is pushed to a GitLab or GitHub repository. By setting up this integration, you can ensure seamless and automated builds for your application.

## Technologies Used
- **Jenkins**: For managing and triggering CI pipelines.
- **GitLab/GitHub**: As the source code repository.
- **Git**: Version control system.
- **Docker**: For containerization.
- **Java & Maven**: For the demo application.

---

## Project Description
This guide walks through:
1. Installing the GitLab and GitHub plugins in Jenkins.
2. Configuring GitLab and GitHub access tokens and webhook settings.
3. Setting up Jenkins to trigger the CI pipeline on changes.

---

## Prerequisites
- A Jenkins server installed and running.
- A GitLab or GitHub repository for your application.
- A Java Maven application to build.
- Docker installed on the Jenkins server.

---

## Steps

### 1. Install GitLab and GitHub Plugins in Jenkins
1. Log in to the Jenkins dashboard.
2. Navigate to **Manage Jenkins** > **Plugin Manager** > **Available Plugins**.
3. Search for **GitLab Plugin** and **GitHub Integration Plugin**, then install them.
4. Restart Jenkins if prompted.

### 2. Configure Access Tokens
#### GitLab
1. In GitLab:
   - Go to **User Settings** > **Access Tokens**.
   - Create a new token with the following scopes:
     - `api`
     - `read_repository`
   - Save the token securely.

2. In Jenkins:
   - Go to **Manage Jenkins** > **Credentials** > **Global Credentials** > **Add Credentials**.
   - Select **Secret Text**.
   - Paste the GitLab access token and give it an ID (e.g., `gitlab-token`).

#### GitHub
1. In GitHub:
   - Go to **Settings** > **Developer Settings** > **Personal Access Tokens**.
   - Generate a new token with the following scopes:
     - `repo`
     - `admin:repo_hook`
   - Save the token securely.

2. In Jenkins:
   - Go to **Manage Jenkins** > **Credentials** > **Global Credentials** > **Add Credentials**.
   - Select **Secret Text**.
   - Paste the GitHub personal access token and give it an ID (e.g., `github-token`).

### 3. Configure Project to Use Webhook
#### GitLab
1. Open your GitLab repository.
2. Go to **Settings** > **Webhooks**.
3. Add a new webhook:
   - **URL**: `http://<jenkins-server-url>:8080/project/<job-name>`
   - **Trigger**: Select **Push Events**.
   - Add the webhook and test it.

#### GitHub
1. Open your GitHub repository.
2. Go to **Settings** > **Webhooks**.
3. Add a new webhook:
   - **Payload URL**: `http://<jenkins-server-url>:8080/github-webhook/`
   - **Content Type**: `application/json`
   - Select **Just the push event**.
   - Add the webhook and test it.

### 4. Create a Jenkins Pipeline Job
1. In Jenkins, create a new **Pipeline** job.
2. Configure the job:
   - **Source Code Management**: Select Git.
   - Provide the GitLab or GitHub repository URL and credentials.
   - Use a `Jenkinsfile` in the repository for the pipeline definition.
3. Example `Jenkinsfile`:
   ```groovy
   pipeline {
       agent any
       stages {
           stage('Checkout') {
               steps {
                   git credentialsId: 'github-token', url: 'https://github.com/<your-repo>.git'
               }
           }
           stage('Build') {
               steps {
                   sh 'mvn clean package'
               }
           }
           stage('Docker Build') {
               steps {
                   sh 'docker build -t your-dockerhub-user/demo-app:$BUILD_NUMBER .'
               }
           }
           stage('Docker Push') {
               steps {
                   withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials-id', usernameVariable: 'USER', passwordVariable: 'PASS')]) {
                       sh 'echo $PASS | docker login -u $USER --password-stdin'
                       sh 'docker push your-dockerhub-user/demo-app:$BUILD_NUMBER'
                   }
               }
           }
       }
   }
   ```
4. Save the job configuration.

### 5. Verify the Webhook Integration
1. Push a change to the GitLab or GitHub repository.
2. Check the Jenkins dashboard to verify that the pipeline is triggered automatically.

---

## Best Practices
- Use meaningful names for your Jenkins jobs to identify them easily.
- Secure your credentials using Jenkins' credential management.
- Regularly update Jenkins plugins for security and performance.
- Enable logging in Jenkins to troubleshoot webhook issues.

---

## Conclusion
This project demonstrates how to set up a webhook to trigger a CI pipeline in Jenkins whenever changes are pushed to a GitLab or GitHub repository. This integration helps streamline your CI/CD workflows and ensures automated builds.
