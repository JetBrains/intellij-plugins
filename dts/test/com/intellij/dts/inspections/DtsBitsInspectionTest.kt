package com.intellij.dts.inspections

class DtsBitsInspectionTest : DtsInspectionTest(DtsBitsInspection::class) {
  override fun getBasePath(): String = "inspections/bits"

  fun `test valid`() = doInspectionTest()
  fun `test invalid`() = doInspectionTest()
}