package org.intellij.qodana.ijVoid

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayNameGeneration
import org.junit.jupiter.api.DisplayNameGenerator
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.minutes

@DisplayNameGeneration(DisplayNameGenerator.Simple::class)
class OpenGrepAnalysisTest : IntegrationTest() {
  @Test
  fun `custom OpenGrep rules produce SARIF results`() {
    val workdir = checkout("opengrep-custom-rules")

    val result = analyze(workdir) {
      timeout = 10.minutes
      vm.properties["qodana.product.name"] = "Qodana for IJ Void"
    }

    assertTrue(result.ok, "Qodana failed with exit code ${result.exitCode}:\n${result.stdout}")
    assertTrue(result.ideaLog.contains("IDE: Qodana for IJ Void"), "QDIV startup marker is missing from idea.log")
    assertNotNull(result.findIssue("hardcoded-password", "test-file.py:5"))
    assertNotNull(result.findIssue("hardcoded-secret", "test-file.py:5"))
    assertNotNull(result.findIssue("print-statement", "test-file.py:15"))
    assertNotNull(result.findIssue("sql-string-concat", "test-file.py:10"))
    assertEquals(
      listOf("hardcoded-password", "hardcoded-secret", "print-statement", "sql-string-concat"),
      result.results!!.map { it.ruleId }.sorted()
    )
  }
}
