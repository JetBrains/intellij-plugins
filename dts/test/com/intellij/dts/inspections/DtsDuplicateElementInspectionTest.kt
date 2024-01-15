package com.intellij.dts.inspections

class DtsDuplicateElementInspectionTest : DtsInspectionTest(DtsDuplicateElementInspection::class) {
  override fun getBasePath(): String = "inspections/duplicateElement"

  fun `test property`() = doInspectionTest()

  fun `test node`() = doInspectionTest()

  fun `test mixed`() = doInspectionTest()

  fun `test valid`() = doInspectionTest()
}