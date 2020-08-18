pipeline {
  agent any
  stages {
    stage('Build') {
      steps {
        bat 'test.bat'
      }
    }

    stage('Change Name') {
      steps {
        buildName '${SVNREV}'
      }
    }

  }
}