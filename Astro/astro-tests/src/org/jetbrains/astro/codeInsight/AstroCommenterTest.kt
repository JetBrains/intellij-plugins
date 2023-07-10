package org.jetbrains.astro.codeInsight

import com.intellij.lang.javascript.JSTestUtils
import com.intellij.openapi.actionSystem.IdeActions
import org.intellij.plugins.postcss.settings.PostCssCodeStyleSettings
import org.jetbrains.astro.AstroCodeInsightTestCase
import org.jetbrains.astro.codeInsight.AstroCommenterTest.CommentStyle.BLOCK
import org.jetbrains.astro.codeInsight.AstroCommenterTest.CommentStyle.LINE

class AstroCommenterTest : AstroCodeInsightTestCase() {
  fun testSimple() = doTest()

  fun testMultilineSelection() = doTest()

  fun testRootElement() = doTest()

  fun testRootElementSelection() = doTest()

  fun testRootElementMultilineSelection() = doTest()

  fun testSelection() = doTest()

  fun testNestedScript() = doTest()

  fun testNestedScriptSelection() = doTest()

  fun testNestedScriptMultilineSelection() = doTest()

  fun testFrontmatterBlockUncomment() = doTest(BLOCK)

  fun testCss() = JSTestUtils.testWithTempCodeStyleSettings<Throwable>(myFixture.project) {
    val settings = it.getCustomSettings(PostCssCodeStyleSettings::class.java)
    val initialValue = settings.COMMENTS_INLINE_STYLE
    try {
      settings.COMMENTS_INLINE_STYLE = true
      doTest(0, LINE)
      settings.COMMENTS_INLINE_STYLE = false
      doTest(1, LINE)
    } finally {
      settings.COMMENTS_INLINE_STYLE = initialValue
    }
  }
  fun testSass() = doTest(LINE)
  fun testScss() = doTest(LINE)
  fun testLess() = doTest(LINE)

  override fun getBasePath() = "codeInsight/commenter"

  private fun doTest(commentStyle: CommentStyle) {
    val name = getTestName(true)
    myFixture.configureByFile("$name.astro")
    myFixture.performEditorAction(
      if (commentStyle == LINE) IdeActions.ACTION_COMMENT_LINE
      else IdeActions.ACTION_COMMENT_BLOCK
    )
    myFixture.checkResultByFile("${name}_after.astro")
  }

  private fun doTest(id: Int, commentStyle: CommentStyle) {
    val name = getTestName(true)
    myFixture.configureByFile("$name.astro")
    try {
      myFixture.performEditorAction(
        if (commentStyle == LINE) IdeActions.ACTION_COMMENT_LINE
        else IdeActions.ACTION_COMMENT_BLOCK
      )
      myFixture.checkResultByFile("${name}_after_$id.astro")
    } finally {
      myFixture.configureByFile("$name.astro")
    }
  }

  private fun doTest() {
    doTest(0, LINE)
    doTest(1, BLOCK)
  }

  private enum class CommentStyle {
    LINE,
    BLOCK
  }
}