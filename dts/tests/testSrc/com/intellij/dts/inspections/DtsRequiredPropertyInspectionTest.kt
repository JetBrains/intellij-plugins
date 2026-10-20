package com.intellij.dts.inspections

import com.intellij.dts.core.inspections.DtsRequiredPropertyInspection
import com.intellij.dts.dtsTimeoutRunBlocking
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Test

@TestApplication
@TestDataPath($$"$PROJECT_ROOT/contrib/dts/tests/testData/inspections/requiredProperty")
class DtsRequiredPropertyInspectionTest : DtsInspectionTest(DtsRequiredPropertyInspection::class, needsZephyr = true, extension = "dts") {
  @Test
  fun `test node`() = dtsTimeoutRunBlocking { doTest() }

  @Test
  fun `test ref node`() = dtsTimeoutRunBlocking { doTest() }

  @Test
  fun `test delete property`() = dtsTimeoutRunBlocking { doTest() }

  @Test
  fun `test delete node`() = dtsTimeoutRunBlocking { doTest() }

  @Test
  fun `test delete node ref`() = dtsTimeoutRunBlocking { doTest() }

  @Test
  fun `test delete node root`() = dtsTimeoutRunBlocking { doTest() }

  private suspend fun doTest() {
    val declaration = dts.getFixture("inspections/requiredProperty/Declaration.dtsi")
    dts.addFile("Declaration.dtsi", declaration)

    doInspectionTest()
  }
}