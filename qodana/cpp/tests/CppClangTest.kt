package org.jetbrains.qodana.cpp

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import kotlin.io.path.div
import kotlin.io.path.writeText

class CppClangTest : CppIntegrationTest() {

  @Test
  fun `custom profile reports a local variable that can be const`() {
    val cwd = checkout("clang-test")
    (cwd / "qodana.yaml").writeText("""
      version: "1.0"
      profile:
        path: inspection-profile.xml
    """.trimIndent())

    val result = analyze(cwd)
    result.ok shouldBe true
    result.results.shouldNotBeNull().map { it.ruleId } shouldBe listOf("CppLocalVariableMayBeConst")
    result.findIssue("CppLocalVariableMayBeConst", "test.cpp:2:10").shouldNotBeNull()
      .message.text shouldBe "Local variable 'f' can be made const"
  }

  @Test
  fun `sanity profile reports an unresolved symbol`() {
    val cwd = checkout("clang-test")
    (cwd / "qodana.yaml").writeText("""
      version: "1.0"
      profile:
        name: qodana.sanity
    """.trimIndent())

    val result = analyze(cwd)
    result.ok shouldBe true
    result.results.shouldNotBeNull().map { it.ruleId } shouldBe listOf("RadGlobal")
    result.findIssue("RadGlobal", "test.cpp:7:9").shouldNotBeNull()
      .message.text shouldBe "Cannot resolve symbol 'undeclared'"
  }
}
