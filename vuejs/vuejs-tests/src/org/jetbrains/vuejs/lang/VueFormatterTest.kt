package org.jetbrains.vuejs.lang

import com.intellij.lang.css.CSSLanguage
import com.intellij.lang.html.HTMLLanguage
import com.intellij.lang.javascript.JSFixtureOptions
import com.intellij.lang.javascript.JSFormatterFixture
import com.intellij.lang.javascript.JSFormatterFixture.Companion.jsFormatterFixture
import com.intellij.lang.javascript.JSTestUtils
import com.intellij.lang.javascript.JavaScriptSupportLoader
import com.intellij.lang.javascript.JavascriptLanguage
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings
import com.intellij.lang.javascript.jsProjectFixture
import com.intellij.lang.typescript.formatter.TypeScriptCodeStyleSettings
import com.intellij.openapi.application.runReadActionBlocking
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.platform.testFramework.core.FileComparisonFailedError
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CommonCodeStyleSettings
import com.intellij.psi.css.codeStyle.CssCodeStyleSettings
import com.intellij.psi.formatter.xml.HtmlCodeStyleSettings
import com.intellij.testFramework.junit5.TestApplication
import com.jetbrains.plugins.jade.JadeLanguage
import org.jetbrains.plugins.sass.SASSLanguage
import org.jetbrains.plugins.scss.SCSSLanguage
import org.jetbrains.plugins.stylus.StylusLanguage
import org.jetbrains.plugins.stylus.settings.StylusCodeStyleSettings
import org.jetbrains.vuejs.lang.html.VueLanguage
import org.jetbrains.vuejs.lang.html.psi.formatter.VueCodeStyleSettings
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Paths

@TestApplication
class VueFormatterTest {
  private val projectFixture = jsProjectFixture()
  private val formatter by jsFormatterFixture(
    projectFixture,
    JSFixtureOptions(getVueTestDataPath() + "/", "js").withBasePath(JSFormatterFixture.BASE_PATH),
  )

  @BeforeEach
  fun setUp() {
    formatter.useReformatText = true
  }

  @Test
  fun testTypeScript() {
    JSTestUtils.testWithTempCodeStyleSettings<Throwable>(formatter.project) {
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

      formatter.doTestFromFile("vue")
    }
  }

  @Test
  fun testScriptTagWithinTemplateTag() {
    formatter.doTestFromFile("vue")
  }

  @Test
  fun testI18nTag() {
    formatter.doTestFromFile("vue")
  }

  @Test
  fun testIndentationPerLang() {
    JSTestUtils.testWithTempCodeStyleSettings<Throwable>(formatter.project) {
      it.getCustomSettings(VueCodeStyleSettings::class.java).UNIFORM_INDENT = false
      doIndentationTest(it)
    }
  }

  @Test
  fun testIndentationUniform() {
    JSTestUtils.testWithTempCodeStyleSettings<Throwable>(formatter.project) {
      it.getCustomSettings(VueCodeStyleSettings::class.java).UNIFORM_INDENT = true
      doIndentationTest(it)
    }
  }

  @Test
  fun testIndentationBlock1() {
    JSTestUtils.testWithTempCodeStyleSettings<Throwable>(formatter.project) {
      it.getCustomSettings(VueCodeStyleSettings::class.java).UNIFORM_INDENT = true
      it.getCustomSettings(VueCodeStyleSettings::class.java).INDENT_CHILDREN_OF_TOP_LEVEL = "script,style"
      doIndentationTest(it)
    }
  }

  @Test
  fun testIndentationBlock2() {
    JSTestUtils.testWithTempCodeStyleSettings<Throwable>(formatter.project) {
      it.getCustomSettings(VueCodeStyleSettings::class.java).UNIFORM_INDENT = false
      it.getCustomSettings(VueCodeStyleSettings::class.java).INDENT_CHILDREN_OF_TOP_LEVEL = "template,script"
      doIndentationTest(it)
    }
  }

  @Test
  fun testIndentationHtmlUniform() {
    JSTestUtils.testWithTempCodeStyleSettings<Throwable>(formatter.project) {
      it.getCustomSettings(VueCodeStyleSettings::class.java).UNIFORM_INDENT = false
      it.getCustomSettings(HtmlCodeStyleSettings::class.java).HTML_UNIFORM_INDENT = true
      doIndentationTest(it)
    }
  }

  @Test
  fun testPureHtmlUniformIndent() {
    formatter.fixture.configureVueDependencies()
    JSTestUtils.testWithTempCodeStyleSettings<Throwable>(formatter.project) {
      it.getCustomSettings(VueCodeStyleSettings::class.java).UNIFORM_INDENT = false
      it.getCustomSettings(HtmlCodeStyleSettings::class.java).HTML_UNIFORM_INDENT = true
      it.getLanguageIndentOptions(VueLanguage).INDENT_SIZE = 1
      it.getLanguageIndentOptions(HTMLLanguage.INSTANCE).INDENT_SIZE = 2
      it.getLanguageIndentOptions(JavascriptLanguage).INDENT_SIZE = 4
      formatter.doTestFromFile("html")
    }
  }

  @Test
  fun testPureHtmlPerLangIndent() {
    formatter.fixture.configureVueDependencies()
    JSTestUtils.testWithTempCodeStyleSettings<Throwable>(formatter.project) {
      it.getCustomSettings(VueCodeStyleSettings::class.java).UNIFORM_INDENT = true
      it.getCustomSettings(HtmlCodeStyleSettings::class.java).HTML_UNIFORM_INDENT = false
      it.getLanguageIndentOptions(VueLanguage).INDENT_SIZE = 1
      it.getLanguageIndentOptions(HTMLLanguage.INSTANCE).INDENT_SIZE = 2
      it.getLanguageIndentOptions(JavascriptLanguage).INDENT_SIZE = 4
      formatter.doTestFromFile("html")
    }
  }

  @Test
  fun testForceQuoteInCss() {
    JSTestUtils.testWithTempCodeStyleSettings<Throwable>(formatter.project) { styleSettings ->
      styleSettings.getCustomSettings(CssCodeStyleSettings::class.java).let {
        it.ENFORCE_QUOTES_ON_FORMAT = true
        it.USE_DOUBLE_QUOTES = false
      }
      formatter.doTestFromFile("vue")
    }
  }

  @Test
  fun testForceQuoteInHtml() {
    JSTestUtils.testWithTempCodeStyleSettings<Throwable>(formatter.project) { styleSettings ->
      styleSettings.getCustomSettings(HtmlCodeStyleSettings::class.java).let {
        it.HTML_ENFORCE_QUOTES = true
        it.HTML_QUOTE_STYLE = CodeStyleSettings.QuoteStyle.Single
      }
      formatter.doTestFromFile("vue")
    }
  }

  @Test
  fun testColonFormattingInStylus() {
    JSTestUtils.testWithTempCodeStyleSettings<Throwable>(formatter.project) { styleSettings ->
      styleSettings.getCustomSettings(StylusCodeStyleSettings::class.java).let {
        it.IS_PROPERTY_NAME_VALUE_SEPARATED_BY_COLON = false
        it.ENFORCE_PROPERTY_NAME_VALUE_SEPARATOR_ON_FORMAT = true
      }
      formatter.doTestFromFile("vue")
    }
  }

  @Test
  fun testSass() {
    JSTestUtils.testWithTempCodeStyleSettings<Throwable>(formatter.project) { styleSettings ->
      styleSettings.getCustomSettings(VueCodeStyleSettings::class.java).let {
        it.UNIFORM_INDENT = true
        it.INDENT_CHILDREN_OF_TOP_LEVEL = "style"
      }
      styleSettings.getLanguageIndentOptions(VueLanguage).INDENT_SIZE = 2
      styleSettings.getLanguageIndentOptions(SASSLanguage.INSTANCE).INDENT_SIZE = 1
      formatter.doTestFromFile("vue")
    }
  }

  @Test
  fun testStylus() {
    JSTestUtils.testWithTempCodeStyleSettings<Throwable>(formatter.project) { styleSettings ->
      styleSettings.getCustomSettings(VueCodeStyleSettings::class.java).let {
        it.UNIFORM_INDENT = true
        it.INDENT_CHILDREN_OF_TOP_LEVEL = "style"
      }
      styleSettings.getLanguageIndentOptions(VueLanguage).INDENT_SIZE = 2
      styleSettings.getLanguageIndentOptions(StylusLanguage.INSTANCE).INDENT_SIZE = 1
      formatter.doTestFromFile("vue")
    }
  }

  @Test
  fun testInjectionsJS() {
    JSTestUtils.testWithTempCodeStyleSettings<Throwable>(formatter.project) { styleSettings ->
      styleSettings.getCustomSettings(VueCodeStyleSettings::class.java).let {
        it.UNIFORM_INDENT = true
        it.INDENT_CHILDREN_OF_TOP_LEVEL = "template"
        it.SPACES_WITHIN_INTERPOLATION_EXPRESSIONS = true
      }
      styleSettings.getCommonSettings(JavascriptLanguage).SPACE_AROUND_ADDITIVE_OPERATORS = false
      styleSettings.getLanguageIndentOptions(VueLanguage).INDENT_SIZE = 2
      formatter.doTestFromFile("vue")
    }
  }

  @Test
  fun testInjectionsTS() {
    JSTestUtils.testWithTempCodeStyleSettings<Throwable>(formatter.project) { styleSettings ->
      styleSettings.getCustomSettings(VueCodeStyleSettings::class.java).let {
        it.UNIFORM_INDENT = true
        it.INDENT_CHILDREN_OF_TOP_LEVEL = "template"
        it.SPACES_WITHIN_INTERPOLATION_EXPRESSIONS = true
      }
      styleSettings.getCommonSettings(JavaScriptSupportLoader.TYPESCRIPT).SPACE_AROUND_ADDITIVE_OPERATORS = false
      styleSettings.getLanguageIndentOptions(VueLanguage).INDENT_SIZE = 2
      formatter.doTestFromFile("vue")
    }
  }

  @Test
  fun testInjectionsWithDifferentSpacing() {
    JSTestUtils.testWithTempCodeStyleSettings<Throwable>(formatter.project) { styleSettings ->
      styleSettings.getCustomSettings(VueCodeStyleSettings::class.java).let {
        it.UNIFORM_INDENT = true
        it.INDENT_CHILDREN_OF_TOP_LEVEL = "template"
        it.SPACES_WITHIN_INTERPOLATION_EXPRESSIONS = false
      }
      styleSettings.getCommonSettings(JavascriptLanguage).SPACE_AROUND_MULTIPLICATIVE_OPERATORS = false
      styleSettings.getCommonSettings(JavascriptLanguage).SPACE_AROUND_ADDITIVE_OPERATORS = true
      styleSettings.getLanguageIndentOptions(VueLanguage).INDENT_SIZE = 2
      formatter.doTestFromFile("vue")
    }
  }

  @Test
  fun testInterpolationWrapping() {
    formatter.fixture.configureByText("package.json", "{\"dependencies\":{\"vue\":\"*\"}}")
    JSTestUtils.testWithTempCodeStyleSettings<Throwable>(formatter.project) { styleSettings ->
      val vueSettings = styleSettings.getCustomSettings(VueCodeStyleSettings::class.java)
      vueSettings.UNIFORM_INDENT = true
      vueSettings.SPACES_WITHIN_INTERPOLATION_EXPRESSIONS = false
      styleSettings.getLanguageIndentOptions(VueLanguage).INDENT_SIZE = 4
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

  @Test
  fun testHardWrap() {
    JSTestUtils.testWithTempCodeStyleSettings<Throwable>(formatter.project) { styleSettings ->
      styleSettings.getCommonSettings(HTMLLanguage.INSTANCE).RIGHT_MARGIN = 30
      //styleSettings.getCustomSettings(HtmlCodeStyleSettings::class.java).HTML_TEXT_WRAP = WRAP_ALWAYS
      formatter.doTestFromFile("vue")
    }
  }

  private fun doTest(id: Int) {
    val testName = formatter.getTestName(false)
    val sourceFile = "${formatter.testDataPath}/${formatter.basePath}/$testName.vue"
    val goldFile = "${formatter.testDataPath}/${formatter.basePath}/${testName}_after$id.vue"
    formatter.doTestFromAbsolutePaths(sourceFile, goldFile, "vue")
    doEmbedmentTest(sourceFile, goldFile)
  }

  private fun doIndentationTest(settings: CodeStyleSettings) {
    settings.getLanguageIndentOptions(VueLanguage).INDENT_SIZE = 1
    settings.getLanguageIndentOptions(HTMLLanguage.INSTANCE).INDENT_SIZE = 2
    settings.getLanguageIndentOptions(CSSLanguage.INSTANCE).INDENT_SIZE = 3
    settings.getLanguageIndentOptions(JavascriptLanguage).INDENT_SIZE = 4
    //settings.getLanguageIndentOptions(JavaScriptSupportLoader.TYPESCRIPT).INDENT_SIZE = 5 // impossible to have TS and JS in single file
    settings.getLanguageIndentOptions(SCSSLanguage.INSTANCE).INDENT_SIZE = 6
    settings.getLanguageIndentOptions(JadeLanguage.INSTANCE).INDENT_SIZE = 7
    formatter.doTestFromAbsolutePaths(
      Paths.get(formatter.testDataPath, formatter.basePath, "Indentation.vue").toFile().toString(),
      Paths.get(formatter.testDataPath, formatter.basePath, formatter.getTestName(false) + "_after.vue").toFile().toString(),
      "vue",
    )
  }

  private fun doEmbedmentTest(sourceFile: String, goldFile: String) {
    formatter.fixture.configureByText("dummy.{{.}}.#@injected@#.html", loadText(File(sourceFile)))
    val psiElement = runReadActionBlocking {
      val start = formatter.editor.selectionModel.selectionStart
      val end = formatter.editor.selectionModel.selectionEnd
      var psiElement = formatter.file.findElementAt(start)
      while (psiElement != null && psiElement.textRange.endOffset != end) {
        psiElement = psiElement.parent
        if (psiElement is PsiFile) psiElement = null
      }

      psiElement ?: formatter.file
    }
    formatter.doReformat(psiElement)
    try {
      formatter.fixture.checkResult(loadText(File(goldFile)))
    }
    catch (e: FileComparisonFailedError) {
      throw FileComparisonFailedError(e.message, e.expectedStringPresentation, e.actualStringPresentation, goldFile)
    }
  }

  private fun loadText(file: File): String {
    try {
      return StringUtil.convertLineSeparators(FileUtil.loadFile(file))
    }
    catch (e: Exception) {
      throw RuntimeException(e)
    }

  }
}
