package org.jetbrains.vuejs.lang

import com.intellij.lang.css.CSSLanguage
import com.intellij.lang.html.HTMLLanguage
import com.intellij.lang.javascript.JSTestUtils
import com.intellij.lang.javascript.JavaScriptFormatterTestBase
import com.intellij.lang.javascript.JavaScriptSupportLoader
import com.intellij.lang.javascript.JavascriptLanguage
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings
import com.intellij.lang.typescript.formatter.TypeScriptCodeStyleSettings
import com.intellij.openapi.application.PathManager
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CommonCodeStyleSettings
import com.intellij.psi.css.codeStyle.CssCodeStyleSettings
import com.intellij.psi.formatter.xml.HtmlCodeStyleSettings
import com.jetbrains.plugins.jade.JadeLanguage
import org.jetbrains.plugins.sass.SASSLanguage
import org.jetbrains.plugins.scss.SCSSLanguage
import org.jetbrains.plugins.stylus.StylusLanguage
import org.jetbrains.vuejs.lang.html.VueLanguage
import org.jetbrains.vuejs.lang.html.psi.formatter.VueCodeStyleSettings
import java.nio.file.Paths

class VueFormatterTest : JavaScriptFormatterTestBase() {

  override fun getTestDataPath(): String = PathManager.getHomePath() + "/contrib/vuejs/vuejs-tests/testData/"

  init {
    myUseReformatText = true
  }

  fun testTypeScript() {
    JSTestUtils.testWithTempCodeStyleSettings<Throwable>(project) {
      val jsSettings = it.getCustomSettings(JSCodeStyleSettings::class.java)
      val typeScriptSettings = it.getCustomSettings(TypeScriptCodeStyleSettings::class.java)
      jsSettings.FORCE_SEMICOLON_STYLE = true
      jsSettings.USE_SEMICOLON_AFTER_STATEMENT = true
      jsSettings.FORCE_QUOTE_STYlE = true
      jsSettings.USE_DOUBLE_QUOTES = true

      typeScriptSettings.FORCE_SEMICOLON_STYLE = true
      typeScriptSettings.USE_SEMICOLON_AFTER_STATEMENT = false
      typeScriptSettings.FORCE_QUOTE_STYlE = true
      typeScriptSettings.USE_DOUBLE_QUOTES = false

      doTestFromFile("vue")
    }
  }

  fun testScriptTagWithinTemplateTag() {
    doTestFromFile("vue")
  }

  fun testPerLangIndent() {
    JSTestUtils.testWithTempCodeStyleSettings<Throwable>(project) {
      it.getCustomSettings(VueCodeStyleSettings::class.java).UNIFORM_INDENT = false
      doIndentationTest(it)
    }
  }

  fun testUniformIndent() {
    JSTestUtils.testWithTempCodeStyleSettings<Throwable>(project) {
      it.getCustomSettings(VueCodeStyleSettings::class.java).UNIFORM_INDENT = true
      doIndentationTest(it)
    }
  }

  fun testBlockIndentation1() {
    JSTestUtils.testWithTempCodeStyleSettings<Throwable>(project) {
      it.getCustomSettings(VueCodeStyleSettings::class.java).UNIFORM_INDENT = true
      it.getCustomSettings(VueCodeStyleSettings::class.java).INDENT_CHILDREN_OF_TOP_LEVEL = "script,style"
      doIndentationTest(it)
    }
  }

  fun testBlockIndentation2() {
    JSTestUtils.testWithTempCodeStyleSettings<Throwable>(project) {
      it.getCustomSettings(VueCodeStyleSettings::class.java).UNIFORM_INDENT = false
      it.getCustomSettings(VueCodeStyleSettings::class.java).INDENT_CHILDREN_OF_TOP_LEVEL = "template,script"
      doIndentationTest(it)
    }
  }

  fun testHtmlUniformIndent() {
    JSTestUtils.testWithTempCodeStyleSettings<Throwable>(project) {
      it.getCustomSettings(VueCodeStyleSettings::class.java).UNIFORM_INDENT = false
      it.getCustomSettings(HtmlCodeStyleSettings::class.java).HTML_UNIFORM_INDENT = true
      doIndentationTest(it)
    }
  }

  fun testPureHtmlUniformIndent() {
    myFixture.configureDependencies()
    JSTestUtils.testWithTempCodeStyleSettings<Throwable>(project) {
      it.getCustomSettings(VueCodeStyleSettings::class.java).UNIFORM_INDENT = false
      it.getCustomSettings(HtmlCodeStyleSettings::class.java).HTML_UNIFORM_INDENT = true
      it.getLanguageIndentOptions(VueLanguage.INSTANCE).INDENT_SIZE = 1
      it.getLanguageIndentOptions(HTMLLanguage.INSTANCE).INDENT_SIZE = 2
      it.getLanguageIndentOptions(JavascriptLanguage.INSTANCE).INDENT_SIZE = 4
      doTestFromFile("html")
    }
  }

  fun testPureHtmlPerLangIndent() {
    myFixture.configureDependencies()
    JSTestUtils.testWithTempCodeStyleSettings<Throwable>(project) {
      it.getCustomSettings(VueCodeStyleSettings::class.java).UNIFORM_INDENT = true
      it.getCustomSettings(HtmlCodeStyleSettings::class.java).HTML_UNIFORM_INDENT = false
      it.getLanguageIndentOptions(VueLanguage.INSTANCE).INDENT_SIZE = 1
      it.getLanguageIndentOptions(HTMLLanguage.INSTANCE).INDENT_SIZE = 2
      it.getLanguageIndentOptions(JavascriptLanguage.INSTANCE).INDENT_SIZE = 4
      doTestFromFile("html")
    }
  }

  fun testForceQuoteInCss() {
    JSTestUtils.testWithTempCodeStyleSettings<Throwable>(project) { styleSettings ->
      styleSettings.getCustomSettings(CssCodeStyleSettings::class.java).let {
        it.ENFORCE_QUOTES_ON_FORMAT = true
        it.USE_DOUBLE_QUOTES = false
      }
      doTestFromFile("vue")
    }
  }

  fun testForceQuoteInHtml() {
    JSTestUtils.testWithTempCodeStyleSettings<Throwable>(project) { styleSettings ->
      styleSettings.getCustomSettings(HtmlCodeStyleSettings::class.java).let {
        it.HTML_ENFORCE_QUOTES = true
        it.HTML_QUOTE_STYLE = CodeStyleSettings.QuoteStyle.Single
      }
      doTestFromFile("vue")
    }
  }

  fun testSass() {
    JSTestUtils.testWithTempCodeStyleSettings<Throwable>(project) { styleSettings ->
      styleSettings.getCustomSettings(VueCodeStyleSettings::class.java).let {
        it.UNIFORM_INDENT = true
        it.INDENT_CHILDREN_OF_TOP_LEVEL = "style"
      }
      styleSettings.getLanguageIndentOptions(VueLanguage.INSTANCE).INDENT_SIZE = 2
      styleSettings.getLanguageIndentOptions(SASSLanguage.INSTANCE).INDENT_SIZE = 1
      doTestFromFile("vue")
    }
  }

  fun testStylus() {
    JSTestUtils.testWithTempCodeStyleSettings<Throwable>(project) { styleSettings ->
      styleSettings.getCustomSettings(VueCodeStyleSettings::class.java).let {
        it.UNIFORM_INDENT = true
        it.INDENT_CHILDREN_OF_TOP_LEVEL = "style"
      }
      styleSettings.getLanguageIndentOptions(VueLanguage.INSTANCE).INDENT_SIZE = 2
      styleSettings.getLanguageIndentOptions(StylusLanguage.INSTANCE).INDENT_SIZE = 1
      doTestFromFile("vue")
    }
  }

  fun testInjections() {
    JSTestUtils.testWithTempCodeStyleSettings<Throwable>(project) { styleSettings ->
      styleSettings.getCustomSettings(VueCodeStyleSettings::class.java).let {
        it.UNIFORM_INDENT = true
        it.INDENT_CHILDREN_OF_TOP_LEVEL = "template"
        it.SPACES_WITHIN_INTERPOLATION_EXPRESSIONS = true
      }
      styleSettings.getCommonSettings(JavascriptLanguage.INSTANCE).SPACE_AROUND_ADDITIVE_OPERATORS = false
      styleSettings.getLanguageIndentOptions(VueLanguage.INSTANCE).INDENT_SIZE = 2
      doTestFromFile("vue")
    }
  }

  fun testInjections2() {
    JSTestUtils.testWithTempCodeStyleSettings<Throwable>(project) { styleSettings ->
      styleSettings.getCustomSettings(VueCodeStyleSettings::class.java).let {
        it.UNIFORM_INDENT = true
        it.INDENT_CHILDREN_OF_TOP_LEVEL = "template"
        it.SPACES_WITHIN_INTERPOLATION_EXPRESSIONS = false
      }
      styleSettings.getCommonSettings(JavascriptLanguage.INSTANCE).SPACE_AROUND_MULTIPLICATIVE_OPERATORS = false
      styleSettings.getCommonSettings(JavascriptLanguage.INSTANCE).SPACE_AROUND_ADDITIVE_OPERATORS = true
      styleSettings.getLanguageIndentOptions(VueLanguage.INSTANCE).INDENT_SIZE = 2
      doTestFromFile("vue")
    }
  }

  fun testInterpolationWrapping() {
    JSTestUtils.testWithTempCodeStyleSettings<Throwable>(project) { styleSettings ->
      val vueSettings = styleSettings.getCustomSettings(VueCodeStyleSettings::class.java)
      vueSettings.UNIFORM_INDENT = true
      vueSettings.SPACES_WITHIN_INTERPOLATION_EXPRESSIONS = false
      styleSettings.getLanguageIndentOptions(VueLanguage.INSTANCE).INDENT_SIZE = 4
      vueSettings.INTERPOLATION_NEW_LINE_BEFORE_END_DELIMITER = false
      vueSettings.INTERPOLATION_NEW_LINE_AFTER_START_DELIMITER = false

      vueSettings.INTERPOLATION_WRAP = CommonCodeStyleSettings.DO_NOT_WRAP
      doTest(1)

      vueSettings.INTERPOLATION_WRAP = CommonCodeStyleSettings.WRAP_AS_NEEDED
      doTest(2)

      vueSettings.INTERPOLATION_WRAP = CommonCodeStyleSettings.WRAP_ALWAYS
      doTest(3)

      vueSettings.INTERPOLATION_NEW_LINE_BEFORE_END_DELIMITER = true
      vueSettings.INTERPOLATION_NEW_LINE_AFTER_START_DELIMITER = true

      vueSettings.INTERPOLATION_WRAP = CommonCodeStyleSettings.DO_NOT_WRAP
      doTest(4)

      vueSettings.INTERPOLATION_WRAP = CommonCodeStyleSettings.WRAP_AS_NEEDED
      doTest(5)

      vueSettings.INTERPOLATION_WRAP = CommonCodeStyleSettings.WRAP_ALWAYS
      doTest(6)

      vueSettings.INTERPOLATION_WRAP = CommonCodeStyleSettings.WRAP_AS_NEEDED

      vueSettings.INTERPOLATION_NEW_LINE_AFTER_START_DELIMITER = false
      vueSettings.INTERPOLATION_NEW_LINE_BEFORE_END_DELIMITER = true
      doTest(7)

      vueSettings.INTERPOLATION_NEW_LINE_AFTER_START_DELIMITER = true
      vueSettings.INTERPOLATION_NEW_LINE_BEFORE_END_DELIMITER = false

      doTest(8)

    }
  }

  private fun doTest(id: Int) {
    val testName = getTestName(false)
    myFixture.configureByFile("$basePath/$testName.vue")
    try {
      val file = myFixture.file
      doReformat(file)
      myFixture.checkResultByFile("$basePath/${testName}_after$id.vue")
    }
    finally {
      myFixture.configureByFile("$basePath/$testName.vue")
    }
  }

  private fun doIndentationTest(settings: CodeStyleSettings) {
    settings.getLanguageIndentOptions(VueLanguage.INSTANCE).INDENT_SIZE = 1
    settings.getLanguageIndentOptions(HTMLLanguage.INSTANCE).INDENT_SIZE = 2
    settings.getLanguageIndentOptions(CSSLanguage.INSTANCE).INDENT_SIZE = 3
    settings.getLanguageIndentOptions(JavascriptLanguage.INSTANCE).INDENT_SIZE = 4
    settings.getLanguageIndentOptions(JavaScriptSupportLoader.TYPESCRIPT).INDENT_SIZE = 5
    settings.getLanguageIndentOptions(SCSSLanguage.INSTANCE).INDENT_SIZE = 6
    settings.getLanguageIndentOptions(JadeLanguage.INSTANCE).INDENT_SIZE = 7
    doTestFromAbsolutePaths(Paths.get(testDataPath, basePath, "indentation.vue").toFile().toString(),
                            Paths.get(testDataPath, basePath, getTestName(false) + "_after.vue").toFile().toString(),
                            "vue")
  }

}
