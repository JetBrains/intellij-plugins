package com.intellij.dts.lang

import com.intellij.dts.DtsTestBase

class DtsPropertyAssignableToConstTest : DtsTestBase() {
  val string = "\"string\""
  val strings = "\"first\", \"second\""

  fun `test int (1)`() = doTest(
    type = DtsPropertyValue.Int(1),
    valid = listOf("<1>", "<1 MACRO>", "<MACRO 3>", "MACRO", "<MACRO>"),
    invalid = listOf("", "<>, <1>", "<>", "MACRO, <>"),
  )

  fun `test int (9)`() = doTest(
    type = DtsPropertyValue.Int(9),
    valid = listOf("<9>", "<011>", "<0x9>", "<9U>", "<9UL>", "<9ULL>"),
    invalid = emptyList(),
  )

  fun `test ints (1 2 3)`() = doTest(
    type = DtsPropertyValue.IntList(listOf(1, 2, 3)),
    valid = listOf("<1 2 3>", "<>, <1>, <2>, <3>", "<1 MACRO>", "<1>, MACRO") +
            listOf("[010203]", "[], [01], [02], [03]", "[01 MACRO]", "[01], MACRO"),
    invalid = listOf("<>, []", "<1>", "<1 2 3 4>", "<2 MACRO>") +
              listOf("", "[01]", "[01020304]", "[02 MACRO]"),
  )

  fun `test string (string)`() = doTest(
    type = DtsPropertyValue.String("string"),
    valid = listOf(string, "MACRO"),
    invalid = listOf("", "\"other\"", "$string, $string", "$string, MACRO", "MACRO, MACRO"),
  )

  fun `test strings (first second)`() = doTest(
    type = DtsPropertyValue.StringList(listOf("first", "second")),
    valid = listOf(strings, "MACRO"),
    invalid = listOf("", "\"first\"", "$strings, $string"),
  )

  private fun doTest(
    type: DtsPropertyValue,
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