// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.inspections

import org.angular2.Angular2TestCase
import org.angular2.Angular2TestModule
import org.angular2.inspections.AngularIncorrectBlockUsageInspection

class Angular2BlockInspectionsTest : Angular2TestCase("inspections/blocks") {

  fun testUnknown() = doHighlightingTest()

  fun testMisplaced() = doHighlightingTest()

  fun testMultipleElse() = doHighlightingTest()

  fun testElseNotLast() = doHighlightingTest()

  fun testElseWithParameters() = doHighlightingTest()

  fun testIfParameters() = doHighlightingTest()

  fun testElseIfParameters() = doHighlightingTest()

  fun testMultipleEmpty() = doHighlightingTest()

  fun testEmptyWithParameters() = doHighlightingTest()

  fun testMultipleError() = doHighlightingTest()

  fun testErrorWithParameters() = doHighlightingTest()

  fun testMultiplePlaceholder() = doHighlightingTest()

  fun testMultipleLoading() = doHighlightingTest()

  fun testMultipleDefault() = doHighlightingTest()

  fun testDefaultWithParameters() = doHighlightingTest()

  override fun setUp() {
    super.setUp()
    myFixture.enableInspections(AngularIncorrectBlockUsageInspection())
  }

  private fun doHighlightingTest() {
    doConfiguredTest(Angular2TestModule.ANGULAR_CORE_17_0_0_RC_0, Angular2TestModule.ANGULAR_COMMON_17_0_0_RC_0,
                     extension = "html", checkResult = false) {
      myFixture.checkHighlighting()
    }
  }

}