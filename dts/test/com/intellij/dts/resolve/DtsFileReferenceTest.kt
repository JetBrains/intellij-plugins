package com.intellij.dts.resolve

import com.intellij.dts.DtsTestBase
import com.intellij.openapi.application.readAction

class DtsFileReferenceTest : DtsTestBase() {
  fun `test relative dts include`() = dtsTimeoutRunBlocking {
    doTest(
      input = "/include/ \"test.dtsi<caret>\"",
      filePath = "test.dtsi",
    )
  }

  fun `test relative c include`() = dtsTimeoutRunBlocking {
    doTest(
      input = "#include \"test.dtsi<caret>\"",
      filePath = "test.dtsi",
    )
  }

  fun `test nested relative include`() = dtsTimeoutRunBlocking {
    doTest(
      input = "/include/ \"path/to/file/test.dtsi<caret>\"",
      filePath = "path/to/file/test.dtsi",
    )
  }

  private suspend fun doTest(
    input: String,
    filePath: String,
  ) {
    val includedFile = addFile(filePath, "")
    val file = configureByText(input)

    val reference = myFixture.getReferenceAtCaretPositionWithAssertion(file.name)
    assertEquals(includedFile, readAction { reference.resolve() })
  }
}