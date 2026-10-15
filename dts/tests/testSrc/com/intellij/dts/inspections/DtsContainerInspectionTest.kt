package com.intellij.dts.inspections

class DtsContainerInspectionTest : DtsInspectionTest(DtsContainerInspection::class) {
  override fun getBasePath(): String = "inspections/container"

  override fun getTestFileExtension(): String = "dts"

  fun `test root property`() = dtsTimeoutRunBlocking { doInspectionTest() }
  fun `test root sub node`() = dtsTimeoutRunBlocking { doInspectionTest() }
  fun `test root delete property`() = dtsTimeoutRunBlocking { doInspectionTest() }
  fun `test root delete node by name`() = dtsTimeoutRunBlocking { doInspectionTest() }
  fun `test root delete node by ref`() = dtsTimeoutRunBlocking { doInspectionTest() }
  fun `test node root node`() = dtsTimeoutRunBlocking { doInspectionTest() }
  fun `test node v1`() = dtsTimeoutRunBlocking { doInspectionTest() }
  fun `test node delete node by name`() = dtsTimeoutRunBlocking { doInspectionTest() }
  fun `test node delete node by ref`() = dtsTimeoutRunBlocking { doInspectionTest() }
}