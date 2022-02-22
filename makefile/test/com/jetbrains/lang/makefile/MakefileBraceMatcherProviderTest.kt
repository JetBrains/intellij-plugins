package com.jetbrains.lang.makefile

import com.intellij.codeInsight.highlighting.BraceMatchingUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.jetbrains.lang.makefile.psi.MakefileTypes.*
import org.intellij.lang.annotations.Language
import org.junit.Assert
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.runners.MethodSorters.NAME_ASCENDING

/**
 * @see MakefileBraceMatcherProvider
 */
@RunWith(JUnit4::class)
@FixMethodOrder(NAME_ASCENDING)
class MakefileBraceMatcherProviderTest : BasePlatformTestCase() {
  @Test
  fun defineMatchForwardCaretBeforeOpeningBrace(): Unit =
    doTest(DEFINE_ENDEF_EXAMPLE,
           openingBrace = "define",
           closingBrace = "endef",
           caretBeforeBrace = true,
           forward = true)

  @Test
  fun defineMatchForwardCaretAfterOpeningBrace(): Unit =
    doTest(DEFINE_ENDEF_EXAMPLE,
           openingBrace = "define",
           closingBrace = "endef",
           caretBeforeBrace = false,
           forward = true)

  @Test
  fun defineMatchBackwardCaretBeforeOpeningBrace(): Unit =
    doTest(DEFINE_ENDEF_EXAMPLE,
           openingBrace = "define",
           closingBrace = "endef",
           caretBeforeBrace = true,
           forward = false)

  @Test
  fun defineMatchBackwardCaretAfterOpeningBrace(): Unit =
    doTest(DEFINE_ENDEF_EXAMPLE,
           openingBrace = "define",
           closingBrace = "endef",
           caretBeforeBrace = false,
           forward = false)

  @Test
  fun curlyMatchForwardCaretBeforeOpeningBrace(): Unit =
    doTest(CURLY_BRACE_EXAMPLE,
           openingBrace = "{",
           closingBrace = "}",
           caretBeforeBrace = true,
           forward = true)

  @Test
  fun curlyMatchForwardCaretAfterOpeningBrace(): Unit =
    doTest(CURLY_BRACE_EXAMPLE,
           openingBrace = "{",
           closingBrace = "}",
           caretBeforeBrace = false,
           forward = true)

  @Test
  fun curlyMatchBackwardCaretBeforeOpeningBrace(): Unit =
    doTest(CURLY_BRACE_EXAMPLE,
           openingBrace = "{",
           closingBrace = "}",
           caretBeforeBrace = true,
           forward = false)

  @Test
  fun curlyMatchBackwardCaretAfterOpeningBrace(): Unit =
    doTest(CURLY_BRACE_EXAMPLE,
           openingBrace = "{",
           closingBrace = "}",
           caretBeforeBrace = false,
           forward = false)

  @Test
  fun parenthesisMatchForwardCaretBeforeOpeningBrace(): Unit =
    doTest(PARENTHESIS_EXAMPLE,
           openingBrace = "(",
           closingBrace = ")",
           caretBeforeBrace = true,
           forward = true)

  @Test
  fun parenthesisMatchForwardCaretAfterOpeningBrace(): Unit =
    doTest(PARENTHESIS_EXAMPLE,
           openingBrace = "(",
           closingBrace = ")",
           caretBeforeBrace = false,
           forward = true)

  @Test
  fun parenthesisMatchBackwardCaretBeforeOpeningBrace(): Unit =
    doTest(PARENTHESIS_EXAMPLE,
           openingBrace = "(",
           closingBrace = ")",
           caretBeforeBrace = true,
           forward = false)

  @Test
  fun parenthesisMatchBackwardCaretAfterOpeningBrace(): Unit =
    doTest(PARENTHESIS_EXAMPLE,
           openingBrace = "(",
           closingBrace = ")",
           caretBeforeBrace = false,
           forward = false)

  private fun doTest(@Language("Makefile") text: String,
                     openingBrace: String,
                     closingBrace: String,
                     caretBeforeBrace: Boolean,
                     forward: Boolean) {
    val (braceWithCaret, matchingBrace) = when {
      forward -> openingBrace to closingBrace
      else -> closingBrace to openingBrace
    }

    val caretOffset = when {
      caretBeforeBrace -> text.indexOf(braceWithCaret)
      else -> text.indexOf(braceWithCaret) + braceWithCaret.length - 1
    }

    val textWithCaret = StringBuilder(text).insert(caretOffset, CARET_MARKER).toString()
    myFixture.configureByText(MakefileFileType, textWithCaret)
    val expectedBraceOffset = text.indexOf(matchingBrace)
    val actualBraceOffset = BraceMatchingUtil.getMatchedBraceOffset(myFixture.editor, forward, myFixture.file)

    Assert.assertEquals("$matchingBrace at offset", expectedBraceOffset, actualBraceOffset)
  }

  private companion object {
    private const val CARET_MARKER = "<caret>"

    /**
     * @see KEYWORD_DEFINE
     * @see KEYWORD_ENDEF
     */
    @Language("Makefile")
    private val DEFINE_ENDEF_EXAMPLE = """
      define two-lines
      echo foo
      echo bar
      endef
    """.trimIndent()

    /**
     * @see OPEN_CURLY
     * @see CLOSE_CURLY
     */
    @Language("Makefile")
    private val CURLY_BRACE_EXAMPLE = """
      export FOO := ${'$'}{BAR}
    """.trimIndent()

    /**
     * @see OPEN_PAREN
     * @see CLOSE_PAREN
     */
    @Language("Makefile")
    private val PARENTHESIS_EXAMPLE = """
      FILES = $(shell ls)
    """.trimIndent()
  }
}
