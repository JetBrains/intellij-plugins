package org.jetbrains.astro.codeInsight

import com.intellij.testFramework.junit5.TestApplication
import org.jetbrains.astro.AstroFixture
import org.jetbrains.astro.astroFixture
import org.junit.jupiter.api.Test

@TestApplication
class AstroTypingTest {

  private val astro: AstroFixture by astroFixture("codeInsight/typing")

  @Test
  fun testBracesInExpressions() = doTest("{([{")

  @Test
  fun testDeleteBracesInExpressions() = doTest("\b\b\b\b")

  @Test
  fun testAttributeValueQuotes() = doTest("\"")

  @Test
  fun testDeleteAttributeValueQuotes() = doTest("\b")

  @Test
  fun testAttributeValueBraces() = doTest("{")

  @Test
  fun testDeleteAttributeValueBraces() = doTest("\b\b")

  @Test
  fun testParensInHtmlCode() = doTest("(")

  @Test
  fun testEmmetExpressions() = doTest("\t")

  @Test
  fun testHtmlTagRootElementCompletion() = doTest(">")

  @Test
  fun testNestedHtmlTagElementCompletion() = doTest(">")

  @Test
  fun testInnerHtmlTagElementCompletion() = doTest(">")

  @Test
  fun testHtmlEndTagElementCompletion() = doTest(">")

  //region Test configuration and helper methods
  private fun doTest(textToType: String) {
    astro.doEditorTypingTest(checkResult = true) {
      type(textToType)
    }
  }
  //endregion
}