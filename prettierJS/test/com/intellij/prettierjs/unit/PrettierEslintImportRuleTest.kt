// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.prettierjs.unit

import com.intellij.lang.html.HTMLLanguage
import com.intellij.lang.javascript.JavaScriptSupportLoader
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings
import com.intellij.lang.javascript.linter.eslint.unit.ESLintImportCodeStyleTestUtil.doImportTest
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.codeInsightFixture
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.codeInsightProjectFixture
import com.intellij.psi.codeStyle.CodeStyleSettings.QuoteStyle
import com.intellij.psi.formatter.xml.HtmlCodeStyleSettings
import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@TestApplication
class PrettierEslintImportRuleTest {
  private val projectFixture = codeInsightProjectFixture()
  private val myFixture by codeInsightFixture(projectFixture)

  @Test
  fun testBasic() {
    doImportTest(myFixture, """{"rules": {"prettier/prettier": ["error"]}}""",
                 {
                   it.getCustomSettings(HtmlCodeStyleSettings::class.java).HTML_ENFORCE_QUOTES = false
                   it.getCustomSettings(HtmlCodeStyleSettings::class.java).HTML_QUOTE_STYLE = QuoteStyle.Single
                   it.getCustomSettings(JSCodeStyleSettings::class.java).FORCE_QUOTE_STYlE = false
                   it.getLanguageIndentOptions(HTMLLanguage.INSTANCE).INDENT_SIZE = 5
                   it.getLanguageIndentOptions(JavaScriptSupportLoader.TYPESCRIPT).INDENT_SIZE = 5
                 },
                 {
                   assertEquals(it.getCustomSettings(HtmlCodeStyleSettings::class.java).HTML_ENFORCE_QUOTES, false)
                   assertEquals(it.getCustomSettings(HtmlCodeStyleSettings::class.java).HTML_QUOTE_STYLE, QuoteStyle.Single)
                   assertEquals(it.getCustomSettings(JSCodeStyleSettings::class.java).FORCE_QUOTE_STYlE, true)
                   assertEquals(it.getLanguageIndentOptions(HTMLLanguage.INSTANCE).INDENT_SIZE, 2)
                   assertEquals(it.getLanguageIndentOptions(JavaScriptSupportLoader.TYPESCRIPT).INDENT_SIZE, 2)
                 })
  }

  @Test
  fun testPrettierRC() {
    myFixture.configureByText(".prettierrc.json", """{"singleQuote": true, "tabWidth": 3}""")
    doImportTest(myFixture, """{"rules": {"prettier/prettier": ["error"]}}""",
                 {
                   it.getCustomSettings(HtmlCodeStyleSettings::class.java).HTML_ENFORCE_QUOTES = false
                   it.getCustomSettings(HtmlCodeStyleSettings::class.java).HTML_QUOTE_STYLE = QuoteStyle.Double
                   it.getLanguageIndentOptions(HTMLLanguage.INSTANCE).INDENT_SIZE = 5
                 },
                 {
                   assertEquals(it.getCustomSettings(HtmlCodeStyleSettings::class.java).HTML_ENFORCE_QUOTES, false)
                   assertEquals(it.getCustomSettings(HtmlCodeStyleSettings::class.java).HTML_QUOTE_STYLE, QuoteStyle.Double)
                   it.getLanguageIndentOptions(HTMLLanguage.INSTANCE).INDENT_SIZE = 3
                 })
  }

  @Test
  fun testOverridePrettierRC() {
    myFixture.configureByText(".prettierrc.json", """{"singleQuote": true, "tabWidth": 3}""")
    doImportTest(myFixture, """{"rules": {"prettier/prettier": ["error", {"singleQuote": false}]}}""",
                 {
                   it.getCustomSettings(HtmlCodeStyleSettings::class.java).HTML_ENFORCE_QUOTES = false
                   it.getCustomSettings(HtmlCodeStyleSettings::class.java).HTML_QUOTE_STYLE = QuoteStyle.Single
                   it.getLanguageIndentOptions(HTMLLanguage.INSTANCE).INDENT_SIZE = 5
                 },
                 {
                   assertEquals(it.getCustomSettings(HtmlCodeStyleSettings::class.java).HTML_ENFORCE_QUOTES, false)
                   assertEquals(it.getCustomSettings(HtmlCodeStyleSettings::class.java).HTML_QUOTE_STYLE, QuoteStyle.Single)
                   it.getLanguageIndentOptions(HTMLLanguage.INSTANCE).INDENT_SIZE = 3
                 })
  }

  @Test
  fun testIgnorePrettierRC() {
    myFixture.configureByText(".prettierrc.json", """{"singleQuote": true, "tabWidth": 3}""")
    doImportTest(myFixture, """{"rules": {"prettier/prettier": ["error", {"singleQuote": false}, {"usePrettierrc": false}]}}""",
                 {
                   it.getCustomSettings(HtmlCodeStyleSettings::class.java).HTML_ENFORCE_QUOTES = false
                   it.getCustomSettings(HtmlCodeStyleSettings::class.java).HTML_QUOTE_STYLE = QuoteStyle.Single
                   it.getLanguageIndentOptions(HTMLLanguage.INSTANCE).INDENT_SIZE = 5
                 },
                 {
                   assertEquals(it.getCustomSettings(HtmlCodeStyleSettings::class.java).HTML_ENFORCE_QUOTES, false)
                   assertEquals(it.getCustomSettings(HtmlCodeStyleSettings::class.java).HTML_QUOTE_STYLE, QuoteStyle.Single)
                   it.getLanguageIndentOptions(HTMLLanguage.INSTANCE).INDENT_SIZE = 2
                 })
  }
}
