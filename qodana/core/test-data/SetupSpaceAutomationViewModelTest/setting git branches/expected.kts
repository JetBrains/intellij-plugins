job("Qodana") {
  startOn {
    gitPush {
      anyBranchMatching {
        +"main"
        +"dev"
        +"test-branch"
      }
    }
    codeReviewOpened{}
  }
  container("jetbrains/qodana-<linter>") {
    env["QODANA_TOKEN"] = "{{ project:qodana-token }}"
    shellScript {
      content = "qodana"
    }
  }
}