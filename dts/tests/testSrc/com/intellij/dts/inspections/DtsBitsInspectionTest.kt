package com.intellij.dts.inspections

class DtsBitsInspectionTest : DtsInspectionTest(DtsBitsInspection::class) {
  override fun getBasePath(): String = "inspections/bits"

  fun `test valid`() = dtsTimeoutRunBlocking { doInspectionTest() }
  fun `test invalid`() = dtsTimeoutRunBlocking { doInspectionTest() }
}