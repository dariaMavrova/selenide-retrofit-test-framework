// Same suites as .github/workflows/tests.yml, for Jenkins (see jenkins/ for a ready-to-run local controller).
pipeline {
    agent any

    options {
        timeout(time: 30, unit: 'MINUTES')
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '20'))
        disableConcurrentBuilds()
    }

    environment {
        // UI tests drive the Chromium container from jenkins/docker-compose.yml.
        SELENIUM_URL = 'http://selenium:4444/wd/hub'
    }

    stages {
        stage('Compile') {
            steps {
                sh './gradlew --no-daemon testClasses'
            }
        }

        stage('Tests') {
            steps {
                // One Gradle run: test classes already run in parallel inside JUnit.
                sh "./gradlew --no-daemon test -Dselenide.remote=${env.SELENIUM_URL}"
            }
        }
    }

    post {
        always {
            junit testResults: 'build/test-results/test/*.xml', allowEmptyResults: true
            allure results: [[path: 'build/allure-results']], includeProperties: false
            archiveArtifacts artifacts: 'build/reports/selenide/**', allowEmptyArchive: true
        }
        cleanup {
            cleanWs()
        }
    }
}
