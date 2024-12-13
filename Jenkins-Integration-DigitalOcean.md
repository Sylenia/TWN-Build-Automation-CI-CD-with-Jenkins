# Demo Project: Jenkins Integration on DigitalOcean

## Overview
This project demonstrates how to set up Jenkins on a DigitalOcean Ubuntu server and run it as a Docker container. The goal is to showcase the integration of Jenkins into CI/CD pipeline jobs.

## Technologies Used
- **Jenkins**: For continuous integration and delivery.
- **Docker**: To containerize Jenkins.
- **DigitalOcean**: To host the Jenkins server.
- **Linux**: Operating system for the server (Ubuntu).

---

## Project Description
This guide walks through the steps to:
1. Create an Ubuntu server on DigitalOcean.
2. Set up and run Jenkins in a Docker container on the server.
3. Initialize Jenkins for first use.

---

## Prerequisites
- A DigitalOcean Droplet with Ubuntu installed.
- Basic knowledge of Linux command-line operations.

---

## Steps

### 1. Update the Server
Ensure the Ubuntu server is up to date:
```bash
sudo apt update && sudo apt upgrade -y
```

### 2. Install Docker
Install Docker to run Jenkins as a container:
```bash
sudo apt install -y docker.io
```
Enable and start the Docker service:
```bash
sudo systemctl enable docker
sudo systemctl start docker
```
Verify Docker installation:
```bash
docker --version
```

### 3. Run Jenkins as a Docker Container
Pull the official Jenkins Docker image:
```bash
docker pull jenkins/jenkins:lts
```

Create a directory on the server for Jenkins data:
```bash
sudo mkdir -p /var/jenkins_home
sudo chown -R 1000:1000 /var/jenkins_home
```

Run the Jenkins container:
```bash
docker run -d \
  --name jenkins \
  -p 8080:8080 -p 50000:50000 \
  -v /var/jenkins_home:/var/jenkins_home \
  jenkins/jenkins:lts
```

### 4. Access Jenkins
- Open your browser and navigate to `http://<YOUR_SERVER_IP>:8080`.
- Retrieve the Jenkins initial admin password:
  ```bash
  sudo cat /var/jenkins_home/secrets/initialAdminPassword
  ```
- Enter the password into the Jenkins web interface to unlock it.

### 5. Complete Jenkins Setup
- Follow the on-screen instructions to install recommended plugins.
- Create your first admin user and finalize the setup.

---

## Notes
- Make sure to open port **8080** on your DigitalOcean server to allow external access.
  ```bash
  sudo ufw allow 8080
  ```
- Regularly update Jenkins and Docker for security and performance.
  ```bash
  docker pull jenkins/jenkins:lts
  docker restart jenkins
  ```

---

## Conclusion
This project demonstrated setting up Jenkins on a DigitalOcean Ubuntu server using Docker. With Jenkins running, you can now integrate CI/CD pipelines for your projects.
