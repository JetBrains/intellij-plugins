package com.intellij.dts.resolve

import com.intellij.dts.DtsTestBase

class DtsFileReferenceTest : DtsTestBase() {
    fun `test relative dts include`() = doTest(
        "/include/ \"test.dtsi<caret>\"",
        "test.dtsi",
    )

    fun `test relative c include`() = doTest(
        "#include \"test.dtsi<caret>\"",
        "test.dtsi",
    )

    fun `test nested relative include`() = doTest(
        "/include/ \"path/to/file/test.dtsi<caret>\"",
        "path/to/file/test.dtsi",
    )

    fun doTest(
        input: String,
        filePath: String,
    ) {
        val includedFile = addFile(filePath, "")
        val fileName = configureByText(input)

        val reference = myFixture.getReferenceAtCaretPositionWithAssertion(fileName)
        assertEquals(includedFile, reference.resolve())
    }
}