package com.intellij.dts.resolve

import com.intellij.dts.DtsTestBase
import com.intellij.openapi.application.runReadActionBlocking

class DtsFileReferenceTest : DtsTestBase() {
  fun `test relative dts include`() = doTest(
    input = "/include/ \"test.dtsi<caret>\"",
    filePath = "test.dtsi",
  )

  fun `test relative c include`() = doTest(
    input = "#include \"test.dtsi<caret>\"",
    filePath = "test.dtsi",
  )

  fun `test nested relative include`() = doTest(
    input = "/include/ \"path/to/file/test.dtsi<caret>\"",
    filePath = "path/to/file/test.dtsi",
  )

  fun doTest(
    input: String,
    filePath: String,
  ) {
    val includedFile = addFile(filePath, "")
    val file = configureByText(input)

    val reference = myFixture.getReferenceAtCaretPositionWithAssertion(file.name)
    assertEquals(includedFile, runReadActionBlocking { reference.resolve() })
  }
}