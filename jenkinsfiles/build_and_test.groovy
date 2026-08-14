/* groovylint-disable CatchException, DuplicateMapLiteral, DuplicateStringLiteral, ImplicitReturnStatement, LineLength, NestedBlockDepth, ParameterCount */

def actualBranch = (env.CHANGE_BRANCH ?: (env.BRANCH_NAME ?: 'main'))

pipeline {
    //Change this when we get dedicated performance test agent
    //agent { label 'webviewer-performance-vm' }
    agent {
        docker {
            image 'webviewer-tests:latest'
            registryUrl 'https://448036597521.dkr.ecr.us-west-1.amazonaws.com'
            registryCredentialsId 'ecr:us-west-1:instance-ecr-pull-role'
        }
    }

    options {
        quietPeriod(60)
        disableConcurrentBuilds()
        timeout(time: 3, unit: 'HOURS')
        skipDefaultCheckout()
    }

    environment {
        GIT_REPO = 'https://github.com/XodoDocs/webviewer'
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
                    sh 'npm install'
                }
            }
        }

        stage('Run Tests') {
            steps {
                dir('src/minimal-node-app') {
                    sh 'npm test'
                }
            }
        }


    }
    post {
        always {
            script {
                if (fileExists('src/minimal-node-app/test-results')) {
                    junit testResults: 'src/minimal-node-app/test-results/**/*.xml', allowEmptyResults: true
                }
            }
        }
    }
}
