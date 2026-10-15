package com.intellij.dts.inspections.fixes

import com.intellij.dts.inspections.DtsInspectionTest
import com.intellij.dts.inspections.DtsRequiredPropertyInspection

class DtsCreatePropertyFixTest : DtsInspectionTest(DtsRequiredPropertyInspection::class) {
  override fun getTestFileExtension(): String = "dts"

  override fun getBasePath(): String = "inspections/fixes/createProperty"

  fun `test in empty node`() = dtsTimeoutRunBlocking { doTest() }

  fun `test after property`() = dtsTimeoutRunBlocking { doTest() }

  fun `test after property with comment`() = dtsTimeoutRunBlocking { doTest() }

  fun `test before node`() = dtsTimeoutRunBlocking { doTest() }

  fun `test between property and node`() = dtsTimeoutRunBlocking { doTest() }

  fun `test multiple`() = dtsTimeoutRunBlocking { doTest() }

  private suspend fun doTest() {
    addZephyr()
    doQuickfixTest("Create property")
  }
}