/* groovylint-disable CatchException, DuplicateMapLiteral, DuplicateStringLiteral, ImplicitReturnStatement, LineLength, NestedBlockDepth, ParameterCount */

def actualBranch = (env.CHANGE_BRANCH ?: (env.BRANCH_NAME ?: 'main'))

pipeline {
    agent {
        docker {
            image 'mcr.microsoft.com/playwright:v1.63.0-noble'
        }
    }

    options {
        quietPeriod(60)
        disableConcurrentBuilds()
        timeout(time: 5, unit: 'MINUTES')
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

                    sh '''
                        echo "===== TEST RESULTS ====="
                        find test-results -type f | sort
                    '''

                    stash name: 'test-results', includes: "test-results/**/*", allowEmpty: true
                }
            }
        }
    }
    post {
        always {
            node('linux_fleet') {
                script {
                    dir('unstash') {
                        unstash 'test-results'

                        sh '''
                            echo "=== Current Directory ==="
                            pwd

                            echo "=== Files ==="
                            find . -type f | sort
                        '''

                        def allureResults = []
                        allureResults << [
                            path: 'test-results'
                        ]
                        allure(includeProperties: false, results: allureResults)
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
}
