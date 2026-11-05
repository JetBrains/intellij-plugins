package org.jetbrains.qodana.dart.integration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayNameGeneration
import org.junit.jupiter.api.DisplayNameGenerator
import org.junit.jupiter.api.Test
import kotlin.io.path.div
import kotlin.io.path.exists

@DisplayNameGeneration(DisplayNameGenerator.Simple::class)
class DartAnalysisTest : DartIntegrationTest() {
  @Test
  fun `Dart analysis produces SARIF results`() {
    val workdir = checkout("dart-analysis")

    val result = analyze(workdir)

    assertTrue(result.ok, "Qodana failed with exit code ${result.exitCode}:\n${result.stdout}")
    assertTrue(result.ideaLog.contains("IDE: QDPOLY"), "QDPOLY startup marker is missing from idea.log")
    assertNotNull(result.findIssue("dart_await_only_futures", "lib/main.dart:2"))
    assertEquals(listOf("dart_await_only_futures"), result.results!!.mapNotNull { it.ruleId })
  }

  @Test
  fun `nested Dart packages keep local lint rules separate`() {
    val workdir = checkout("dart-nested-packages")
    assertFalse((workdir / "pubspec.yaml").exists())

    val result = analyze(workdir)

    assertTrue(result.ok, "Qodana failed with exit code ${result.exitCode}:\n${result.stdout}")
    assertNotNull(result.findIssue("dart_await_only_futures", "first/lib/main.dart:2"))
    assertNotNull(result.findIssue("dart_await_only_futures", "second/lib/main.dart:2"))
    assertNotNull(result.findIssue("dart_avoid_print", "first/lib/main.dart:3"))
    assertNull(result.findIssue("dart_avoid_print", "second/lib/main.dart:3"))
    assertEquals(3, result.results!!.size)
  }
}
