package org.jetbrains.qodana.dart.integration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayNameGeneration
import org.junit.jupiter.api.DisplayNameGenerator
import org.junit.jupiter.api.Test

@DisplayNameGeneration(DisplayNameGenerator.Simple::class)
class DartAnalysisTest : DartIntegrationTest() {
  @Test
  fun `Dart analysis produces SARIF results`() {
    val workdir = checkout("dart-analysis")

    val result = analyze(workdir)

    assertTrue(result.ok, "Qodana failed with exit code ${result.exitCode}:\n${result.stdout}")
    assertTrue(result.ideaLog.contains("IDE: QDPOLY"), "QDPOLY startup marker is missing from idea.log")
    assertNotNull(result.findIssue("await_only_futures", "lib/main.dart:2"))
    assertEquals(listOf("await_only_futures"), result.results!!.mapNotNull { it.ruleId })
  }
}
