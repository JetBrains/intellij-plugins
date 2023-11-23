package com.intellij.dts.zephyr

import com.intellij.dts.DtsTestBase
import com.intellij.dts.lang.DtsFile
import com.intellij.dts.lang.DtsPropertyType
import com.intellij.dts.lang.dtsAssignableTo

class DtsPropertyAssignableTest : DtsTestBase() {
  val string = "\"string\""

  fun `test int with macros`() = doTest(
    DtsPropertyType.Int,
    valid = listOf("<MACRO>", "<MACRO 1>", "<MACRO MACRO>", "MACRO"),
    invalid = listOf("MACRO, MACRO", "<1>, MACRO", "<1 MACRO 2>")
  )

  fun `test int`() = doTest(
    DtsPropertyType.Int,
    valid = listOf("<3>", "<(1 + 1)>"),
    invalid = listOf("<>", "<1 2>", "<1>, <2>", string, "[]")
  )

  fun `test ints`() = doTest(
    DtsPropertyType.Ints,
    valid = listOf("<>", "<1>", "<1 2>", "<1 (1 + 1)>", "<1>, <2>"),
    invalid = listOf("\"string\"", "[]")
  )

  fun `test ints with macros`() = doTest(
    DtsPropertyType.Ints,
    valid = listOf("<MACRO>", "<MACRO 1>", "<1 MACRO 2>", "MACRO"),
    invalid = emptyList()
  )

  fun `test string`() = doTest(
    DtsPropertyType.String,
    valid = listOf(string),
    invalid = listOf("[]", "<>", "$string, $string")
  )

  fun `test string list`() = doTest(
    DtsPropertyType.StringList,
    valid = listOf(string, "$string, $string"),
    invalid = listOf("[]", "<>")
  )

  fun `test bytes`() = doTest(
    DtsPropertyType.Bytes,
    valid = listOf("[]", "[ab cd]", "['a']"),
    invalid = listOf("<>", string)
  )

  fun `test phandls`() = doTest(
    DtsPropertyType.PHandles,
    valid = listOf("<>", "<&handle>", "<&handle &handle>", "<&handle>, <&handle>"),
    invalid = listOf("[]", string)
  )

  private fun doTest(
    type: DtsPropertyType,
    valid: List<String>,
    invalid: List<String>,
  ) {
    for (value in valid) {
      configureByText("name = $value;")

      val property = (myFixture.file as DtsFile).dtsProperties.first()
      assertTrue("should be assignable: $value", property.dtsAssignableTo(type))
    }

    for (value in invalid) {
      configureByText("name = $value;")

      val property = (myFixture.file as DtsFile).dtsProperties.first()
      assertFalse("should not be assignable: $value", property.dtsAssignableTo(type))
    }
  }
}