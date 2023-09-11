package com.intellij.dts.inspections

class DtsPHandleWhitespaceInspectionTest : DtsInspectionTest(DtsPHandleWhitespaceInspection::class) {
    override fun getBasePath(): String = "inspections/pHandleWhitespace"

    fun `test valid`() = doTest()
    fun `test invalid label`() = doTest()
    fun `test invalid path`() = doTest()
}