// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight

import com.intellij.polySymbols.testFramework.moveToOffsetBySignature
import com.intellij.testFramework.fixtures.CodeInsightTestUtil
import org.angular2.Angular2TestCase
import org.angular2.Angular2TestModule
import org.angular2.TestNoService
import org.angular2.TestTsGoFork
import org.junit.Test

@TestNoService
@TestTsGoFork
class Angular2EditorTest : Angular2TestCase("editor") {

  @Test
  fun testBlockTyping() =
    doConfiguredTest(Angular2TestModule.ANGULAR_CORE_17_3_0,
                     fileContents = "<div></div><!--within tag-->\n <!--space-->\ntext", extension = "html", checkResult = true) {
      moveToOffsetBySignature("<div><caret></div>")
      type("@if (")
      moveToOffsetBySignature("()<caret>")
      type("{\n<div>")
      moveToOffsetBySignature("<caret> <!--space-->")
      type("@if (")
      moveToOffsetBySignature("<caret>text")
      type("@if (")
    }

  @Test
  fun testBlockFolding() {
    doFoldingTest(Angular2TestModule.ANGULAR_CORE_17_3_0,
                  extension = "html")
  }

  @Test
  fun testClosingBraceWithInterpolation() =
    doConfiguredTest(Angular2TestModule.ANGULAR_CORE_17_3_0,
                     fileContents = "@if (foo) { {{foo}} } @else { <caret>", extension = "html", checkResult = true) {
      type("}")
    }

  @Test
  fun testDeletingParenInParams() =
    doConfiguredTest(Angular2TestModule.ANGULAR_CORE_17_3_0,
                     fileContents = "@if (((<caret>))) { }", extension = "html", checkResult = true) {
      type("\b\b\b")
    }

  @Test
  fun testIfBlockExtendSelection() =
    doWordSelectionTest()

  @Test
  fun testIfElseBlockExtendSelection() =
    doWordSelectionTest()

  @Test
  fun testSwitchBlockExtendSelection() =
    doWordSelectionTest()

  @Test
  fun testTopLevelBlockEmmetExpansion() =
    doEditorTypingTest(checkResult = true, extension = "html") {
      type("\t")
    }

  @Test
  fun testIncompleteStringInInterpolation() =
    doConfiguredTest(Angular2TestModule.ANGULAR_CORE_17_3_0,
                     fileContents = "<main>{{ <caret> }}</main>", extension = "html", checkResult = true) {
      type("'sdf'")
    }

  private fun doWordSelectionTest() =
    doConfiguredTest(Angular2TestModule.ANGULAR_CORE_17_3_0, configureFile = false) {
      CodeInsightTestUtil.doWordSelectionTestOnDirectory(myFixture, getTestName(true), "html")
    }

}