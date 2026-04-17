package com.intellij.dts.inspections

class DtsPropertyNameInspectionTest : DtsInspectionTest(DtsPropertyNameInspection::class) {
  override fun getBasePath(): String = "inspections/propertyName"

  fun `test invalid`() = dtsTimeoutRunBlocking { doInspectionTest() }
  fun `test valid`() = dtsTimeoutRunBlocking { doInspectionTest() }
}