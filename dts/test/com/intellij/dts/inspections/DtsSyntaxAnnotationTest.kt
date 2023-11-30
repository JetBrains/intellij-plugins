package com.intellij.dts.inspections

class DtsSyntaxAnnotationTest : DtsInspectionTest() {
  override fun getBasePath(): String = "inspections/syntax"

  fun `test empty char`() = doTest()

  fun `test unterminated char`() = doTest()

  fun `test unterminated string`() = doTest()

  fun `test pHandle whitespace label`() = doTest()

  fun `test pHandle whitespace path`() = doTest()
}