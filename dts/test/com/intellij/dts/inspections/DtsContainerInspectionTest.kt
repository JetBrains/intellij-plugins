package com.intellij.dts.inspections

class DtsContainerInspectionTest : DtsInspectionTest(DtsContainerInspection::class) {
  override fun getBasePath(): String = "inspections/container"

  override fun getTestFileExtension(): String = "dts"

  fun `test root property`() = doInspectionTest()
  fun `test root sub node`() = doInspectionTest()
  fun `test root delete property`() = doInspectionTest()
  fun `test root delete node by name`() = doInspectionTest()
  fun `test root delete node by ref`() = doInspectionTest()
  fun `test node root node`() = doInspectionTest()
  fun `test node v1`() = doInspectionTest()
  fun `test node delete node by name`() = doInspectionTest()
  fun `test node delete node by ref`() = doInspectionTest()
}