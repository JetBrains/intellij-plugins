job("Qodana") {
  startOn {
    gitPush {
      anyBranchMatching {
        +"main"
      }
    }
    codeReviewOpened{}
  }
  container("jetbrains/qodana-<linter>") {
    env["QODANA_TOKEN"] = "{{ project:qodana-token }}"
    shellScript {
      content = "qodana --baseline qodana.sarif.json"
    }
  }
}