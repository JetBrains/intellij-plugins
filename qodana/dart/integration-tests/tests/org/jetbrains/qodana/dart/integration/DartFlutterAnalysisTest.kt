package org.jetbrains.qodana.dart.integration

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DartFlutterAnalysisTest : DartIntegrationTest() {
  @Test
  fun `Flutter analysis produces Flutter lint`() {
    val workdir = checkout("flutter-analysis")

    val result = analyze(workdir)

    assertTrue(result.ok, "Qodana failed with exit code ${result.exitCode}:\n${result.stdout}")
    assertNotNull(result.findIssue("dart_avoid_print", "lib/main.dart:2"))
    assertTrue(result.stdout.contains("avoid_print"), "Qodana output does not name the Dart lint:\n${result.stdout}")
  }
}
