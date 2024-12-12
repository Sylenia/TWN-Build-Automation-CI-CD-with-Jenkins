
# Build Automation & CI/CD with Jenkins

## Project Goal
The goal of this project is to demonstrate the process of automating software builds, continuous integration (CI), and continuous delivery/deployment (CD) using Jenkins. By integrating modern DevOps practices, we aim to showcase the power and flexibility of Jenkins in managing CI/CD pipelines efficiently, from building applications to deploying Docker containers.

## Tools and Technologies

### Core Tools:
- **Jenkins**: Open-source automation server to orchestrate CI/CD pipelines.
- **Docker**: Platform for containerizing applications and managing deployments.
- **Git**: Version control system for managing source code.
- **GitLab**: Hosting service for Git repositories, integrated with CI/CD pipelines.

### Programming and Build Tools:
- **Java**: Programming language for the demo application.
- **Maven**: Build automation tool for managing Java application dependencies and lifecycle.
- **Groovy**: Scripting language used in Jenkins Shared Libraries.
- **Linux (Ubuntu)**: Operating system for hosting Jenkins on a DigitalOcean droplet.

## Project Summary

### 1. **Install Jenkins on DigitalOcean**
- Create an Ubuntu server on DigitalOcean.
- Set up and run Jenkins as a Docker container.
- Initialize Jenkins for project usage.

### 2. **Create a CI Pipeline with Jenkinsfile**
- Develop CI pipelines for a Java Maven application:
  - Freestyle jobs.
  - Declarative and scripted Jenkins Pipelines.
  - Multibranch Pipelines for managing multiple branches.
- Steps include:
  - Connecting to the Git repository.
  - Building a JAR file.
  - Creating Docker images.
  - Pushing Docker images to a private DockerHub repository.
- Install build tools (Maven, Node.js) and configure Docker in Jenkins.
- Manage Jenkins credentials for secure access to repositories.

### 3. **Create a Jenkins Shared Library**
- Build a reusable Jenkins Shared Library (JSL) in Groovy.
- Create a separate Git repository for the shared library.
- Extract common build logic into reusable functions.
- Integrate the JSL into Jenkins pipelines for global and project-specific use.

### 4. **Configure Webhook for Automatic CI Pipeline Trigger**
- Install GitLab plugin in Jenkins.
- Configure GitLab access token and Jenkins-GitLab integration.
- Set up webhooks in GitLab to automatically trigger Jenkins pipelines on every code push.

### 5. **Dynamically Increment Application Version**
- Automate application versioning in the CI pipeline:
  - Increment patch version.
  - Build the Java application and clean old artifacts.
  - Build Docker images with dynamic tags.
  - Push images to a private DockerHub repository.
  - Commit version updates back to the Git repository.
- Avoid commit loops by preventing automatic pipeline triggers for CI build commits.

---

This repository serves as a demonstration of Jenkins capabilities in CI/CD workflows.
