package com.intellij.dts.inspections

import com.intellij.dts.core.inspections.DtsPropertyTypeInspection
import com.intellij.dts.dtsTimeoutRunBlocking
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Test

@TestApplication
@TestDataPath($$"$PROJECT_ROOT/contrib/dts/tests/testData/inspections/propertyType")
class DtsPropertyTypeInspectionTest : DtsInspectionTest(DtsPropertyTypeInspection::class) {
  @Test
  fun `test default property`() = dtsTimeoutRunBlocking { doInspectionTest() }

  @Test
  fun `test binding property`() = dtsTimeoutRunBlocking { doInspectionTest() }

  override suspend fun doInspectionTest() {
    dts.addZephyr()
    super.doInspectionTest()
  }
}