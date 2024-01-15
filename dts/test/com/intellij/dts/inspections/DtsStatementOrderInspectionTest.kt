package com.intellij.dts.inspections

class DtsStatementOrderInspectionTest : DtsInspectionTest(DtsStatementOrderInspection::class) {
  override fun getBasePath(): String = "inspections/statementOrder"

  fun `test invalid property`() = doInspectionTest()
  fun `test invalid delete property`() = doInspectionTest()
  fun `test after delete node`() = doInspectionTest()
}