package org.jetbrains.qodana.cpp

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.io.path.appendText
import kotlin.io.path.deleteIfExists
import kotlin.io.path.deleteRecursively
import kotlin.io.path.div

class CMakePresetsTest : IntegrationTest() {
  @Test
  fun testBugsPreset() {
    doTest("cpp:\n  cmakePreset: bugs", listOf("CppDFANullDereference"))
  }

  @Test
  fun testNobugsPreset() {
    doTest("cpp:\n  cmakePreset: nobugs", emptyList())
  }

  @Test
  fun testNoPreset() {
    doTest("", emptyList())
  }

  @Test
  fun testMissingPreset() {
    // Without .idea
    val test1 = checkout("cpp-presets-test")
    (test1 / "qodana.yaml").appendText("\ncpp:\n  cmakePreset: missing")
    (test1 / ".idea").deleteRecursively()
    val result1 = analyze(test1)
    assertThat(result1.ok).isFalse()
    assertThat(result1.ideaLog).contains("Cannot select CMake preset: preset \"missing\" was not found")

    // With .idea
    val test2 = checkout("cpp-presets-test")
    (test2 / "qodana.yaml").appendText("\ncpp:\n  cmakePreset: missing")
    val result2 = analyze(test2)
    assertThat(result2.ok).isFalse()
    assertThat(result2.ideaLog).contains("Cannot select CMake preset: preset \"missing\" was not found")
  }

  @Test
  fun testNoYaml() {
    val cwd = checkout("cpp-presets-test")
    (cwd / "qodana.yaml").deleteIfExists()
    val result = analyze(cwd)
    assertThat(result.ok).isTrue()
    assertThat(result.results!!.map { it.ruleId }).isEmpty()
  }

  private fun doTest(yamlSuffix: String, expectedProblems: List<String>) {
    // Without .idea
    val test1 = checkout("cpp-presets-test")
    (test1 / "qodana.yaml").appendText("\n$yamlSuffix")
    (test1 / ".idea").deleteRecursively()
    val result1 = analyze(test1)
    assertThat(result1.ok).isTrue()
    assertThat(result1.results!!.map { it.ruleId }).containsExactlyInAnyOrder(*expectedProblems.toTypedArray())

    // With .idea
    val test2 = checkout("cpp-presets-test")
    (test2 / "qodana.yaml").appendText("\n$yamlSuffix")
    val result2 = analyze(test2)
    assertThat(result2.ok).isTrue()
    assertThat(result2.results!!.map { it.ruleId }).containsExactlyInAnyOrder(*expectedProblems.toTypedArray())
  }
}