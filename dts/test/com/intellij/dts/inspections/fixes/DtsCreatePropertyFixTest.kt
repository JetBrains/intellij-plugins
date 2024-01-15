package com.intellij.dts.inspections.fixes

import com.intellij.dts.inspections.DtsInspectionTest
import com.intellij.dts.inspections.DtsRequiredPropertyInspection

class DtsCreatePropertyFixTest : DtsInspectionTest(DtsRequiredPropertyInspection::class) {
  override fun getTestFileExtension(): String = "dts"

  override fun getBasePath(): String = "inspections/fixes/createProperty"

  override fun setUp() {
    super.setUp()
    addZephyr()
  }

  fun `test in empty node`() = doTest()

  fun `test after property`() = doTest()

  fun `test after property with comment`() = doTest()

  fun `test before node`() = doTest()

  fun `test between property and node`() = doTest()

  fun `test multiple`() = doTest()

  private fun doTest() = doQuickfixTest("Create property")
}