package org.jetbrains.qodana.cpp

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import org.junit.jupiter.api.Test
import kotlin.io.path.appendText
import kotlin.io.path.deleteRecursively
import kotlin.io.path.div

class CMakeOutputTest : CppIntegrationTest() {

  @Test
  fun `cmake output is printed to stdout`() {
    val cwd = checkout("cpp-presets-test")
    (cwd / ".idea").deleteRecursively()

    val result = analyze(cwd)
    result.ok shouldBe true
    result.stdout.shouldContain("CMake configure output:")
  }

  @Test
  fun `cmake output appears once even with preset reload`() {
    val cwd = checkout("cpp-presets-test")
    (cwd / "qodana.yaml").appendText("\ncpp:\n  cmakePreset: bugs")
    (cwd / ".idea").deleteRecursively()

    val result = analyze(cwd)
    result.ok shouldBe true

    val label = "CMake configure output:"
    val count = Regex.fromLiteral(label).findAll(result.stdout).count()
    count shouldBe 1
  }

  @Test
  fun `cmake configure failure is detected early`() {
    val cwd = checkout("cmake-broken-test")

    val result = analyze(cwd)
    result.ok shouldBe false
    result.stdout.shouldContain("CMake configure failed (exit code")
    result.stdout.shouldNotContain("timeout reached")
  }

  @Test
  fun `stdout has no stack traces`() {
    val cwd = checkout("cpp-presets-test")
    (cwd / ".idea").deleteRecursively()

    val result = analyze(cwd)
    result.ok shouldBe true
    result.stdout.shouldNotContain("\tat ")
  }
}
