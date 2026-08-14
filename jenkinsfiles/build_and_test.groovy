/* groovylint-disable CatchException, DuplicateMapLiteral, DuplicateStringLiteral, ImplicitReturnStatement, LineLength, NestedBlockDepth, ParameterCount */

def actualBranch = (env.CHANGE_BRANCH ?: (env.BRANCH_NAME ?: 'main'))

pipeline {
    agent {
        docker {
            image 'mcr.microsoft.com/playwright:v1.62.1-noble'
        }
    }

    options {
        quietPeriod(60)
        disableConcurrentBuilds()
        timeout(time: 3, unit: 'HOURS')
        skipDefaultCheckout()
    }

    environment {
        GIT_REPO = 'https://github.com/IsaacStarnes/NodeTestRepo'
        NPM_CONFIG_CACHE = "${WORKSPACE}/.npm"
    }

    stages {
        stage('Checkout Repo') {
            steps {
                script {
                    gitCheckout(repo: env.GIT_REPO, branch: actualBranch, skipTriggerCheck: true)
                }
            }
        }

        stage('Install Dependencies') {
            steps {
                dir('src/minimal-node-app') {
                    sh 'npm ci'
                    //sh 'npx playwright install --with-deps'
                }
            }
        }

        stage('Run Tests') {
            steps {
                dir('src/minimal-node-app') {
                    sh 'npm run test:all'
                }
            }
        }
    }
    post {
        always {
            script {
                dir('src/minimal-node-app') {
                    junit testResults: 'test-results/**/*.xml', allowEmptyResults: true
                    publishHTML([
                        reportDir: 'test-results/playwright/report',
                        reportFiles: 'index.html',
                        reportName: 'Playwright Report',
                        allowMissing: true,
                        keepAll: true,
                        alwaysLinkToLastBuild: true,
                        useWrapperFileDirectly: true
                    ])
                }
            }
        }
    }
}
