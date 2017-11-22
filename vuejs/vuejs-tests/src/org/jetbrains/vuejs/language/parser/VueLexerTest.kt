package org.jetbrains.vuejs.language.parser

import com.intellij.javascript.HtmlInlineJSScriptTokenTypesProvider
import com.intellij.lang.LanguageHtmlInlineScriptTokenTypesProvider
import com.intellij.lang.LanguageHtmlScriptContentProvider
import com.intellij.lang.LanguageParserDefinitions
import com.intellij.lang.MetaLanguage
import com.intellij.lang.ecmascript6.ES6ScriptContentProvider
import com.intellij.lang.javascript.JavaScriptSupportLoader
import com.intellij.lang.javascript.JavascriptLanguage
import com.intellij.lang.javascript.dialects.ECMA6SyntaxHighlighterFactory
import com.intellij.lang.javascript.dialects.JSLanguageLevel
import com.intellij.lang.javascript.dialects.TypeScriptSyntaxHighlighterFactory
import com.intellij.lang.typescript.TypeScriptContentProvider
import com.intellij.lexer.EmbeddedTokenTypesProvider
import com.intellij.lexer.Lexer
import com.intellij.openapi.extensions.ExtensionPoint
import com.intellij.openapi.extensions.Extensions
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.util.KeyedExtensionCollector
import com.intellij.testFramework.LexerTestCase
import com.intellij.testFramework.TestDataPath
import com.jetbrains.plugins.jade.JadeLanguage
import com.jetbrains.plugins.jade.psi.impl.JadeScriptContentProvider
import org.jetbrains.plugins.sass.SASSLanguage
import org.jetbrains.plugins.sass.SASSParserDefinition
import org.jetbrains.plugins.sass.SassTokenTypesProvider
import org.jetbrains.plugins.sass.highlighting.SASSSyntaxHighlighterFactory
import java.util.*
import kotlin.collections.forEach
import kotlin.collections.plus
import kotlin.jvm.java

@TestDataPath("\$CONTENT_ROOT/testData/lexer")
open class VueLexerTest : LexerTestCase() {
  val onTearDown : List<Runnable> = ArrayList()

  override fun setUp() {
    super.setUp()
    registerMetaLanguage()
    registerEmbeddedTokens()
    registerScriptTokens()
  }

  override fun tearDown() {
    super.tearDown()
    onTearDown.forEach(Runnable::run)
  }

  fun registerMetaLanguage() {
    val extensionName = MetaLanguage.EP_NAME.name
    val area = Extensions.getRootArea()
    if (!area.hasExtensionPoint(extensionName)) {
      area.registerExtensionPoint(extensionName, MetaLanguage::class.java.name, ExtensionPoint.Kind.INTERFACE)
      onTearDown.plus(Runnable() { area.unregisterExtensionPoint(extensionName) })
    }
  }

  fun <T, KeyT> addExplicitExtension(collector: KeyedExtensionCollector<T, KeyT>, key : KeyT, t : T) {
    if (!collector.forKey(key).isEmpty()) return
    collector.addExplicitExtension(key, t)
    onTearDown.plus(Runnable() { collector.removeExplicitExtension(key, t) })
  }

  fun registerEmbeddedTokens() {
    addExplicitExtension(SyntaxHighlighterFactory.LANGUAGE_FACTORY, SASSLanguage.INSTANCE, SASSSyntaxHighlighterFactory())

    val extensionName = EmbeddedTokenTypesProvider.EXTENSION_POINT_NAME.name
    val area = Extensions.getRootArea()
    if (!area.hasExtensionPoint(extensionName)) {
      area.registerExtensionPoint(extensionName, EmbeddedTokenTypesProvider::class.java.name, ExtensionPoint.Kind.INTERFACE)
      onTearDown.plus(Runnable(){area.unregisterExtensionPoint(extensionName)})
    }
    val extensionPoint = area.getExtensionPoint<EmbeddedTokenTypesProvider>(extensionName)
    val sassTokenTypesProvider = SassTokenTypesProvider()
    if (!extensionPoint.hasAnyExtensions()) {
      extensionPoint.registerExtension(sassTokenTypesProvider)
      onTearDown.plus(Runnable() { extensionPoint.unregisterExtension(sassTokenTypesProvider) })
    }

    addExplicitExtension(LanguageParserDefinitions.INSTANCE, SASSLanguage.INSTANCE, SASSParserDefinition())
    addExplicitExtension(LanguageHtmlInlineScriptTokenTypesProvider.INSTANCE, JavascriptLanguage.INSTANCE,
                         HtmlInlineJSScriptTokenTypesProvider())
  }

  fun registerScriptTokens() {
    addExplicitExtension(LanguageHtmlScriptContentProvider.INSTANCE, JavaScriptSupportLoader.ECMA_SCRIPT_6, ES6ScriptContentProvider())
    addExplicitExtension(SyntaxHighlighterFactory.LANGUAGE_FACTORY, JavaScriptSupportLoader.ECMA_SCRIPT_6, ECMA6SyntaxHighlighterFactory())

    addExplicitExtension(LanguageHtmlScriptContentProvider.INSTANCE, JavaScriptSupportLoader.TYPESCRIPT, TypeScriptContentProvider())
    addExplicitExtension(SyntaxHighlighterFactory.LANGUAGE_FACTORY, JavaScriptSupportLoader.TYPESCRIPT, TypeScriptSyntaxHighlighterFactory())

    addExplicitExtension(LanguageHtmlScriptContentProvider.INSTANCE, JadeLanguage.INSTANCE, JadeScriptContentProvider())
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
  fun testHtmlLangTemplate() = doFileTest("vue")
  fun testVFor() = doFileTest("vue")

  override fun createLexer(): Lexer = org.jetbrains.vuejs.language.VueLexer(JSLanguageLevel.ES6)
  override fun getDirPath() = "/contrib/vuejs/vuejs-tests/testData/lexer"

  override fun doTest(text: String?) {
    super.doTest(text)
    checkCorrectRestart(text)
  }
}