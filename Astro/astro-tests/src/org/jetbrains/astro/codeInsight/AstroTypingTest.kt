package org.jetbrains.astro.codeInsight

import org.jetbrains.astro.AstroCodeInsightTestCase

class AstroTypingTest : AstroCodeInsightTestCase("codeInsight/typing") {

  fun testBracesInExpressions() = doTest("{([{")

  fun testDeleteBracesInExpressions() = doTest("\b\b\b\b")

  fun testAttributeValueQuotes() = doTest("\"")

  fun testDeleteAttributeValueQuotes() = doTest("\b")

  fun testAttributeValueBraces() = doTest("{")

  fun testDeleteAttributeValueBraces() = doTest("\b\b")

  fun testParensInHtmlCode() = doTest("(")

  fun testEmmetExpressions() = doTest("\t")

  fun testHtmlTagRootElementCompletion() = doTest(">")

  fun testNestedHtmlTagElementCompletion() = doTest(">")

  fun testInnerHtmlTagElementCompletion() = doTest(">")

  fun testHtmlEndTagElementCompletion() = doTest(">")

  //region Test configuration and helper methods
  private fun doTest(textToType: String) {
    doEditorTypingTest(checkResult = true) {
      type(textToType)
    }
  }
  //endregion
}