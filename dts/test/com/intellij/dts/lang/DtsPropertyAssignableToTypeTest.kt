package com.intellij.dts.lang

import com.intellij.dts.DtsTestBase
import com.intellij.openapi.application.readAction

class DtsPropertyAssignableToTypeTest : DtsTestBase() {
  val string = "\"string\""

  fun `test int`() = dtsTimeoutRunBlocking {
    doTest(
      DtsPropertyType.Int,
      valid = listOf("<3>", "<(1 + 1)>"),
      invalid = listOf("", "<>", "<1 2>", "<1>, <2>", string, "[]")
    )
  }

  fun `test int with macros`() = dtsTimeoutRunBlocking {
    doTest(
      DtsPropertyType.Int,
      valid = listOf("<MACRO>", "MACRO", "<MACRO 1>", "<MACRO MACRO>"),
      invalid = listOf("MACRO, MACRO", "<1>, MACRO", "<1 MACRO 2>")
    )
  }


  fun `test ints`() = dtsTimeoutRunBlocking {
    doTest(
      DtsPropertyType.Ints,
      valid = listOf("", "<>", "<1>", "<1 2>", "<1 (1 + 1)>", "<1>, <2>"),
      invalid = listOf("\"string\"", "[]")
    )
  }

  fun `test ints with macros`() = dtsTimeoutRunBlocking {
    doTest(
      DtsPropertyType.Ints,
      valid = listOf("<MACRO>", "<MACRO 1>", "<1 MACRO 2>", "MACRO"),
      invalid = emptyList()
    )
  }

  fun `test string`() = dtsTimeoutRunBlocking {
    doTest(
      DtsPropertyType.String,
      valid = listOf(string),
      invalid = listOf("", "[]", "<>", "$string, $string")
    )
  }

  fun `test string list`() = dtsTimeoutRunBlocking {
    doTest(
      DtsPropertyType.StringList,
      valid = listOf("", string, "$string, $string"),
      invalid = listOf("[]", "<>")
    )
  }

  fun `test bytes`() = dtsTimeoutRunBlocking {
    doTest(
      DtsPropertyType.Bytes,
      valid = listOf("", "[]", "[ab cd]", "['a']", "[ab], [cd]"),
      invalid = listOf("<>", string)
    )
  }

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
      configureByText("name = $value;")

      val property = readAction { (myFixture.file as DtsFile).dtsProperties.first() }
      assertTrue("should be assignable: $value", property.dtsAssignableTo(type))
    }

    for (value in invalid) {
      configureByText("name = $value;")

      val property = readAction { (myFixture.file as DtsFile).dtsProperties.first() }
      assertFalse("should not be assignable: $value", property.dtsAssignableTo(type))
    }
  }
}