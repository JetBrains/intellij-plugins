import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildFeatures.perfmon
import jetbrains.buildServer.configs.kotlin.buildSteps.qodana
import jetbrains.buildServer.configs.kotlin.triggers.vcs
import jetbrains.buildServer.configs.kotlin.vcs.GitVcsRoot

version = "2022.10"

project {
  vcsRoot(ProjectVcsRoot)
  buildType(Build)
}

object Build : BuildType({
  name = "Build"
  vcs {
    root(ProjectVcsRoot)
  }
  steps {
    qodana {
      name = "Qodana Step"
      reportAsTests = true
      linter = <linter> {}
      additionalQodanaArguments = "--baseline qodana.sarif.json"
    }
  }
  triggers {
    vcs {}
  }
  features {
    perfmon {}
  }
})

object ProjectVcsRoot : GitVcsRoot({
  name = "https://test-git.com/test-project.git#refs/heads/main"
  url = "https://test-git.com/test-project.git"
  branch = "refs/heads/main"
  branchSpec = """
    refs/heads/dev
    refs/heads/test-branch
  """.trimIndent()
})