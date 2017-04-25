package org.jetbrains.vuejs.language

import com.intellij.javascript.HtmlInlineJSScriptTokenTypesProvider
import com.intellij.lang.LanguageHtmlInlineScriptTokenTypesProvider
import com.intellij.lang.LanguageHtmlScriptContentProvider
import com.intellij.lang.LanguageParserDefinitions
import com.intellij.lang.MetaLanguage
import com.intellij.lang.ecmascript6.ES6ScriptContentProvider
import com.intellij.lang.javascript.JavaScriptSupportLoader
import com.intellij.lang.javascript.JavascriptLanguage
import com.intellij.lang.javascript.dialects.ECMA6SyntaxHighlighterFactory
import com.intellij.lang.javascript.dialects.TypeScriptSyntaxHighlighterFactory
import com.intellij.lang.typescript.TypeScriptContentProvider
import com.intellij.lexer.EmbeddedTokenTypesProvider
import com.intellij.lexer.Lexer
import com.intellij.openapi.extensions.ExtensionPoint
import com.intellij.openapi.extensions.Extensions
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.testFramework.LexerTestCase
import com.intellij.testFramework.TestDataPath
import com.jetbrains.plugins.jade.JadeLanguage
import com.jetbrains.plugins.jade.psi.impl.JadeScriptContentProvider
import org.jetbrains.plugins.sass.SASSLanguage
import org.jetbrains.plugins.sass.SASSParserDefinition
import org.jetbrains.plugins.sass.SassTokenTypesProvider
import org.jetbrains.plugins.sass.highlighting.SASSSyntaxHighlighterFactory

@TestDataPath("\$CONTENT_ROOT/testData/lexer")
open class VueLexerTest : LexerTestCase() {
  override fun setUp() {
    super.setUp()
    registerMetaLanguage()
    registerEmbeddedTokens()
    registerScriptTokens()
  }

  companion object {
    fun registerMetaLanguage() {
      val extensionName = MetaLanguage.EP_NAME.name
      val area = Extensions.getRootArea()
      if (!area.hasExtensionPoint(extensionName)) {
        area.registerExtensionPoint(extensionName, MetaLanguage::class.java.name, ExtensionPoint.Kind.INTERFACE)
      }
    }

    fun registerEmbeddedTokens() {
      SyntaxHighlighterFactory.LANGUAGE_FACTORY.addExplicitExtension(SASSLanguage.INSTANCE, SASSSyntaxHighlighterFactory())
      val extensionName = EmbeddedTokenTypesProvider.EXTENSION_POINT_NAME.name
      val area = Extensions.getRootArea()
      if (!area.hasExtensionPoint(extensionName)) {
        area.registerExtensionPoint(extensionName, EmbeddedTokenTypesProvider::class.java.name, ExtensionPoint.Kind.INTERFACE)
      }
      val extensionPoint = area.getExtensionPoint<EmbeddedTokenTypesProvider>(extensionName)
      extensionPoint.registerExtension(SassTokenTypesProvider())
      LanguageParserDefinitions.INSTANCE.addExplicitExtension(SASSLanguage.INSTANCE, SASSParserDefinition())

      LanguageHtmlInlineScriptTokenTypesProvider.INSTANCE.addExplicitExtension(JavascriptLanguage.INSTANCE, HtmlInlineJSScriptTokenTypesProvider())
    }

    fun registerScriptTokens() {
      LanguageHtmlScriptContentProvider.INSTANCE.addExplicitExtension(JavaScriptSupportLoader.ECMA_SCRIPT_6, ES6ScriptContentProvider())
      SyntaxHighlighterFactory.LANGUAGE_FACTORY.addExplicitExtension(JavaScriptSupportLoader.ECMA_SCRIPT_6, ECMA6SyntaxHighlighterFactory())

      LanguageHtmlScriptContentProvider.INSTANCE.addExplicitExtension(JavaScriptSupportLoader.TYPESCRIPT, TypeScriptContentProvider())
      SyntaxHighlighterFactory.LANGUAGE_FACTORY.addExplicitExtension(JavaScriptSupportLoader.TYPESCRIPT, TypeScriptSyntaxHighlighterFactory())

      LanguageHtmlScriptContentProvider.INSTANCE.addExplicitExtension(JadeLanguage.INSTANCE, JadeScriptContentProvider())
    }
  }

  fun testScriptEmpty() = doFileTest("vue")
  fun testScriptTS() = doFileTest("vue")

  fun testStyleEmpty() = doFileTest("vue")
  fun testStyleSass() = doFileTest("vue")
  fun testStyleSassAfterTemplate() = doFileTest("vue")

  fun testTemplateEmpty() = doFileTest("vue")
  fun testTemplateInner() = doFileTest("vue")
  fun testTemplateInnerDouble() = doFileTest("vue")
  fun testTemplateJade() = doFileTest("vue")
  fun testTemplateNewLine() = doFileTest("vue")

  fun testBindingAttribute() = doFileTest("vue")
  fun testEventAttribute() = doFileTest("vue")

  fun testInterpolation() = doFileTest("vue")
  fun testInterpolationNewLine() = doFileTest("vue")
  fun testInterpolationDoubleNewLine() = doFileTest("vue")
  fun testInterpolationInText() = doFileTest("vue")
  fun testMultipleInterpolations() = doFileTest("vue")

  override fun createLexer(): Lexer = VueLexer()
  override fun getDirPath() = "/contrib/vuejs/testData/lexer"

  override fun doTest(text: String?) {
    super.doTest(text)
    checkCorrectRestart(text)
  }
}