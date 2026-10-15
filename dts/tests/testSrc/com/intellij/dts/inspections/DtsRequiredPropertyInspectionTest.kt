package com.intellij.dts.inspections

class DtsRequiredPropertyInspectionTest : DtsInspectionTest(DtsRequiredPropertyInspection::class) {
  override fun getBasePath(): String = "inspections/requiredProperty"

  override fun getTestFileExtension(): String = "dts"

  fun `test node`() = dtsTimeoutRunBlocking { doInspectionTest() }

  fun `test ref node`() = dtsTimeoutRunBlocking { doInspectionTest() }

  fun `test delete property`() = dtsTimeoutRunBlocking { doInspectionTest() }

  fun `test delete node`() = dtsTimeoutRunBlocking { doInspectionTest() }

  fun `test delete node ref`() = dtsTimeoutRunBlocking { doInspectionTest() }

  fun `test delete node root`() = dtsTimeoutRunBlocking { doInspectionTest() }

  override suspend fun doInspectionTest() {
    addZephyr()

    val declaration = getFixture("inspections/requiredProperty/Declaration.dtsi")
    addFile("Declaration.dtsi", declaration)

    super.doInspectionTest()
  }
}