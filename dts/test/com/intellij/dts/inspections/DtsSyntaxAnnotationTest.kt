package com.intellij.dts.inspections

class DtsSyntaxAnnotationTest : DtsInspectionTest() {
  override fun getBasePath(): String = "inspections/syntax"

  fun `test empty char`() = doInspectionTest()

  fun `test unterminated char`() = doInspectionTest()

  fun `test unterminated string`() = doInspectionTest()

  fun `test pHandle whitespace label`() = doInspectionTest()

  fun `test pHandle whitespace path`() = doInspectionTest()
}