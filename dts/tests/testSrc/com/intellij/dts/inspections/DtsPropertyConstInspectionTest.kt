package com.intellij.dts.inspections

class DtsPropertyConstInspectionTest : DtsInspectionTest(DtsPropertyConstInspection::class) {
  override fun getBasePath(): String = "inspections/propertyConst"

  fun `test valid`() = dtsTimeoutRunBlocking { doInspectionTest() }
  fun `test invalid`() = dtsTimeoutRunBlocking { doInspectionTest() }

  override suspend fun doInspectionTest() {
    addZephyr()
    super.doInspectionTest()
  }
}