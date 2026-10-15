package com.intellij.dts.inspections.fixes

import com.intellij.dts.dtsTimeoutRunBlocking
import com.intellij.dts.inspections.DtsInspectionTest
import com.intellij.dts.inspections.DtsRequiredPropertyInspection
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Test

@TestApplication
@TestDataPath($$"$PROJECT_ROOT/contrib/dts/tests/testData/inspections/fixes/createProperty")
class DtsCreatePropertyFixTest : DtsInspectionTest(DtsRequiredPropertyInspection::class, extension = "dts") {
  @Test
  fun `test in empty node`() = dtsTimeoutRunBlocking { doTest() }

  @Test
  fun `test after property`() = dtsTimeoutRunBlocking { doTest() }

  @Test
  fun `test after property with comment`() = dtsTimeoutRunBlocking { doTest() }

  @Test
  fun `test before node`() = dtsTimeoutRunBlocking { doTest() }

  @Test
  fun `test between property and node`() = dtsTimeoutRunBlocking { doTest() }

  @Test
  fun `test multiple`() = dtsTimeoutRunBlocking { doTest() }

  private suspend fun doTest() {
    dts.addZephyr()
    doQuickfixTest("Create property")
  }
}