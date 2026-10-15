package com.intellij.dts.lang

import com.intellij.dts.dtsFixture
import com.intellij.dts.dtsTimeoutRunBlocking
import com.intellij.openapi.application.readAction
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.projectFixture
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@TestApplication
class DtsPropertyAssignableToConstTest {
  companion object {
    private val projectFixture = projectFixture(openAfterCreation = true)
  }

  private val dts by dtsFixture(projectFixture)

  private val myFixture get() = dts.codeInsight

  val string = "\"string\""
  val strings = "\"first\", \"second\""

  @Test
  fun `test int (1)`() = dtsTimeoutRunBlocking {
    doTest(
      type = DtsPropertyValue.Int(1),
      valid = listOf("<1>", "<1 MACRO>", "<MACRO 3>", "MACRO", "<MACRO>"),
      invalid = listOf("", "<>, <1>", "<>", "MACRO, <>"),
    )
  }

  @Test
  fun `test int (9)`() = dtsTimeoutRunBlocking {
    doTest(
      type = DtsPropertyValue.Int(9),
      valid = listOf("<9>", "<011>", "<0x9>", "<9U>", "<9UL>", "<9ULL>"),
      invalid = emptyList(),
    )
  }

  @Test
  fun `test ints (1 2 3)`() = dtsTimeoutRunBlocking {
    doTest(
      type = DtsPropertyValue.IntList(listOf(1, 2, 3)),
      valid = listOf("<1 2 3>", "<>, <1>, <2>, <3>", "<1 MACRO>", "<1>, MACRO") +
              listOf("[010203]", "[], [01], [02], [03]", "[01 MACRO]", "[01], MACRO"),
      invalid = listOf("<>, []", "<1>", "<1 2 3 4>", "<2 MACRO>") +
                listOf("", "[01]", "[01020304]", "[02 MACRO]"),
    )
  }

  @Test
  fun `test string (string)`() = dtsTimeoutRunBlocking {
    doTest(
      type = DtsPropertyValue.String("string"),
      valid = listOf(string, "MACRO"),
      invalid = listOf("", "\"other\"", "$string, $string", "$string, MACRO", "MACRO, MACRO"),
    )
  }

  @Test
  fun `test strings (first second)`() = dtsTimeoutRunBlocking {
    doTest(
      type = DtsPropertyValue.StringList(listOf("first", "second")),
      valid = listOf(strings, "MACRO"),
      invalid = listOf("", "\"first\"", "$strings, $string"),
    )
  }

  private suspend fun doTest(
    type: DtsPropertyValue,
    valid: List<String>,
    invalid: List<String>,
  ) {
    for (value in valid) {
      dts.configureByText("name = $value;")

      val property = readAction { (myFixture.file as DtsFile).dtsProperties.first() }
      assertTrue(property.dtsAssignableTo(type), "should be assignable: $value")
    }

    for (value in invalid) {
      dts.configureByText("name = $value;")

      val property = readAction { (myFixture.file as DtsFile).dtsProperties.first() }
      assertFalse(property.dtsAssignableTo(type), "should not be assignable: $value")
    }
  }
}