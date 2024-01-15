package com.intellij.dts.inspections

class DtsPropertyNameInspectionTest : DtsInspectionTest(DtsPropertyNameInspection::class) {
  override fun getBasePath(): String = "inspections/propertyName"

  fun `test invalid`() = doInspectionTest()
  fun `test valid`() = doInspectionTest()
}