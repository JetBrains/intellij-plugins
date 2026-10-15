package com.intellij.dts.resolve

import com.intellij.dts.dtsFixture
import com.intellij.dts.dtsTimeoutRunBlocking
import com.intellij.openapi.application.readAction
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.projectFixture
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@TestApplication
class DtsFileReferenceTest {
  companion object {
    private val projectFixture = projectFixture(openAfterCreation = true)
  }

  private val dts by dtsFixture(projectFixture)

  private val myFixture get() = dts.codeInsight

  @Test
  fun `test relative dts include`() = dtsTimeoutRunBlocking {
    doTest(
      input = "/include/ \"test.dtsi<caret>\"",
      filePath = "test.dtsi",
    )
  }

  @Test
  fun `test relative c include`() = dtsTimeoutRunBlocking {
    doTest(
      input = "#include \"test.dtsi<caret>\"",
      filePath = "test.dtsi",
    )
  }

  @Test
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
    val includedFile = dts.addFile(filePath, "")
    val file = dts.configureByText(input)

    val reference = myFixture.getReferenceAtCaretPositionWithAssertion(file.name)
    assertEquals(includedFile, readAction { reference.resolve() })
  }
}