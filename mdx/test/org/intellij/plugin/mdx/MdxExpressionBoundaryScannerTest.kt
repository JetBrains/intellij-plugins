package org.intellij.plugin.mdx

import com.intellij.testFramework.junit5.TestApplication
import org.intellij.plugin.mdx.lang.parse.MdxExpressionBoundaryScanner
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@TestApplication
class MdxExpressionBoundaryScannerTest {
  @Test
  fun stringAndTemplateBracesDoNotEndExpression() {
    assertExpressionEnd("{'}'}")
    assertExpressionEnd("{\"}\"}")
    assertExpressionEnd("{`a" + '$' + "{{ value: '}' }}b`}")
  }

  @Test
  fun commentsAndRegularExpressionsDoNotEndExpression() {
    assertExpressionEnd("{value.replace(/[/}]/g, '')}")
    assertExpressionEnd("{/a{1,2}/.test(value)}")
    assertExpressionEnd("{/* } */ value}")
    assertExpressionEnd("{// }\nvalue}")
  }

  @Test
  fun nestedJavaScriptAndJsxAreBalancedByPlatformLexer() {
    assertExpressionEnd("{{ value: { nested: true } }}")
    assertExpressionEnd("{() => { return <Item value={{ nested: true }} /> }}")
  }

  @Test
  fun unterminatedExpressionHasNoBoundary() {
    val text = "{`unterminated"
    assertEquals(-1, MdxExpressionBoundaryScanner.findExpressionEnd(text, 0, text.length))
    val regularExpression = "{/unterminated"
    assertEquals(-1, MdxExpressionBoundaryScanner.findExpressionEnd(regularExpression, 0, regularExpression.length))
  }

  @Test
  fun malformedMarkdownLikeJavaScriptStillEndsAtHostBrace() {
    assertExpressionEnd("{[Target](./target.mdx)}")
  }

  private fun assertExpressionEnd(text: String) {
    assertEquals(text.length, MdxExpressionBoundaryScanner.findExpressionEnd(text, 0, text.length), text)
  }
}
