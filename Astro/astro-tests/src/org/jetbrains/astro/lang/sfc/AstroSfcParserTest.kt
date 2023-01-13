package org.jetbrains.astro.lang.sfc

import com.intellij.html.HtmlParsingTest
import com.intellij.html.embedding.HtmlEmbeddedContentSupport
import com.intellij.javascript.JSHtmlEmbeddedContentSupport
import com.intellij.lang.LanguageASTFactory
import com.intellij.lang.LanguageHtmlScriptContentProvider
import com.intellij.lang.LanguageParserDefinitions
import com.intellij.lang.css.CSSLanguage
import com.intellij.lang.css.CSSParserDefinition
import com.intellij.lang.ecmascript6.ES6ScriptContentProvider
import com.intellij.lang.html.HTMLLanguage
import com.intellij.lang.html.HTMLParserDefinition
import com.intellij.lang.javascript.JavaScriptSupportLoader
import com.intellij.lang.javascript.JavascriptParserDefinition
import com.intellij.lang.javascript.dialects.ECMA6ParserDefinition
import com.intellij.lang.javascript.dialects.JSLanguageLevel
import com.intellij.lang.javascript.dialects.TypeScriptParserDefinition
import com.intellij.lang.javascript.index.FrameworkIndexingHandler
import com.intellij.lang.javascript.index.FrameworkIndexingHandlerEP
import com.intellij.lang.javascript.settings.JSRootConfiguration
import com.intellij.lang.javascript.settings.JSRootConfigurationBase
import com.intellij.lang.typescript.TypeScriptContentProvider
import com.intellij.lexer.EmbeddedTokenTypesProvider
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.progress.EmptyProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import com.intellij.psi.css.CssElementDescriptorProvider
import com.intellij.psi.css.CssEmbeddedTokenTypesProvider
import com.intellij.psi.css.CssHtmlEmbeddedContentSupport
import com.intellij.psi.css.impl.CssTreeElementFactory
import com.intellij.psi.css.impl.util.scheme.CssElementDescriptorFactory2
import com.intellij.psi.css.impl.util.scheme.CssElementDescriptorProviderImpl
import com.intellij.psi.impl.BlockSupportImpl
import com.intellij.psi.impl.DebugUtil
import com.intellij.psi.impl.source.html.TemplateHtmlScriptContentProvider
import com.intellij.psi.stubs.StubElementTypeHolderEP
import com.intellij.util.ObjectUtils
import org.jetbrains.astro.lang.jsx.psi.AstroJsxExpressionElementType
import org.jetbrains.astro.lang.jsx.psi.AstroJsxStubElementTypes
import org.jetbrains.astro.lang.sfc.lexer.AstroSfcEmbeddedContentSupport
import org.jetbrains.astro.lang.sfc.parser.AstroSfcParserDefinition

class AstroSfcParserTest : HtmlParsingTest("", "astro",
                                           AstroSfcParserDefinition(),
                                           HTMLParserDefinition(),
                                           JavascriptParserDefinition(),
                                           CSSParserDefinition()) {

  fun testBasicExpression() {
    doTestAstro("""
      { name }
    """)
  }

  fun testBasicExpressionWithTags() {
    doTestAstro("""
      { 12 + <a>foo</a> + 32 }
    """)
  }

  fun testNestedExpressionsWithTags() {
    doTestAstro("""
      { 12 + <a>foo{12 + <b>bar</> + 12}bar</a> + 32 }
    """)
  }

  fun testNestedExpressionEmptyTag() {
    doTestAstro("""
      foo<a><b>12</b>{23 + <c/> + 12} </a>foo
    """)
  }

  fun testNestedExpressionEmptyTagRandomBraces() {
    doTestAstro("""
      foo}<a><b>12</b>}{23 + <c/> + 12} </a>}
    """)
  }

  fun testBroken1() {
    doTestAstro("""
      <a><b>12</b>{23 +<c/></a>
    """)
  }

  fun testBroken2() {
    doTestAstro("""
      { 12 + <a>foo{12 + <b>bar</> + 12 bar</a> + 32 }
    """)
  }

  override fun setUp() {
    super.setUp()

    registerExtensions(EmbeddedTokenTypesProvider.EXTENSION_POINT_NAME, EmbeddedTokenTypesProvider::class.java,
                       listOf(CssEmbeddedTokenTypesProvider()))

    addExplicitExtension(LanguageASTFactory.INSTANCE, CSSLanguage.INSTANCE, CssTreeElementFactory())

    registerExtensionPoint(CssElementDescriptorProvider.EP_NAME, CssElementDescriptorProvider::class.java)
    registerExtension(CssElementDescriptorProvider.EP_NAME, CssElementDescriptorProviderImpl())
    application.registerService(CssElementDescriptorFactory2::class.java, CssElementDescriptorFactory2("css-parsing-tests.xml"))

    // Update parser definition if version is changed
    assert(JSLanguageLevel.DEFAULT == JSLanguageLevel.ES6)
    addExplicitExtension(LanguageParserDefinitions.INSTANCE, JavaScriptSupportLoader.ECMA_SCRIPT_6, ECMA6ParserDefinition())
    addExplicitExtension(LanguageParserDefinitions.INSTANCE, JavaScriptSupportLoader.TYPESCRIPT, TypeScriptParserDefinition())
    addExplicitExtension(LanguageHtmlScriptContentProvider.INSTANCE, JavaScriptSupportLoader.ECMA_SCRIPT_6, ES6ScriptContentProvider())
    addExplicitExtension(LanguageHtmlScriptContentProvider.INSTANCE, JavaScriptSupportLoader.TYPESCRIPT, TypeScriptContentProvider())
    addExplicitExtension(LanguageHtmlScriptContentProvider.INSTANCE, HTMLLanguage.INSTANCE, TemplateHtmlScriptContentProvider())

    registerExtensionPoint(FrameworkIndexingHandler.EP_NAME, FrameworkIndexingHandlerEP::class.java)

    project.registerService(JSRootConfiguration::class.java, MockJSRootConfiguration(project))

    HtmlEmbeddedContentSupport.register(application, testRootDisposable, AstroSfcEmbeddedContentSupport::class.java,
                                        CssHtmlEmbeddedContentSupport::class.java, JSHtmlEmbeddedContentSupport::class.java)

    registerExtensionPoint(StubElementTypeHolderEP.EP_NAME, StubElementTypeHolderEP::class.java)
    registerExtension(StubElementTypeHolderEP.EP_NAME,
                      StubElementTypeHolderEP().also {
                        it.holderClass = AstroJsxExpressionElementType::class.java.simpleName
                        it.externalIdPrefix = "ASTRO_JSX:"
                      })
    // Force create class
    AstroJsxStubElementTypes.STUB_VERSION
  }

  override fun checkResult(targetDataName: String, file: PsiFile) {
    super.checkResult(targetDataName, file)
    ensureReparsingConsistent(file)
  }

  private fun ensureReparsingConsistent(file: PsiFile) {
    DebugUtil.performPsiModification<RuntimeException>("ensureReparsingConsistent") {
      val fileText = file.text
      val diffLog = BlockSupportImpl().reparseRange(
        file, file.node, TextRange.allOf(fileText), fileText, EmptyProgressIndicator(), fileText)
      val event = diffLog.performActualPsiChange(file)
      assertEmpty(event.changedElements)
    }
  }

  override fun getTestDataPath(): String = PathManager.getHomePath() + "/contrib/Astro/astro-tests/testData/lang/sfc/parser"

  private fun doTestAstro(text: String) {
    doTest(text.trimIndent(), "test.astro")
  }

  override fun doTest(text: String, fileName: String) {
    super.doTest(
      if (fileName.endsWith(".astro"))
        text
      else
        "------\n$text",
      "${fileName.substring(0, fileName.lastIndexOf('.'))}.astro")
  }

  private class MockJSRootConfiguration(project: Project) : JSRootConfigurationBase(project) {

    override fun storeLanguageLevelAndUpdateCaches(languageLevel: JSLanguageLevel?) {
      if (myState == null) myState = State()
      myState.languageLevel = ObjectUtils.coalesce(languageLevel, JSLanguageLevel.DEFAULT).id
    }
  }
}