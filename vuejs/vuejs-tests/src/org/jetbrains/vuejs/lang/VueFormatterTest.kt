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
import com.jetbrains.plugins.jade.JadeLanguage
import org.jetbrains.plugins.scss.SCSSLanguage
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
