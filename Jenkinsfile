@Library('jenkins-shared-library')
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
                expression { env.BRANCH_NAME == 'main' || env.BRANCH_NAME == 'jenkins-shared-lib' }
            }
            steps {
                script {
                    echo "Current branch: ${env.BRANCH_NAME}"
                    buildJar()
                }
            }
        }

        stage("build image") {
            when {
                expression { env.BRANCH_NAME == 'main' || env.BRANCH_NAME == 'jenkins-shared-lib' }
            }
            steps {
                script {
                    echo "Current branch: ${env.BRANCH_NAME}"
                    buildImage 'syleniainc/demo-app:2.0'
                }
            }
        }

        stage("deploy") {
            when {
                expression { env.BRANCH_NAME == 'main' || env.BRANCH_NAME == 'jenkins-shared-lib' }
            }
            steps {
                script {
                    echo "Current branch: ${env.BRANCH_NAME}"
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
