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
