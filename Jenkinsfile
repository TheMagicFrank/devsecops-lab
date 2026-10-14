// Pipeline CI del laboratorio DevSecOps
// Compila, ejecuta los tests, analiza con SonarQube y se para si no pasa el quality gate
pipeline {
    agent any

    options {
        timestamps()
        timeout(time: 30, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    stages {
        stage('Build y tests') {
            steps {
                dir('app') {
                    // En Windows el wrapper pierde el permiso de ejecución al subirse a Git
                    sh 'chmod +x mvnw'
                    sh './mvnw -B clean verify'
                }
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'app/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Análisis SonarQube') {
            steps {
                dir('app') {
                    // El token se inyecta desde las credenciales de Jenkins: nunca aparece en el código
                    withSonarQubeEnv('sonarqube') {
                        sh './mvnw -B org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -Dsonar.projectKey=devsecops-lab -Dsonar.projectName=devsecops-lab'
                    }
                }
            }
        }

        stage('Quality Gate') {
            steps {
                // Espera el aviso del webhook de SonarQube; si el código no pasa, el pipeline falla
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }
    }

    post {
        success {
            echo 'Pipeline OK: build, tests y quality gate superados'
        }
        failure {
            echo 'Pipeline FALLIDO: revisa la etapa en rojo'
        }
    }
}
