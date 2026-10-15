package com.intellij.dts.inspections

class DtsUndeclaredPropertyInspectionTest : DtsInspectionTest(DtsUndeclaredPropertyInspection::class) {
  override fun getBasePath(): String = "inspections/undeclaredProperty"

  fun `test child binding unresolved override`() = dtsTimeoutRunBlocking { doInspectionTest() }

  fun `test default properties`() = dtsTimeoutRunBlocking { doInspectionTest() }

  override suspend fun doInspectionTest() {
    addZephyr()
    super.doInspectionTest()
  }
}