package com.intellij.dts.inspections

class DtsSyntaxAnnotationTest : DtsInspectionTest() {
  override fun getBasePath(): String = "inspections/syntax"

  fun `test empty char`() = dtsTimeoutRunBlocking { doInspectionTest() }

  fun `test unterminated char`() = dtsTimeoutRunBlocking { doInspectionTest() }

  fun `test unterminated string`() = dtsTimeoutRunBlocking { doInspectionTest() }

  fun `test pHandle whitespace label`() = dtsTimeoutRunBlocking { doInspectionTest() }

  fun `test pHandle whitespace path`() = dtsTimeoutRunBlocking { doInspectionTest() }
}