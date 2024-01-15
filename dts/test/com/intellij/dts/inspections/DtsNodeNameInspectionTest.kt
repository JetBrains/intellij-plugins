package com.intellij.dts.inspections

class DtsNodeNameInspectionTest : DtsInspectionTest(DtsNodeNameInspection::class) {
  override fun getBasePath(): String = "inspections/nodeName"

  fun `test invalid`() = doInspectionTest()

  fun `test multiple at`() = doInspectionTest()
}