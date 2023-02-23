package org.jetbrains.astro.codeInsight

import com.intellij.openapi.actionSystem.IdeActions
import org.jetbrains.astro.AstroCodeInsightTestCase

class AstroCommenterTest : AstroCodeInsightTestCase() {
  fun testJsxBlockComment() = doBlockCommentTest()

  fun testJsxBlockCommentMultilineSelection() = doBlockCommentTest()

  fun testJsxBlockCommentRootElement() = doBlockCommentTest()

  fun testJsxBlockCommentRootElementMultilineSelection() = doBlockCommentTest()

  fun testJsxBlockCommentRootElementSelection() = doBlockCommentTest()

  fun testJsxBlockCommentSelection() = doBlockCommentTest()

  fun testJsxLineComment() = doLineCommentTest()

  fun testJsxLineCommentMultilineSelection() = doLineCommentTest()

  fun testJsxLineCommentRootElement() = doLineCommentTest()

  fun testJsxLineCommentRootElementMultilineSelection() = doLineCommentTest()

  fun testJsxLineCommentRootElementSelection() = doLineCommentTest()

  fun testJsxLineCommentSelection() = doLineCommentTest()

  fun testNestedScriptBlockComment() = doBlockCommentTest()

  fun testNestedScriptBlockCommentMultilineSelection() = doBlockCommentTest()

  fun testNestedScriptBlockCommentSelection() = doBlockCommentTest()

  fun testNestedScriptLineComment() = doLineCommentTest()

  fun testNestedScriptLineCommentMultilineSelection() = doLineCommentTest()

  fun testNestedScriptLineCommentSelection() = doLineCommentTest()

  override fun getBasePath() = "codeInsight/commenter"

  private fun doLineCommentTest() {
    configure()
    myFixture.performEditorAction(IdeActions.ACTION_COMMENT_LINE)
    checkResult()
  }

  private fun doBlockCommentTest() {
    configure()
    myFixture.performEditorAction(IdeActions.ACTION_COMMENT_BLOCK)
    checkResult()
  }
}