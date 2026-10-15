package com.intellij.dts.inspections

class DtsLabelNameInspectionTest : DtsInspectionTest(DtsLabelNameInspection::class) {
  override fun getBasePath(): String = "inspections/labelName"

  fun `test invalid start`() = dtsTimeoutRunBlocking { doInspectionTest() }
  fun `test invalid`() = dtsTimeoutRunBlocking { doInspectionTest() }
}