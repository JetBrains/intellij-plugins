package com.intellij.dts.inspections

import com.intellij.dts.dtsTimeoutRunBlocking
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Test

@TestApplication
@TestDataPath($$"$PROJECT_ROOT/contrib/dts/tests/testData/inspections/undeclaredProperty")
class DtsUndeclaredPropertyInspectionTest : DtsInspectionTest(DtsUndeclaredPropertyInspection::class) {
  @Test
  fun `test child binding unresolved override`() = dtsTimeoutRunBlocking { doInspectionTest() }

  @Test
  fun `test default properties`() = dtsTimeoutRunBlocking { doInspectionTest() }

  override suspend fun doInspectionTest() {
    dts.addZephyr()
    super.doInspectionTest()
  }
}