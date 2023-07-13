package org.jetbrains.astro.codeInsight

import com.intellij.javascript.web.WebFrameworkTestCase.CommentStyle.BLOCK
import com.intellij.javascript.web.WebFrameworkTestCase.CommentStyle.LINE
import com.intellij.lang.javascript.JSTestUtils
import org.intellij.plugins.postcss.settings.PostCssCodeStyleSettings
import org.jetbrains.astro.AstroCodeInsightTestCase

class AstroCommenterTest : AstroCodeInsightTestCase("codeInsight/commenter") {

  fun testSimple() = doTest()

  fun testMultilineSelection() = doTest()

  fun testRootElement() = doTest()

  fun testRootElementSelection() = doTest()

  fun testRootElementMultilineSelection() = doTest()

  fun testSelection() = doTest()

  fun testNestedScript() = doTest()

  fun testNestedScriptSelection() = doTest()

  fun testNestedScriptMultilineSelection() = doTest()

  fun testFrontmatterBlockUncomment() = doCommentTest(BLOCK)

  fun testCss() = JSTestUtils.testWithTempCodeStyleSettings<Throwable>(myFixture.project) {
    val settings = it.getCustomSettings(PostCssCodeStyleSettings::class.java)
    val initialValue = settings.COMMENTS_INLINE_STYLE
    try {
      settings.COMMENTS_INLINE_STYLE = true
      doCommentTest(LINE, 0)
      settings.COMMENTS_INLINE_STYLE = false
      doCommentTest(LINE, 1)
    }
    finally {
      settings.COMMENTS_INLINE_STYLE = initialValue
    }
  }

  fun testSass() = doCommentTest(LINE)
  fun testScss() = doCommentTest(LINE)
  fun testLess() = doCommentTest(LINE)

  private fun doTest() {
    doCommentTest(LINE, 0)
    doCommentTest(BLOCK, 1)
  }

}