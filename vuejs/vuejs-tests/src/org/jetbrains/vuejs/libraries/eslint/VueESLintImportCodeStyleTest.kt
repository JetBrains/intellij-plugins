// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.eslint

import com.intellij.lang.html.HTMLLanguage
import com.intellij.lang.javascript.linter.eslint.unit.ESLintImportCodeStyleTestUtil.doImportTest
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.codeInsightFixture
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.codeInsightProjectFixture
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CodeStyleSettings.HtmlTagNewLineStyle.Never
import com.intellij.psi.codeStyle.CodeStyleSettings.HtmlTagNewLineStyle.WhenMultiline
import com.intellij.psi.formatter.xml.HtmlCodeStyleSettings
import com.intellij.testFramework.junit5.TestApplication
import org.jetbrains.vuejs.lang.html.VueLanguage
import org.jetbrains.vuejs.lang.html.psi.formatter.VueCodeStyleSettings
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@TestApplication
class VueESLintImportCodeStyleTest {
  private val projectFixture = codeInsightProjectFixture()
  private val myFixture by codeInsightFixture(projectFixture)

  @Test
  fun testVueHtmlClosingBracketNewline() {
    doImportTest(myFixture, """{"rules": {"vue/html-closing-bracket-newline": ["error", {"multiline": "always"}]}}""",
                 { it.getCustomSettings(HtmlCodeStyleSettings::class.java).HTML_NEWLINE_AFTER_LAST_ATTRIBUTE = Never },
                 {
                   assertEquals(WhenMultiline,
                                it.getCustomSettings(HtmlCodeStyleSettings::class.java).HTML_NEWLINE_AFTER_LAST_ATTRIBUTE)
                 })
  }

  @Test
  fun testVueHtmlIndentWithTabsVue() {
    doImportTest(myFixture,
      """{"rules": {"vue/html-indent": ["warn", "tab", {"baseIndent": 1, "attribute": 2, "alignAttributesVertically": true}]}}""",
      { settings ->
        settings.getLanguageIndentOptions(VueLanguage).let {
          it.isOverrideLanguageOptions = true
          it.USE_TAB_CHARACTER = false
          it.INDENT_SIZE = 2
          it.CONTINUATION_INDENT_SIZE = 3
        }
        settings.getLanguageIndentOptions(HTMLLanguage.INSTANCE).let {
          it.USE_TAB_CHARACTER = false
          it.INDENT_SIZE = 2
          it.CONTINUATION_INDENT_SIZE = 3
        }
        settings.getCustomSettings(HtmlCodeStyleSettings::class.java).HTML_ALIGN_ATTRIBUTES = false
      },
      { settings ->
        settings.getLanguageIndentOptions(VueLanguage).let {
          assertTrue(it.USE_TAB_CHARACTER)
          assertEquals(1, it.INDENT_SIZE)
          assertEquals(2, it.CONTINUATION_INDENT_SIZE)
        }
        settings.getLanguageIndentOptions(HTMLLanguage.INSTANCE).let {
          assertFalse(it.USE_TAB_CHARACTER)
          assertEquals(2, it.INDENT_SIZE)
          assertEquals(3, it.CONTINUATION_INDENT_SIZE)
        }
        assertTrue(settings.getCustomSettings(HtmlCodeStyleSettings::class.java).HTML_ALIGN_ATTRIBUTES)
      })
  }

  @Test
  fun testVueHtmlIndentHtml() {
    doImportTest(myFixture,
      """{"rules": {"vue/html-indent": ["warn", 3, {"baseIndent": 1}]}}""",
      { settings ->
        settings.getLanguageIndentOptions(VueLanguage).let {
          it.isOverrideLanguageOptions = false
          it.USE_TAB_CHARACTER = true
          it.INDENT_SIZE = 1
          it.CONTINUATION_INDENT_SIZE = 2
        }
        settings.getLanguageIndentOptions(HTMLLanguage.INSTANCE).let {
          it.USE_TAB_CHARACTER = true
          it.INDENT_SIZE = 1
          it.CONTINUATION_INDENT_SIZE = 1
        }
        settings.getCustomSettings(HtmlCodeStyleSettings::class.java).HTML_ALIGN_ATTRIBUTES = false
      },
      { settings ->
        settings.getLanguageIndentOptions(VueLanguage).let {
          assertTrue(it.USE_TAB_CHARACTER)
          assertEquals(1, it.INDENT_SIZE)
          assertEquals(2, it.CONTINUATION_INDENT_SIZE)
        }
        settings.getLanguageIndentOptions(HTMLLanguage.INSTANCE).let {
          assertFalse(it.USE_TAB_CHARACTER)
          assertEquals(3, it.INDENT_SIZE)
          assertEquals(3, it.CONTINUATION_INDENT_SIZE)
        }
        assertTrue(settings.getCustomSettings(HtmlCodeStyleSettings::class.java).HTML_ALIGN_ATTRIBUTES)
      })
  }

  @Test
  fun testVueHtmlQuotes() {
    doImportTest(myFixture,
      """{"rules": {"vue/html-quotes": ["warn", "single"]}}""",
      { settings ->
        settings.getCustomSettings(HtmlCodeStyleSettings::class.java).let {
          it.HTML_QUOTE_STYLE = CodeStyleSettings.QuoteStyle.Double
          it.HTML_ENFORCE_QUOTES = false
        }
      },
      { settings ->
        settings.getCustomSettings(HtmlCodeStyleSettings::class.java).let {
          assertEquals(CodeStyleSettings.QuoteStyle.Single, it.HTML_QUOTE_STYLE)
          assertTrue(it.HTML_ENFORCE_QUOTES)
        }
      }
    )
  }

  @Test
  fun testVueHtmlQuotesDefault() {
    doImportTest(myFixture,
      """{"rules": {"vue/html-quotes": "warn"}}""",
      { settings ->
        settings.getCustomSettings(HtmlCodeStyleSettings::class.java).let {
          it.HTML_QUOTE_STYLE = CodeStyleSettings.QuoteStyle.Single
          it.HTML_ENFORCE_QUOTES = false
        }
      },
      { settings ->
        settings.getCustomSettings(HtmlCodeStyleSettings::class.java).let {
          assertEquals(CodeStyleSettings.QuoteStyle.Double, it.HTML_QUOTE_STYLE)
          assertTrue(it.HTML_ENFORCE_QUOTES)
        }
      }
    )
  }

  @Test
  fun testVueMustacheInterpolationSpacing() {
    doImportTest(myFixture,
      """{"rules": {"vue/mustache-interpolation-spacing": ["warn", "always"]}}""",
      { it.getCustomSettings(VueCodeStyleSettings::class.java).SPACES_WITHIN_INTERPOLATION_EXPRESSIONS = false },
      { assertTrue(it.getCustomSettings(VueCodeStyleSettings::class.java).SPACES_WITHIN_INTERPOLATION_EXPRESSIONS) }
    )
  }

  @Test
  fun testVueNoSpacesAroundEqualSignsInAttribute() {
    doImportTest(myFixture,
      """{"rules": {"vue/no-spaces-around-equal-signs-in-attribute": "warn"}}""",
      { it.getCustomSettings(HtmlCodeStyleSettings::class.java).HTML_SPACE_AROUND_EQUALITY_IN_ATTRIBUTE = true },
      { assertFalse(it.getCustomSettings(HtmlCodeStyleSettings::class.java).HTML_SPACE_AROUND_EQUALITY_IN_ATTRIBUTE) }
    )
  }

  @Test
  fun testVueScriptIndent() {
    doImportTest(myFixture,
      """{"rules": {"vue/script-indent": "warn"}}""",
      { it.getCustomSettings(VueCodeStyleSettings::class.java).INDENT_CHILDREN_OF_TOP_LEVEL = "script" },
      { assertEquals("", it.getCustomSettings(VueCodeStyleSettings::class.java).INDENT_CHILDREN_OF_TOP_LEVEL) }
    )
  }

}
