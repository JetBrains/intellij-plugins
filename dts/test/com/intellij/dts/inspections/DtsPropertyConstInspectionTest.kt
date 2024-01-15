package com.intellij.dts.inspections

class DtsPropertyConstInspectionTest : DtsInspectionTest(DtsPropertyConstInspection::class) {
  override fun getBasePath(): String = "inspections/propertyConst"

  override fun setUp() {
    super.setUp()
    addZephyr()
  }

  fun `test valid`() = doInspectionTest()
  fun `test invalid`() = doInspectionTest()
}