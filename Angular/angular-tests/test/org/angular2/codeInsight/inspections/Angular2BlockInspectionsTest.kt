// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.inspections

import org.angular2.Angular2TestCase
import org.angular2.Angular2TestModule
import org.angular2.TestNoService
import org.angular2.TestTsGoFork
import org.angular2.inspections.AngularIncorrectBlockUsageInspection
import org.junit.Test

@TestNoService
@TestTsGoFork
class Angular2BlockInspectionsTest : Angular2TestCase("inspections/blocks") {

  @Test
  fun testUnknown() = doHighlightingTest()

  @Test
  fun testMisplaced() = doHighlightingTest()

  @Test
  fun testMultipleElse() = doHighlightingTest()

  @Test
  fun testElseNotLast() = doHighlightingTest()

  @Test
  fun testElseWithParameters() = doHighlightingTest()

  @Test
  fun testIfParameters() = doHighlightingTest()

  @Test
  fun testElseIfParameters() = doHighlightingTest()

  @Test
  fun testMultipleEmpty() = doHighlightingTest()

  @Test
  fun testEmptyWithParameters() = doHighlightingTest()

  @Test
  fun testMultipleError() = doHighlightingTest()

  @Test
  fun testErrorWithParameters() = doHighlightingTest()

  @Test
  fun testMultiplePlaceholder() = doHighlightingTest()

  @Test
  fun testMultipleLoading() = doHighlightingTest()

  @Test
  fun testMultipleDefault() = doHighlightingTest()

  @Test
  fun testDefaultWithParameters() = doHighlightingTest()

  override fun setUp() {
    super.setUp()
    myFixture.enableInspections(AngularIncorrectBlockUsageInspection())
  }

  private fun doHighlightingTest() {
    doConfiguredTest(Angular2TestModule.ANGULAR_CORE_17_3_0, Angular2TestModule.ANGULAR_COMMON_17_3_0,
                     extension = "html", checkResult = false) {
      myFixture.checkHighlighting()
    }
  }

}