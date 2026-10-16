package com.intellij.dts.inspections

import com.intellij.dts.core.inspections.DtsPropertyEnumInspection
import com.intellij.dts.dtsTimeoutRunBlocking
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Test

@TestApplication
@TestDataPath($$"$PROJECT_ROOT/contrib/dts/tests/testData/inspections/propertyEnum")
class DtsPropertyEnumInspectionTest : DtsInspectionTest(DtsPropertyEnumInspection::class) {
  @Test
  fun `test valid`() = dtsTimeoutRunBlocking { doInspectionTest() }

  @Test
  fun `test invalid`() = dtsTimeoutRunBlocking { doInspectionTest() }

  override suspend fun doInspectionTest() {
    dts.addZephyr()
    super.doInspectionTest()
  }
}