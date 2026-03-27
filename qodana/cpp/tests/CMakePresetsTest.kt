package org.jetbrains.qodana.cpp

import org.junit.jupiter.api.Test
import kotlin.io.path.appendText
import kotlin.io.path.deleteIfExists
import kotlin.io.path.deleteRecursively
import kotlin.io.path.div
import kotlin.test.assertEquals

class CMakePresetsTest : IntegrationTest() {
  @Test
  fun testBugsPreset() {
    doTest("cpp:\n  cmakePreset: bugs", Result.Succeeded(listOf("CppDFANullDereference")))
  }

  @Test
  fun testNobugsPreset() {
    doTest("cpp:\n  cmakePreset: nobugs", Result.Succeeded(emptyList()))
  }

  @Test
  fun testNoPreset() {
    doTest("", Result.Succeeded(emptyList()))
  }

  @Test
  fun testMissingPreset() {
    doTest("cpp:\n  cmakePreset: missing", Result.Failed("Cannot select CMake preset: preset \"missing\" was not found"))
  }

  @Test
  fun testNoYaml() {
    val cwd = checkout("cpp-presets-test")
    (cwd / "qodana.yaml").deleteIfExists()

    val expectedResult = Result.Succeeded(emptyList())
    val result = analyzeProject(cwd)
    assertEquals(expectedResult, result)
  }

  fun doTest(yamlFileSuffix: String, expectedResult: Result) {
    // Without .idea
    val test1 = checkout("cpp-presets-test")
    (test1 / "qodana.yaml").appendText("\n$yamlFileSuffix")
    (test1 / ".idea").deleteRecursively()

    val result1 = analyzeProject(test1)
    assertEquals(expectedResult, result1)

    // With .idea
    val test2 = checkout("cpp-presets-test")
    (test2 / "qodana.yaml").appendText("\n$yamlFileSuffix")
    //(test2 / ".idea").deleteRecursively()

    val result2 = analyzeProject(test2)
    assertEquals(expectedResult, result2)
  }
}