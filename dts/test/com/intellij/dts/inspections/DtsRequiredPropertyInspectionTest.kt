package com.intellij.dts.inspections

class DtsRequiredPropertyInspectionTest : DtsInspectionTest(DtsRequiredPropertyInspection::class) {
  override fun getBasePath(): String = "inspections/requiredProperty"

  override fun getTestFileExtension(): String = "dts"

  override fun setUp() {
    super.setUp()
    addZephyr()

    val declaration = getFixture("inspections/requiredProperty/Declaration.dtsi")
    addFile("Declaration.dtsi", declaration)
  }

  fun `test node`() = doTest()

  fun `test ref node`() = doTest()

  fun `test delete property`() = doTest()

  fun `test delete node`() = doTest()

  fun `test delete node ref`() = doTest()

  fun `test delete node root`() = doTest()
}