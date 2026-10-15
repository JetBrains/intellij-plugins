package com.intellij.dts.inspections

import com.intellij.dts.core.inspections.DtsUnitNameInspection
import com.intellij.dts.dtsTimeoutRunBlocking
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Test

@TestApplication
@TestDataPath($$"$PROJECT_ROOT/contrib/dts/tests/testData/inspections/unitName")
class DtsUnitNameInspectionTest : DtsInspectionTest(DtsUnitNameInspection::class) {
  @Test
  fun `test leading 0s`() = dtsTimeoutRunBlocking { doInspectionTest() }
  @Test
  fun `test leading 0x`() = dtsTimeoutRunBlocking { doInspectionTest() }
}