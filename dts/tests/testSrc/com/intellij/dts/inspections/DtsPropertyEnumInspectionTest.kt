package com.intellij.dts.inspections

class DtsPropertyEnumInspectionTest : DtsInspectionTest(DtsPropertyEnumInspection::class) {
  override fun getBasePath(): String = "inspections/propertyEnum"

  fun `test valid`() = dtsTimeoutRunBlocking { doInspectionTest() }
  fun `test invalid`() = dtsTimeoutRunBlocking { doInspectionTest() }

  override suspend fun doInspectionTest() {
    addZephyr()
    super.doInspectionTest()
  }
}