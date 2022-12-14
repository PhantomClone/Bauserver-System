pipeline {

    agent any

    environment {
          mavenHome = tool 'Maven'
          PATH = "$mavenHome/bin:$PATH"
    }

    stages {
        stage('Build all') {
            steps {
                sh 'mvn clean install'
            }
        }
        stage('Package') {
            steps {
                sh 'mvn package'
            }
        }
        stage('Deploy API') {
            steps {
                sh 'mvn deploy -f Bauserver-System-API/pom.xml'
            }
        }
        stage('Dev deployment') {
            when {
                branch 'dev'
            }
            steps {
                sh 'echo "Deploying to dev environment..."'
            }
        }
        stage('Prod deployment') {
            when {
                branch 'master'
            }
            steps {
                sh 'echo "Deploying to prod environment..."'
                sh "#!/bin/bash \n" +
                   "sftp -o StrictHostKeyChecking=no jenkins@172.17.0.1:builds/ <<< \$'put Bauserver-System-Impl/target/Bauserver-System-Impl-*.jar'"
            }
        }
    }

    post {
        success {
            archiveArtifacts artifacts: 'Bauserver-System-Impl/target/Bauserver-System-Impl-*.jar'
        }
    }

}