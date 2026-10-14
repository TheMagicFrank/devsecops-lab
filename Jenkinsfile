// Pipeline CI del laboratorio DevSecOps
// Busca secretos en el historial con Gitleaks, compila, testea, escanea dependencias
// y configuración con Trivy, analiza con SonarQube y se para si algo no pasa los controles
pipeline {
    agent any

    options {
        timestamps()
        timeout(time: 30, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    environment {
        // Versión de Trivy fijada (nunca "latest") y huella SHA-256 de su archivo de checksums,
        // copiada de la página oficial de la release en GitHub
        TRIVY_VERSION = '0.75.0'
        TRIVY_CHECKSUMS_SHA256 = '43a87485d39b60dae55cb1a14725dddd612040c0b1ec4caa88948c07d6c06219'
        TRIVY = "${JENKINS_HOME}/tools/trivy-${TRIVY_VERSION}/trivy"

        // Gitleaks: misma política, versión fijada y huella del checksums.txt verificada en la release oficial
        GITLEAKS_VERSION = '8.30.1'
        GITLEAKS_CHECKSUMS_SHA256 = '061476c21adaf5441516f96f185c1a4706a83cd6329b9b38762271b3d4a52fae'
        GITLEAKS = "${JENKINS_HOME}/tools/gitleaks-${GITLEAKS_VERSION}/gitleaks"
    }

    stages {
        stage('Instalar Gitleaks (verificado)') {
            steps {
                sh '''
                    set -eu
                    GL_DIR="$JENKINS_HOME/tools/gitleaks-$GITLEAKS_VERSION"
                    if [ ! -x "$GL_DIR/gitleaks" ]; then
                        mkdir -p "$GL_DIR"
                        cd "$GL_DIR"
                        BASE="https://github.com/gitleaks/gitleaks/releases/download/v$GITLEAKS_VERSION"
                        curl -fsSLO "$BASE/gitleaks_${GITLEAKS_VERSION}_checksums.txt"
                        echo "$GITLEAKS_CHECKSUMS_SHA256  gitleaks_${GITLEAKS_VERSION}_checksums.txt" | sha256sum -c -
                        curl -fsSLO "$BASE/gitleaks_${GITLEAKS_VERSION}_linux_x64.tar.gz"
                        grep " gitleaks_${GITLEAKS_VERSION}_linux_x64.tar.gz$" "gitleaks_${GITLEAKS_VERSION}_checksums.txt" | sha256sum -c -
                        tar -xzf "gitleaks_${GITLEAKS_VERSION}_linux_x64.tar.gz" gitleaks
                        rm -f "gitleaks_${GITLEAKS_VERSION}_linux_x64.tar.gz"
                    fi
                    "$GL_DIR/gitleaks" version
                '''
            }
        }

        stage('Gitleaks: secretos en el historial') {
            steps {
                // Primer control: si hay un secreto en cualquier commit, no se compila nada más.
                // --redact evita que el secreto aparezca en los logs de Jenkins
                sh '"$GITLEAKS" git --redact -v --exit-code 1 .'
            }
        }

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

        stage('Instalar Trivy (verificado)') {
            steps {
                // Cadena de confianza: la huella fijada valida el archivo de checksums,
                // y el archivo de checksums valida el binario. Si algo no coincide, el pipeline se para.
                sh '''
                    set -eu
                    TRIVY_DIR="$JENKINS_HOME/tools/trivy-$TRIVY_VERSION"
                    if [ ! -x "$TRIVY_DIR/trivy" ]; then
                        mkdir -p "$TRIVY_DIR"
                        cd "$TRIVY_DIR"
                        BASE="https://github.com/aquasecurity/trivy/releases/download/v$TRIVY_VERSION"
                        curl -fsSLO "$BASE/trivy_${TRIVY_VERSION}_checksums.txt"
                        echo "$TRIVY_CHECKSUMS_SHA256  trivy_${TRIVY_VERSION}_checksums.txt" | sha256sum -c -
                        curl -fsSLO "$BASE/trivy_${TRIVY_VERSION}_Linux-64bit.tar.gz"
                        grep " trivy_${TRIVY_VERSION}_Linux-64bit.tar.gz$" "trivy_${TRIVY_VERSION}_checksums.txt" | sha256sum -c -
                        tar -xzf "trivy_${TRIVY_VERSION}_Linux-64bit.tar.gz" trivy
                        rm -f "trivy_${TRIVY_VERSION}_Linux-64bit.tar.gz"
                    fi
                    "$TRIVY_DIR/trivy" --version
                '''
            }
        }

        stage('Trivy: dependencias (SCA)') {
            steps {
                // Vulnerabilidades conocidas (CVE) en las librerías; falla si hay HIGH o CRITICAL con parche disponible
                sh '"$TRIVY" fs --scanners vuln --severity HIGH,CRITICAL --ignore-unfixed --exit-code 1 --no-progress app'
            }
        }

        stage('Trivy: configuración (IaC)') {
            steps {
                // Malas configuraciones en el Dockerfile; falla si hay HIGH o CRITICAL
                sh '"$TRIVY" config --severity HIGH,CRITICAL --exit-code 1 .'
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
            echo 'Pipeline OK: Gitleaks, build, tests, Trivy y quality gate superados'
        }
        failure {
            echo 'Pipeline FALLIDO: revisa la etapa en rojo'
        }
    }
}
