package com.intellij.dts.inspections

class DtsDuplicateElementInspectionTest : DtsInspectionTest(DtsDuplicateElementInspection::class) {
  override fun getBasePath(): String = "inspections/duplicateElement"

  fun `test property`() = doTest()

  fun `test node`() = doTest()

  fun `test mixed`() = doTest()

  fun `test valid`() = doTest()
}