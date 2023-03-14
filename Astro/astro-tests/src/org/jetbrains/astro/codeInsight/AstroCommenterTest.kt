package org.jetbrains.astro.codeInsight

import com.intellij.openapi.actionSystem.IdeActions
import org.jetbrains.astro.AstroCodeInsightTestCase

class AstroCommenterTest : AstroCodeInsightTestCase() {
  fun testBlockComment() = doBlockCommentTest()

  fun testBlockCommentMultilineSelection() = doBlockCommentTest()

  fun testBlockCommentRootElement() = doBlockCommentTest()

  fun testBlockCommentRootElementMultilineSelection() = doBlockCommentTest()

  fun testBlockCommentRootElementSelection() = doBlockCommentTest()

  fun testBlockCommentSelection() = doBlockCommentTest()

  fun testLineComment() = doLineCommentTest()

  fun testLineCommentMultilineSelection() = doLineCommentTest()

  fun testLineCommentRootElement() = doLineCommentTest()

  fun testLineCommentRootElementMultilineSelection() = doLineCommentTest()

  fun testLineCommentRootElementSelection() = doLineCommentTest()

  fun testLineCommentSelection() = doLineCommentTest()

  fun testNestedScriptBlockComment() = doBlockCommentTest()

  fun testNestedScriptBlockCommentMultilineSelection() = doBlockCommentTest()

  fun testNestedScriptBlockCommentSelection() = doBlockCommentTest()

  fun testNestedScriptLineComment() = doLineCommentTest()

  fun testNestedScriptLineCommentMultilineSelection() = doLineCommentTest()

  fun testNestedScriptLineCommentSelection() = doLineCommentTest()

  fun testFrontmatterBlockUncomment() = doBlockCommentTest()

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