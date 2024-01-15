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

  fun `test node`() = doInspectionTest()

  fun `test ref node`() = doInspectionTest()

  fun `test delete property`() = doInspectionTest()

  fun `test delete node`() = doInspectionTest()

  fun `test delete node ref`() = doInspectionTest()

  fun `test delete node root`() = doInspectionTest()
}