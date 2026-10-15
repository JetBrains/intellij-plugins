package com.intellij.dts.inspections

class DtsStatementOrderInspectionTest : DtsInspectionTest(DtsStatementOrderInspection::class) {
  override fun getBasePath(): String = "inspections/statementOrder"

  fun `test invalid property`() = dtsTimeoutRunBlocking { doInspectionTest() }
  fun `test invalid delete property`() = dtsTimeoutRunBlocking { doInspectionTest() }
  fun `test after delete node`() = dtsTimeoutRunBlocking { doInspectionTest() }
}