job("Qodana") {
  container("jetbrains/qodana-<linter>") {
    env["QODANA_TOKEN"] = Secrets("qodana-token")
    shellScript {
      content = """
               qodana
               """.trimIndent()
    }
  }
}