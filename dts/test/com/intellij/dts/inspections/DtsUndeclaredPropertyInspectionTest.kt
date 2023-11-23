package com.intellij.dts.inspections

class DtsUndeclaredPropertyInspectionTest : DtsInspectionTest(DtsUndeclaredPropertyInspection::class) {
  override fun getBasePath(): String = "inspections/undeclaredProperty"

  override fun setUp() {
    super.setUp()
    addZephyr()
  }

  fun `test child binding unresolved override`() = doTest()

  fun `test default properties`() = doTest()
}