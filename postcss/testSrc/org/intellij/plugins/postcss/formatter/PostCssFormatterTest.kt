package org.intellij.plugins.postcss.formatter

import com.intellij.openapi.actionSystem.IdeActions
import org.intellij.plugins.postcss.PostCssFixtureTestCase

class PostCssFormatterTest : PostCssFixtureTestCase() {
  private fun doTest() {
    val testName = getTestName(false)
    myFixture.configureByFile("$testName.pcss")
    myFixture.performEditorAction(IdeActions.ACTION_EDITOR_REFORMAT)
    myFixture.checkResultByFile("${testName}_after.pcss")
  }

  fun testLineComment() = doTest()
}
