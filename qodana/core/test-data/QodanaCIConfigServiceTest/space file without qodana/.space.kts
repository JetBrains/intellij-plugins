job("Qodana") {
  container("other") {
    env["QODANA_TOKEN"] = Secrets("qodana-token")
    shellScript {
      content = """
               qodana
               """.trimIndent()
    }
  }
}