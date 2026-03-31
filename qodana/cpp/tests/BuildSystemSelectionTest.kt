package org.jetbrains.qodana.cpp

import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.io.path.div
import kotlin.io.path.writeText

class BuildSystemSelectionTest : IntegrationTest() {
  @Test
  fun `select CMake build system`() {
    val cwd = checkout("build-system-selection-test")
    (cwd / "qodana.yaml").writeText("""
      cpp:
        buildSystem: CMake
    """.trimIndent())

    val result = analyze(cwd)
    assertThat(result.ok).isTrue()
    assertThat(result.results!!.map { it.ruleId }).containsExactly("CppDFANullDereference")
  }

  @Test
  fun `select CompDB build system`() {
    val cwd = checkout("build-system-selection-test")
    (cwd / "qodana.yaml").writeText("""
      cpp:
        buildSystem: CompDB
    """.trimIndent())

    // Note: compile_commands.json is created at runtime because it needs absolute paths.
    (cwd / "compile_commands.json").writeText("""
      [
        {
          "directory": ${Json.encodeToString(cwd.toString())},
          "file": "main.cpp",
          "arguments": ["c++", "main.cpp", "-o", "main"]
        }
      ]
    """.trimIndent())

    val result = analyze(cwd)
    assertThat(result.ok).isTrue()
    assertThat(result.results!!.map { it.ruleId }).containsExactly("CppDFANullDereference")
  }

  @Test
  fun `select invalid build system`() {
    val cwd = checkout("build-system-selection-test")
    (cwd / "qodana.yaml").writeText("""
      cpp:
        buildSystem: INVALID
    """.trimIndent())

    val result = analyze(cwd)
    assertThat(result.ok).isFalse()
    assertThat(result.ideaLog).contains("Specified build system 'INVALID' is not supported by Qodana")
  }

  @Test
  fun `select no build system`() {
    val cwd = checkout("build-system-selection-test")
    val result = analyze(cwd)
    assertThat(result.ok).isTrue()
    assertThat(result.results!!.map { it.ruleId }).containsExactly("CppDFANullDereference")
  }
}
