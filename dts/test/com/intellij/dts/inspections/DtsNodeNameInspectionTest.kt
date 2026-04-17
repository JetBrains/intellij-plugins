package com.intellij.dts.inspections

class DtsNodeNameInspectionTest : DtsInspectionTest(DtsNodeNameInspection::class) {
  override fun getBasePath(): String = "inspections/nodeName"

  fun `test invalid`() = dtsTimeoutRunBlocking { doInspectionTest() }

  fun `test multiple at`() = dtsTimeoutRunBlocking { doInspectionTest() }
}