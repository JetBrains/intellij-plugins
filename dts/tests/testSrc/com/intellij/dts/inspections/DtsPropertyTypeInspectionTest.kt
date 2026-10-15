package com.intellij.dts.inspections

class DtsPropertyTypeInspectionTest : DtsInspectionTest(DtsPropertyTypeInspection::class) {
  override fun getBasePath(): String = "inspections/propertyType"

  fun `test default property`() = dtsTimeoutRunBlocking { doInspectionTest() }

  fun `test binding property`() = dtsTimeoutRunBlocking { doInspectionTest() }

  override suspend fun doInspectionTest() {
    addZephyr()
    super.doInspectionTest()
  }
}