package com.intellij.dts.inspections

import com.intellij.dts.dtsTimeoutRunBlocking
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Test

@TestApplication
@TestDataPath($$"$PROJECT_ROOT/contrib/dts/tests/testData/inspections/requiredProperty")
class DtsRequiredPropertyInspectionTest : DtsInspectionTest(DtsRequiredPropertyInspection::class, extension = "dts") {
  @Test
  fun `test node`() = dtsTimeoutRunBlocking { doInspectionTest() }

  @Test
  fun `test ref node`() = dtsTimeoutRunBlocking { doInspectionTest() }

  @Test
  fun `test delete property`() = dtsTimeoutRunBlocking { doInspectionTest() }

  @Test
  fun `test delete node`() = dtsTimeoutRunBlocking { doInspectionTest() }

  @Test
  fun `test delete node ref`() = dtsTimeoutRunBlocking { doInspectionTest() }

  @Test
  fun `test delete node root`() = dtsTimeoutRunBlocking { doInspectionTest() }

  override suspend fun doInspectionTest() {
    dts.addZephyr()

    val declaration = dts.getFixture("inspections/requiredProperty/Declaration.dtsi")
    dts.addFile("Declaration.dtsi", declaration)

    super.doInspectionTest()
  }
}