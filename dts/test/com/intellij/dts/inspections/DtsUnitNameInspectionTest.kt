package com.intellij.dts.inspections

class DtsUnitNameInspectionTest : DtsInspectionTest(DtsUnitNameInspection::class) {
  override fun getBasePath(): String = "inspections/unitName"

  fun `test leading 0s`() = doInspectionTest()
  fun `test leading 0x`() = doInspectionTest()
}