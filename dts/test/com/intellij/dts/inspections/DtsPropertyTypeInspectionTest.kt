package com.intellij.dts.inspections

class DtsPropertyTypeInspectionTest : DtsInspectionTest(DtsPropertyTypeInspection::class) {
  override fun getBasePath(): String = "inspections/propertyType"

  override fun setUp() {
    super.setUp()
    addZephyr()
  }

  fun `test default property`() = doInspectionTest()

  fun `test binding property`() = doInspectionTest()
}