// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.eslint

import com.intellij.lang.html.HTMLLanguage
import com.intellij.lang.javascript.linter.ESLintImportCodeStyleTestBase
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CodeStyleSettings.HtmlTagNewLineStyle.Never
import com.intellij.psi.codeStyle.CodeStyleSettings.HtmlTagNewLineStyle.WhenMultiline
import com.intellij.psi.formatter.xml.HtmlCodeStyleSettings
import junit.framework.TestCase
import org.jetbrains.vuejs.lang.html.VueLanguage
import org.jetbrains.vuejs.lang.html.psi.formatter.VueCodeStyleSettings
import org.junit.Assert

class VueESLintImportCodeStyleTest : ESLintImportCodeStyleTestBase() {

  fun testVueHtmlClosingBracketNewline() {
    doImportTest("""{"rules": {"vue/html-closing-bracket-newline": ["error", {"multiline": "always"}]}}""",
                 { it.getCustomSettings(HtmlCodeStyleSettings::class.java).HTML_NEWLINE_AFTER_LAST_ATTRIBUTE = Never },
                 {
                   Assert.assertEquals(WhenMultiline,
                                       it.getCustomSettings(HtmlCodeStyleSettings::class.java).HTML_NEWLINE_AFTER_LAST_ATTRIBUTE)
                 })
  }

  fun testVueHtmlIndentWithTabsVue() {
    doImportTest(
      """{"rules": {"vue/html-indent": ["warn", "tab", {"baseIndent": 1, "attribute": 2, "alignAttributesVertically": true}]}}""",
      { settings ->
        settings.getLanguageIndentOptions(VueLanguage.INSTANCE).let {
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
        settings.getLanguageIndentOptions(VueLanguage.INSTANCE).let {
          TestCase.assertTrue(it.USE_TAB_CHARACTER)
          TestCase.assertEquals(1, it.INDENT_SIZE)
          TestCase.assertEquals(2, it.CONTINUATION_INDENT_SIZE)
        }
        settings.getLanguageIndentOptions(HTMLLanguage.INSTANCE).let {
          TestCase.assertFalse(it.USE_TAB_CHARACTER)
          TestCase.assertEquals(2, it.INDENT_SIZE)
          TestCase.assertEquals(3, it.CONTINUATION_INDENT_SIZE)
        }
        TestCase.assertTrue(settings.getCustomSettings(HtmlCodeStyleSettings::class.java).HTML_ALIGN_ATTRIBUTES)
      })
  }

  fun testVueHtmlIndentHtml() {
    doImportTest(
      """{"rules": {"vue/html-indent": ["warn", 3, {"baseIndent": 1}]}}""",
      { settings ->
        settings.getLanguageIndentOptions(VueLanguage.INSTANCE).let {
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
        settings.getLanguageIndentOptions(VueLanguage.INSTANCE).let {
          TestCase.assertTrue(it.USE_TAB_CHARACTER)
          TestCase.assertEquals(1, it.INDENT_SIZE)
          TestCase.assertEquals(2, it.CONTINUATION_INDENT_SIZE)
        }
        settings.getLanguageIndentOptions(HTMLLanguage.INSTANCE).let {
          TestCase.assertFalse(it.USE_TAB_CHARACTER)
          TestCase.assertEquals(3, it.INDENT_SIZE)
          TestCase.assertEquals(3, it.CONTINUATION_INDENT_SIZE)
        }
        TestCase.assertTrue(settings.getCustomSettings(HtmlCodeStyleSettings::class.java).HTML_ALIGN_ATTRIBUTES)
      })
  }

  fun testVueHtmlQuotes() {
    doImportTest(
      """{"rules": {"vue/html-quotes": ["warn", "single"]}}""",
      { settings ->
        settings.getCustomSettings(HtmlCodeStyleSettings::class.java).let {
          it.HTML_QUOTE_STYLE = CodeStyleSettings.QuoteStyle.Double
          it.HTML_ENFORCE_QUOTES = false
        }
      },
      { settings ->
        settings.getCustomSettings(HtmlCodeStyleSettings::class.java).let {
          TestCase.assertEquals(CodeStyleSettings.QuoteStyle.Single, it.HTML_QUOTE_STYLE)
          TestCase.assertTrue(it.HTML_ENFORCE_QUOTES)
        }
      }
    )
  }

  fun testVueHtmlQuotesDefault() {
    doImportTest(
      """{"rules": {"vue/html-quotes": "warn"}}""",
      { settings ->
        settings.getCustomSettings(HtmlCodeStyleSettings::class.java).let {
          it.HTML_QUOTE_STYLE = CodeStyleSettings.QuoteStyle.Single
          it.HTML_ENFORCE_QUOTES = false
        }
      },
      { settings ->
        settings.getCustomSettings(HtmlCodeStyleSettings::class.java).let {
          TestCase.assertEquals(CodeStyleSettings.QuoteStyle.Double, it.HTML_QUOTE_STYLE)
          TestCase.assertTrue(it.HTML_ENFORCE_QUOTES)
        }
      }
    )
  }

  fun testVueMustacheInterpolationSpacing() {
    doImportTest(
      """{"rules": {"vue/mustache-interpolation-spacing": ["warn", "always"]}}""",
      { it.getCustomSettings(VueCodeStyleSettings::class.java).SPACES_WITHIN_INTERPOLATION_EXPRESSIONS = false },
      { TestCase.assertTrue(it.getCustomSettings(VueCodeStyleSettings::class.java).SPACES_WITHIN_INTERPOLATION_EXPRESSIONS) }
    )
  }

  fun testVueNoSpacesAroundEqualSignsInAttribute() {
    doImportTest(
      """{"rules": {"vue/no-spaces-around-equal-signs-in-attribute": "warn"}}""",
      { it.getCustomSettings(HtmlCodeStyleSettings::class.java).HTML_SPACE_AROUND_EQUALITY_IN_ATTRIBUTE = true },
      { TestCase.assertFalse(it.getCustomSettings(HtmlCodeStyleSettings::class.java).HTML_SPACE_AROUND_EQUALITY_IN_ATTRIBUTE) }
    )
  }

  fun testVueScriptIndent() {
    doImportTest(
      """{"rules": {"vue/script-indent": "warn"}}""",
      { it.getCustomSettings(VueCodeStyleSettings::class.java).INDENT_CHILDREN_OF_TOP_LEVEL = "script" },
      { TestCase.assertEquals("", it.getCustomSettings(VueCodeStyleSettings::class.java).INDENT_CHILDREN_OF_TOP_LEVEL) }
    )
  }

}
