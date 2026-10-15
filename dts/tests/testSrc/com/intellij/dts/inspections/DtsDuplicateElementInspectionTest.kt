package com.intellij.dts.inspections

class DtsDuplicateElementInspectionTest : DtsInspectionTest(DtsDuplicateElementInspection::class) {
  override fun getBasePath(): String = "inspections/duplicateElement"

  fun `test property`() = dtsTimeoutRunBlocking { doInspectionTest() }

  fun `test node`() = dtsTimeoutRunBlocking { doInspectionTest() }

  fun `test mixed`() = dtsTimeoutRunBlocking { doInspectionTest() }

  fun `test valid`() = dtsTimeoutRunBlocking { doInspectionTest() }
}