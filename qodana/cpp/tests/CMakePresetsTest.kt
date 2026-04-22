package org.jetbrains.qodana.cpp

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test
import utilities.qodana.QodanaAnalysisResult
import kotlin.io.path.appendText
import kotlin.io.path.deleteIfExists
import kotlin.io.path.deleteRecursively
import kotlin.io.path.div

class CMakePresetsTest : CppIntegrationTest() {

  @Test
  fun `bugs preset finds issues`() = withAndWithoutIdea("\ncpp:\n  cmakePreset: bugs") {
    it.ok shouldBe true
    it.findIssue("CppDFANullDereference").shouldNotBeNull()
  }

  @Test
  fun `nobugs preset finds no issues`() = withAndWithoutIdea("\ncpp:\n  cmakePreset: nobugs") {
    it.ok shouldBe true
    it.results!!.shouldBeEmpty()
  }

  @Test
  fun `no preset specified finds no issues`() = withAndWithoutIdea {
    it.ok shouldBe true
    it.results!!.shouldBeEmpty()
  }

  @Test
  fun `missing preset fails with error`() = withAndWithoutIdea("\ncpp:\n  cmakePreset: missing") {
    it.ok shouldBe false
    it.stdout.shouldContain("Cannot select CMake preset: preset \"missing\" was not found")
  }

  @Test
  fun `no qodana yaml finds no issues`() {
    val cwd = checkout("cpp-presets-test")
    (cwd / "qodana.yaml").deleteIfExists()

    val result = analyze(cwd)
    result.ok shouldBe true
    result.results!!.shouldBeEmpty()
  }

  private fun withAndWithoutIdea(
    yamlSuffix: String = "",
    check: (QodanaAnalysisResult) -> Unit,
  ) {
    // Without .idea
    val test1 = checkout("cpp-presets-test")
    if (yamlSuffix.isNotEmpty()) {
      (test1 / "qodana.yaml").appendText(yamlSuffix)
    }
    (test1 / ".idea").deleteRecursively()
    check(analyze(test1))

    // With .idea
    val test2 = checkout("cpp-presets-test")
    if (yamlSuffix.isNotEmpty()) {
      (test2 / "qodana.yaml").appendText(yamlSuffix)
    }
    check(analyze(test2))
  }
}
