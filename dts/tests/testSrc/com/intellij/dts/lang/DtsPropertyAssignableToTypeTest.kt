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
class DtsPropertyAssignableToTypeTest {
  companion object {
    private val projectFixture = projectFixture(openAfterCreation = true)
  }

  private val dts by dtsFixture(projectFixture)

  private val myFixture get() = dts.codeInsight

  val string = "\"string\""

  @Test
  fun `test int`() = dtsTimeoutRunBlocking {
    doTest(
      DtsPropertyType.Int,
      valid = listOf("<3>", "<(1 + 1)>"),
      invalid = listOf("", "<>", "<1 2>", "<1>, <2>", string, "[]")
    )
  }

  @Test
  fun `test int with macros`() = dtsTimeoutRunBlocking {
    doTest(
      DtsPropertyType.Int,
      valid = listOf("<MACRO>", "MACRO", "<MACRO 1>", "<MACRO MACRO>"),
      invalid = listOf("MACRO, MACRO", "<1>, MACRO", "<1 MACRO 2>")
    )
  }


  @Test
  fun `test ints`() = dtsTimeoutRunBlocking {
    doTest(
      DtsPropertyType.Ints,
      valid = listOf("", "<>", "<1>", "<1 2>", "<1 (1 + 1)>", "<1>, <2>"),
      invalid = listOf("\"string\"", "[]")
    )
  }

  @Test
  fun `test ints with macros`() = dtsTimeoutRunBlocking {
    doTest(
      DtsPropertyType.Ints,
      valid = listOf("<MACRO>", "<MACRO 1>", "<1 MACRO 2>", "MACRO"),
      invalid = emptyList()
    )
  }

  @Test
  fun `test string`() = dtsTimeoutRunBlocking {
    doTest(
      DtsPropertyType.String,
      valid = listOf(string),
      invalid = listOf("", "[]", "<>", "$string, $string")
    )
  }

  @Test
  fun `test string list`() = dtsTimeoutRunBlocking {
    doTest(
      DtsPropertyType.StringList,
      valid = listOf("", string, "$string, $string"),
      invalid = listOf("[]", "<>")
    )
  }

  @Test
  fun `test bytes`() = dtsTimeoutRunBlocking {
    doTest(
      DtsPropertyType.Bytes,
      valid = listOf("", "[]", "[ab cd]", "['a']", "[ab], [cd]"),
      invalid = listOf("<>", string)
    )
  }

  @Test
  fun `test phandls`() = dtsTimeoutRunBlocking {
    doTest(
      DtsPropertyType.PHandles,
      valid = listOf("", "<>", "<&handle>", "<&handle &handle>", "<&handle>, <&handle>"),
      invalid = listOf("[]", string)
    )
  }

  private suspend fun doTest(
    type: DtsPropertyType,
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