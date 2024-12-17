# Demo Project: Continuous Deployment to AWS EC2 from Jenkins Pipeline

## **Project Overview**
This project demonstrates how to automate the deployment of a Dockerized Java application to an AWS EC2 instance using Jenkins. The Jenkins server is hosted on a DigitalOcean droplet, and the deployment target server is an AWS EC2 instance.

---

## **Technologies Used**
- **AWS**: For EC2 instance hosting.
- **Jenkins**: For automating CI/CD pipelines.
- **Docker**: To containerize and deploy the application.
- **Linux**: For server configuration.
- **Git**: For version control.
- **Java & Maven**: To build the application.
- **Docker Hub**: To store and retrieve the Docker images.
- **Docker Compose**: For multi-container orchestration.

---

## **Project Steps**

### **1. Prerequisites**
Ensure you have the following ready:
- A Jenkins server running on a DigitalOcean droplet.
- An AWS account with permissions to create and manage EC2 instances.
- SSH key pair configured for Jenkins to access the EC2 instance.
- A private or public Docker Hub repository for storing the application image.
- A web application already containerized.

---

### **2. Configure AWS Security Group**
1. Log in to the AWS Management Console.
2. Navigate to **EC2 Dashboard** → **Security Groups**.
3. Modify the security group attached to your EC2 instance:
   - **Allow inbound SSH (port 22)**:
     - Source: **Jenkins server IP** (DigitalOcean droplet IP).
   - **Allow inbound HTTP (port 80)**:
     - Source: `0.0.0.0/0` (accessible to everyone).
4. Save the security group changes.

---

### **3. Configure SSH Credentials in Jenkins**
1. Ensure you already have an **existing SSH key pair** that was created for your AWS EC2 instance.

2. **Install the SSH Agent Plugin** in Jenkins (if not already installed):
   - Go to **Manage Jenkins** → **Manage Plugins** → Search for **SSH Agent Plugin** → Install it.

3. **Add the AWS EC2 key pair to Jenkins Credentials**:
   - Go to **Jenkins Dashboard** → **Manage Jenkins** → **Manage Credentials**.
   - Select **Global credentials** → **Add Credentials**.
   - Choose the following settings:
     - **Kind**: SSH Username with private key
     - **ID**: `ec2-server-key`
     - **Username**: `ec2-user` (or appropriate user for your AWS instance)
     - **Private Key**: Paste the **existing private key** (e.g., `.pem` file content).

4. Save the credentials.

---

### **4. Prepare the AWS EC2 Instance**
SSH into your EC2 instance and install Docker:

```bash
# Update and install Docker
sudo yum update -y
sudo amazon-linux-extras install docker -y

# Start Docker service
sudo service docker start

# Add ec2-user to Docker group
sudo usermod -aG docker ec2-user

# Verify Docker installation
docker --version
```

---

### **5. Jenkins Pipeline Configuration**
Extend the Jenkinsfile to include the deployment step that SSHes into the EC2 instance and runs the Dockerized application.

#### **Pipeline Script (Jenkinsfile)**
```groovy
def gv

pipeline {
    agent any
    tools {
        maven 'maven-3.9'
    }
    stages {
        stage("init") {
            steps {
                script {
                    gv = load "script.groovy"
                }
            }
        }

        stage("build jar") {
            when {
                expression { env.BRANCH_NAME == 'main' || env.BRANCH_NAME == 'master' }
            }
            steps {
                script {
                    echo "Current branch: ${env.BRANCH_NAME}"
                    gv.buildJar()
                }
            }
        }

        stage("build image") {
            when {
                expression { env.BRANCH_NAME == 'main' || env.BRANCH_NAME == 'master' }
            }
            steps {
                script {
                    echo "Current branch: ${env.BRANCH_NAME}"
                    gv.buildImage()
                }
            }
        }

        stage("deploy") {
            when {
                expression { env.BRANCH_NAME == 'main' || env.BRANCH_NAME == 'master' }
            }
            steps {
                script {
                    echo "Current branch: ${env.BRANCH_NAME}"
                    
                    sshagent(['ec2-server-key']) {
                        sh '''
                            ssh -o StrictHostKeyChecking=no ec2-user@<EC2-Public-IP> \
                            "docker pull <dockerhub-username>/<repo-name>:latest && \
                            docker stop demo-app || true && \
                            docker rm demo-app || true && \
                            docker run -d -p 80:3000 --name demo-app <dockerhub-username>/<repo-name>:latest"
                        '''
                    }
                    gv.deployApp()
                }
            }
        }
    }
    post {
        always {
            echo "Pipeline finished. Cleaning up workspace."
            cleanWs()
        }
        success {
            echo "Pipeline completed successfully on branch: ${env.BRANCH_NAME}"
        }
        failure {
            echo "Pipeline failed on branch: ${env.BRANCH_NAME}"
        }
    }
}
```

Replace placeholders:
- `<EC2-Public-IP>`: Public IP of your EC2 instance.
- `<dockerhub-username>/<repo-name>`: Docker Hub image repository.

---

### **6. Run the Jenkins Pipeline**
1. Commit and push the updated Jenkinsfile to your Git repository.
2. Trigger the Jenkins pipeline job.
3. Verify the following:
   - The pipeline builds the application JAR file.
   - The Docker image is created and pushed to Docker Hub.
   - The `deploy` stage connects to the EC2 instance and runs the container.

---

### **7. Access the Web Application**
Open a web browser and go to:
```
http://<EC2-Public-IP>
```
You should see your application running.

---

### **8. Install Docker Compose on EC2**
SSH into your EC2 instance and run the following commands to install Docker and Docker Compose:

```bash
# Install Docker Compose
sudo curl -L https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m) -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Verify installations
docker --version
docker-compose --version
```

---

### **9. Create the `docker-compose.yml` File**
Create a `docker-compose.yml` file in your project repository to define your web application deployment:

```yaml
version: '3.8'
services:
    java-maven-app:
      image: ${IMAGE}
      ports:
        - 8080:8080
    postgres:
      image: postgres:15
      ports:
       - 5432:5432
      environment: 
       - POSTGRES_PASSWORD=my-password
```
- Adjust the ports as necessary.

Commit the file to your project repository.

---

### **10. Create a Shell Script for Deployment**
Create a shell script file named `example.sh` in your project repository to automate Docker Compose execution:

```bash
#!/bin/bash

# Stop and remove existing containers
docker-compose down

# Pull the latest image from Docker Hub
docker-compose pull

# Start the application using Docker Compose
docker-compose up -d

# Clean up unused images
docker image prune -f
```

Make the script executable:
```bash
chmod +x example.sh
```

Commit this file to your project repository.

---

### **11. Configure Jenkins Pipeline**
Update your `Jenkinsfile` to include the `docker-compose.yml` and execute the `deploy.sh` script on the remote EC2 instance.

```groovy
def gv

pipeline {
    agent any
    tools {
        maven 'maven-3.9'
    }
    stages {
        stage("init") {
            steps {
                script {
                    gv = load "script.groovy"
                }
            }
        }

        stage("build jar") {
            when {
                expression { env.BRANCH_NAME == 'main' || env.BRANCH_NAME == 'master' }
            }
            steps {
                script {
                    echo "Current branch: ${env.BRANCH_NAME}"
                    gv.buildJar()
                }
            }
        }

        stage("build image") {
            when {
                expression { env.BRANCH_NAME == 'main' || env.BRANCH_NAME == 'master' }
            }
            steps {
                script {
                    echo "Current branch: ${env.BRANCH_NAME}"
                    gv.buildImage()
                }
            }
        }

        stage("deploy") {
            when {
                expression { env.BRANCH_NAME == 'main' || env.BRANCH_NAME == 'master' }
            }
            steps {
                script {
                    echo "Current branch: ${env.BRANCH_NAME}"
                    def shellCmd = "bash ./server-cmds.sh ${IMAGE_NAME}"
                    // SSH to Amazon Server Instance using sshagent
                    sshagent(['ec2-server-key']) {
                        sh "scp server-cmds.sh ec2-user@<amazon-server-ip>:/home/ec2-user"
                        sh "scp docker-compose.yaml ec2-user@<amazon-server-ip>:/home/ec2-user"
                        sh '''
                            ssh -o StrictHostKeyChecking=no ec2-user@<amazon-server-ip> ${shellCmd} \
                            "docker pull <your-dockerhub-username>/<your-repo-name>:latest && \
                            docker stop demo-app || true && \
                            docker rm demo-app || true && \
                            docker run -d -p 3080:3080 --name demo-app <your-dockerhub-username>/<your-repo-name>:latest"
                        '''
                    }
                    gv.deployApp()
                }
            }
        }
    }
    post {
        always {
            echo "Pipeline finished. Cleaning up workspace."
            cleanWs()
        }
        success {
            echo "Pipeline completed successfully on branch: ${env.BRANCH_NAME}"
        }
        failure {
            echo "Pipeline failed on branch: ${env.BRANCH_NAME}"
        }
    }
}
```

Replace placeholders:
- `<EC2-Public-IP>`: Public IP of your EC2 instance.
- `<dockerhub-username>/<repo-name>`: Docker Hub image repository.

### **12. Run the Jenkins Pipeline**
1. Commit and push the `docker-compose.yml`, `deploy.sh`, and updated `Jenkinsfile` to your Git repository.
2. Trigger the Jenkins pipeline.
3. Monitor the pipeline stages:
   - Build the Maven project.
   - Build and push the Docker image.
   - SSH into the EC2 instance and execute `deploy.sh` using Docker Compose.

---

### **13. Verify the Deployment**
1. SSH into your EC2 instance:
   ```bash
   ssh -i "<aws-key.pem>" ec2-user@<EC2-Public-IP>
   ```
2. Verify the running containers:
   ```bash
   docker-compose ps
   ```
3. Access the web application in your browser:
   ```
   http://<EC2-Public-IP>
   ```

---

## **14. Dynamic Versioning and Commit Version Update**
To incorporate version control and dynamic versioning in the pipeline:

1. **Increment the Application Version**
   Use environment variables to increment the version dynamically:
   ```groovy
   def APP_VERSION = sh(script: 'date +%Y%m%d%H%M%S', returnStdout: true).trim()
   ```

2. **Tag Docker Images with the Version**
   Update the `DOCKER_IMAGE` variable:
   ```groovy
   environment {
       DOCKER_IMAGE = "<dockerhub-username>/<repo-name>:${APP_VERSION}"
   }
   ```

3. **Commit the Version Update**
   Add a step to commit and push the updated version to the Git repository:
   ```groovy
   stage("Commit Version") {
       steps {
           sh '''
               git config user.name "jenkins"
               git config user.email "jenkins@ci-cd.com"
               git commit -am "Version updated to ${APP_VERSION}"
               git push origin main
           '''
       }
   }
   ```

This ensures:
- Dynamic version tags for Docker images.
- The updated version is committed back to the repository.

---


## **Conclusion**
This project automates the deployment process from Jenkins to AWS EC2 using Docker and SSH. By following these steps, you have achieved a fully automated CI/CD pipeline with Jenkins, Docker, and AWS.

---

## **Commands Summary**
| Step                           | Command(s)                                                                      |
|--------------------------------|---------------------------------------------------------------------------------|
| Configure Existing SSH Key     | Paste AWS keypair into Jenkins credentials                                      |
| Install Docker on EC2          | `sudo yum update -y && sudo amazon-linux-extras install docker -y`             |
| Start Docker Service           | `sudo service docker start`                                                    |
| Verify Docker Installation     | `docker --version`                                                             |
| Jenkins Pipeline Deployment    | Run the Jenkins pipeline and monitor stages                                    |
| Verify App Deployment          | Visit `http://<EC2-Public-IP>`                                                 |

---

Happy Deploying and  Automating! 🚀
