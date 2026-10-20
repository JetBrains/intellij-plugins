package org.jetbrains.astro.codeInsight

import com.intellij.lang.javascript.JSTestUtils
import com.intellij.polySymbols.testFramework.PolySymbolsFixture.CommentStyle.BLOCK
import com.intellij.polySymbols.testFramework.PolySymbolsFixture.CommentStyle.LINE
import com.intellij.testFramework.junit5.TestApplication
import org.intellij.plugins.postcss.settings.PostCssCodeStyleSettings
import org.jetbrains.astro.AstroCodeInsightTestCase
import org.junit.jupiter.api.Test

@TestApplication
class AstroCommenterTest : AstroCodeInsightTestCase("codeInsight/commenter") {

  @Test
  fun testSimple() = doTest()

  @Test
  fun testMultilineSelection() = doTest()

  @Test
  fun testRootElement() = doTest()

  @Test
  fun testRootElementSelection() = doTest()

  @Test
  fun testRootElementMultilineSelection() = doTest()

  @Test
  fun testSelection() = doTest()

  @Test
  fun testNestedScript() = doTest()

  @Test
  fun testNestedScriptSelection() = doTest()

  @Test
  fun testNestedScriptMultilineSelection() = doTest()

  @Test
  fun testFrontmatterBlockUncomment() = astro.doCommentTest(BLOCK)

  @Test
  fun testCss() = JSTestUtils.testWithTempCodeStyleSettings<Throwable>(myFixture.project) {
    val settings = it.getCustomSettings(PostCssCodeStyleSettings::class.java)
    val initialValue = settings.COMMENTS_INLINE_STYLE
    try {
      settings.COMMENTS_INLINE_STYLE = true
      astro.doCommentTest(LINE, 0)
      settings.COMMENTS_INLINE_STYLE = false
      astro.doCommentTest(LINE, 1)
    }
    finally {
      settings.COMMENTS_INLINE_STYLE = initialValue
    }
  }

  @Test
  fun testSass() = astro.doCommentTest(LINE)
  @Test
  fun testScss() = astro.doCommentTest(LINE)
  @Test
  fun testLess() = astro.doCommentTest(LINE)

  private fun doTest() {
    astro.doCommentTest(LINE, 0)
    astro.doCommentTest(BLOCK, 1)
  }

}