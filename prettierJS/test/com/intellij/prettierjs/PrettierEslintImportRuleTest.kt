// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.prettierjs

import com.intellij.lang.html.HTMLLanguage
import com.intellij.lang.javascript.JavaScriptSupportLoader
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings
import com.intellij.lang.javascript.linter.ESLintImportCodeStyleTestBase
import com.intellij.psi.codeStyle.CodeStyleSettings.QuoteStyle
import com.intellij.psi.formatter.xml.HtmlCodeStyleSettings
import org.junit.Assert

class PrettierEslintImportRuleTest : ESLintImportCodeStyleTestBase() {

  fun testBasic() {
    doImportTest("""{"rules": {"prettier/prettier": ["error"]}}""",
                 {
                   it.getCustomSettings(HtmlCodeStyleSettings::class.java).HTML_ENFORCE_QUOTES = false
                   it.getCustomSettings(HtmlCodeStyleSettings::class.java).HTML_QUOTE_STYLE = QuoteStyle.Single
                   it.getCustomSettings(JSCodeStyleSettings::class.java).FORCE_QUOTE_STYlE = false
                   it.getLanguageIndentOptions(HTMLLanguage.INSTANCE).INDENT_SIZE = 5
                   it.getLanguageIndentOptions(JavaScriptSupportLoader.TYPESCRIPT).INDENT_SIZE = 5
                 },
                 {
                   Assert.assertEquals(it.getCustomSettings(HtmlCodeStyleSettings::class.java).HTML_ENFORCE_QUOTES, true)
                   Assert.assertEquals(it.getCustomSettings(HtmlCodeStyleSettings::class.java).HTML_QUOTE_STYLE, QuoteStyle.Double)
                   Assert.assertEquals(it.getCustomSettings(JSCodeStyleSettings::class.java).FORCE_QUOTE_STYlE, true)
                   Assert.assertEquals(it.getLanguageIndentOptions(HTMLLanguage.INSTANCE).INDENT_SIZE, 2)
                   Assert.assertEquals(it.getLanguageIndentOptions(JavaScriptSupportLoader.TYPESCRIPT).INDENT_SIZE, 2)
                 })
  }

  fun testPrettierRC() {
    myFixture.configureByText(".prettierrc.json", """{"singleQuote": true, "tabWidth": 3}""")
    doImportTest("""{"rules": {"prettier/prettier": ["error"]}}""",
                 {
                   it.getCustomSettings(HtmlCodeStyleSettings::class.java).HTML_ENFORCE_QUOTES = false
                   it.getCustomSettings(HtmlCodeStyleSettings::class.java).HTML_QUOTE_STYLE = QuoteStyle.Double
                   it.getLanguageIndentOptions(HTMLLanguage.INSTANCE).INDENT_SIZE = 5
                 },
                 {
                   Assert.assertEquals(it.getCustomSettings(HtmlCodeStyleSettings::class.java).HTML_ENFORCE_QUOTES, true)
                   Assert.assertEquals(it.getCustomSettings(HtmlCodeStyleSettings::class.java).HTML_QUOTE_STYLE, QuoteStyle.Single)
                   it.getLanguageIndentOptions(HTMLLanguage.INSTANCE).INDENT_SIZE = 3
                 })
  }

  fun testOverridePrettierRC() {
    myFixture.configureByText(".prettierrc.json", """{"singleQuote": true, "tabWidth": 3}""")
    doImportTest("""{"rules": {"prettier/prettier": ["error", {"singleQuote": false}]}}""",
                 {
                   it.getCustomSettings(HtmlCodeStyleSettings::class.java).HTML_ENFORCE_QUOTES = false
                   it.getCustomSettings(HtmlCodeStyleSettings::class.java).HTML_QUOTE_STYLE = QuoteStyle.Single
                   it.getLanguageIndentOptions(HTMLLanguage.INSTANCE).INDENT_SIZE = 5
                 },
                 {
                   Assert.assertEquals(it.getCustomSettings(HtmlCodeStyleSettings::class.java).HTML_ENFORCE_QUOTES, true)
                   Assert.assertEquals(it.getCustomSettings(HtmlCodeStyleSettings::class.java).HTML_QUOTE_STYLE, QuoteStyle.Double)
                   it.getLanguageIndentOptions(HTMLLanguage.INSTANCE).INDENT_SIZE = 3
                 })
  }

  fun testIgnorePrettierRC() {
    myFixture.configureByText(".prettierrc.json", """{"singleQuote": true, "tabWidth": 3}""")
    doImportTest("""{"rules": {"prettier/prettier": ["error", {"singleQuote": false}, {"usePrettierrc": false}]}}""",
                 {
                   it.getCustomSettings(HtmlCodeStyleSettings::class.java).HTML_ENFORCE_QUOTES = false
                   it.getCustomSettings(HtmlCodeStyleSettings::class.java).HTML_QUOTE_STYLE = QuoteStyle.Single
                   it.getLanguageIndentOptions(HTMLLanguage.INSTANCE).INDENT_SIZE = 5
                 },
                 {
                   Assert.assertEquals(it.getCustomSettings(HtmlCodeStyleSettings::class.java).HTML_ENFORCE_QUOTES, true)
                   Assert.assertEquals(it.getCustomSettings(HtmlCodeStyleSettings::class.java).HTML_QUOTE_STYLE, QuoteStyle.Double)
                   it.getLanguageIndentOptions(HTMLLanguage.INSTANCE).INDENT_SIZE = 2
                 })
  }
}
