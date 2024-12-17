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

---

## **Project Steps**

### **1. Prerequisites**
Ensure you have the following ready:
- A Jenkins server running on a DigitalOcean droplet.
- An AWS account with permissions to create and manage EC2 instances.
- SSH key pair configured for Jenkins to access the EC2 instance.
- A private or public Docker Hub repository for storing the application image.

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

Happy Deploying! 🚀
