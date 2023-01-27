package org.jetbrains.astro.lang

import com.intellij.html.HtmlParsingTest
import com.intellij.html.embedding.HtmlEmbeddedContentSupport
import com.intellij.injected.editor.DocumentWindow
import com.intellij.javascript.JSHtmlEmbeddedContentSupport
import com.intellij.lang.LanguageASTFactory
import com.intellij.lang.LanguageHtmlScriptContentProvider
import com.intellij.lang.LanguageParserDefinitions
import com.intellij.lang.css.CSSLanguage
import com.intellij.lang.css.CSSParserDefinition
import com.intellij.lang.ecmascript6.ES6ScriptContentProvider
import com.intellij.lang.html.HTMLLanguage
import com.intellij.lang.html.HTMLParserDefinition
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.javascript.JavaScriptSupportLoader
import com.intellij.lang.javascript.JavascriptASTFactory
import com.intellij.lang.javascript.JavascriptLanguage
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
import com.intellij.openapi.Disposable
import com.intellij.openapi.editor.Document
import com.intellij.openapi.progress.EmptyProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Pair
import com.intellij.openapi.util.TextRange
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiLanguageInjectionHost
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
import org.jetbrains.astro.getAstroTestDataPath
import org.jetbrains.astro.lang.frontmatter.AstroFrontmatterLanguage
import org.jetbrains.astro.lang.parser.AstroParserDefinition

class AstroParserTest : HtmlParsingTest("", "astro",
                                        AstroParserDefinition(),
                                        HTMLParserDefinition(),
                                        JavascriptParserDefinition(),
                                        CSSParserDefinition()) {

  override fun testUnclosedTag() {
    // disable
  }

  override fun testContent1() {
    // disable
  }

  fun testBasic1() {
    doTestAstro("""
      ---
      const a = 12 - 2
      ---
      <div> { a } </div
    """)
  }

  fun testBasic2() {
    doTestAstro("""
      Some comment < 12
      ---
      const a = new Text<Foo>("12")
      ---
      Result is: { a }
    """)
  }

  fun testBasicComments() {
    doTestAstro("""
      ---
      import MyComponent from "./MyComponent.astro";
      const Element = 'div'
      const Component = MyComponent;
      ---
      <Element>Hello!</Element> <!-- renders as <div>Hello!</div> -->
      <Component /> <!-- renders as <MyComponent /> -->
    """)
  }

  fun testBasicExpressions() {
    doTestAstro("""
      ---
      const visible = true;
      ---
      {visible && <p>Show me!</p>}
      
      {visible ? <p>Show me!</p> : <p>Else show me!</p>}
    """)
  }

  fun testBasicAttributeExpressions() {
    doTestAstro("""
      ---
      const name = "Astro";
      ---
      <h1 class={name}>Attribute expressions are supported</h1>
      
      <MyComponent templateLiteralNameAttribute={`MyNameIs${name}`} />
    """)
  }

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

  fun testAttributeExpression() {
    doTestAstro("""
      { <a href={url}>{url}</a> }
    """)
  }

  fun testAttributeExpressionNoTags() {
    doTestAstro("""
      { <a href={url<foo>bar}></a> }
    """)
  }

  fun testAttributeExpressionNestedTemplateLiteral() {
    doTestAstro("""
      { <a href={`https://${"$"}{site + `foo`}bar`}>{url}</a> }
    """)
  }

  fun testSpreadAttribute() {
    doTestAstro("""
      { <a {...url}>{url}</a> }
    """)
  }

  fun testShorthandAttribute() {
    doTestAstro("""
      { <a {url}>{url}</a> }
    """)
  }

  fun testTemplateLiteralAttribute() {
    doTestAstro("""
      { <a url=`https://${"$"}{site}`>{site}</a> }
    """)
  }

  fun testTemplateLiteralAttributeNoNested() {
    doTestAstro("""
      { <a url=`https://${"$"}{site + `>{site}</a> }
    """)
  }

  fun testTemplateLiteralAttributeNoTags() {
    doTestAstro("""
      { <a url=`https://${"$"}{site<foo>bar}`>{site}</a> }
    """)
  }

  fun testExpressionUnterminated() {
    doTestAstro("""
      {12<
    """)
  }

  fun testExpressionNotClosedIfBraceIsAttribute() {
    doTestAstro("""
      {12 + <a {/>} foo } bar> } fooBar
    """)
  }

  fun testExpressionNoNestedTemplateExpressions() {
    doTestAstro("""
      {12 + `this is ${'$'}{within `an }` expression}` }
    """)
  }

  fun testExpressionDoubleQuoteEscape() {
    doTestAstro("""
      { "This } is \" escaped and ' } " } outside
    """)
  }

  fun testExpressionSingleQuoteEscape() {
    doTestAstro("""
      { 'This } is \' escaped } and " ' }
    """)
  }

  fun testExpressionHtmlStyleComment() {
    doTestAstro("""
      { <!-- this is a comment } ha --> }
    """)
  }

  fun testExpressionMultilineComment() {
    doTestAstro("""
      { /* this is a comment } ha/ */ }
    """)
  }

  fun testExpressionSingleLineComment() {
    doTestAstro("""
      { // this is a comment } ha/ }
       and here is end}
    """)
  }

  fun testShorthandAttributeBeforeAndAfterContent() {
    doTestAstro("""
      <a before{foo}after>
    """)
  }

  fun testNoShorthandAttributeIfValue() {
    doTestAstro("""
      <a {foo} = 12>
    """)
  }

  fun testShorthandAttributeUnterminated() {
    doTestAstro("""
      <a {foo} {"}"a fooBar>
    """)
  }

  fun testShorthandAttributeRegexBoundaryAndUnterminated() {
    doTestAstro("""
      <a {">" /} {/> fooBar>
    """)
  }

  fun testSpreadAttributeContentBeforeAndAfter() {
    doTestAstro("""
      <a before{ ...foo}after>
    """)
  }

  fun testNoSpreadAttributeIfValue() {
    doTestAstro("""
      <a {... "12}>`"} = 12>
    """)
  }

  fun testSpreadAttributeManyAttributes() {
    doTestAstro("""
      <a {....foo} {bar} fooBar>
    """)
  }

  fun testAttributeExpressionNestedTemplateLiteralsSupported() {
    doTestAstro("""
      <a foo={`template${'$'}{expression + `template}` + expression } template}` + attr_expression} attr}>}
    """)
  }

  fun testAttributeExpressionNoEscapeTemplateLiteral() {
    doTestAstro("""
      <a foo={`}>'"/\`}>
    """)
  }

  fun testAttributeExpressionNoEscapeMultilineComment() {
    doTestAstro("""
      <a foo={/*><}**\*/}>
    """)
  }

  fun testAttributeExpressionSingleQuoteEscape() {
    doTestAstro("""
      <a foo={'<">\'}'}>
    """)
  }

  fun testAttributeExpressionDoubleQuoteEscape() {
    doTestAstro("""
      <a foo={"<'>\"}"}>
    """)
  }

  fun testAttributeExpressionEscapeRegex() {
    doTestAstro("""
      <a foo={/regexp\/foo/}>
    """)
  }

  fun testAttributeExpressionRegexBoundary() {
    doTestAstro("""
      <a foo={/regexp} bar={/regexp"\}/}>
    """)
  }

  fun testAttributeExpressionSingleLineCommentBoundary() {
    doTestAstro("""
      <a foo={// comment }
      end}>
    """)
  }

  fun testTemplateLiteralAttributeNoEscape() {
    doTestAstro("""
      <a foo=`12\`>
    """)
  }

  fun testTemplateLiteralAttributeUnterminated() {
    doTestAstro("""
      <a foo=`12\>
    """)
  }

  fun testCharEntity() {
    doTestAstro("""
      {12 &lt; <span>&rarr;</span>}
    """)
  }

  fun testComplexBroken() {
    doTestAstro("""
      <li class="link-card">
       <a title=`112 \` ${'$'}{12 + "12"}`
         <h2>
           {12 + 34}
           <span>&rarr;</span>
         </h2>
         <p>
           {  <a foo={1223 + `121321${'$'}{``}`}> + 12 }
         </p>
       </a>
      </li>
    """)
  }

  fun testBlockStatementParsing() {
    doTestAstro("""
      ---
      {
        const a = 12
      }
      ---
      <div>{ (() => { return <a></a> })() }</div>
    """)
  }

  fun testAutoClosingOverExpression() {
    doTestAstro("""  
      <main>
        <p>Foo
        {<p>Bar
    """)
  }

  fun testAutoClosing() {
    doTestAstro("""
      {
       <p>Foo
       <p>Bar
       </>
       + 12
      }
    """)
  }

  fun testAutoClosingNested() {
    doTestAstro("""
      {
       <p>Foo
       {
         <p>FooBar
         <p>Bar
         </>
         + 12
       }
       <p>Foo2
       </>
       +12
      }
    """)
  }

  fun testEmptyTags() {
    doTestAstro("""
      {
       <img> + 12
      }
    """)
  }

  fun testMalformedExpression() {
    doTestAstro("""
      <foo>
        { 12 <p></> }
      </foo>
    """)
  }

  fun testContentBeforeFrontmatterWithWhitespace() {
    doTestAstro("""
     
          foo - <
      bar
      
           ---export interface Props {title: string;
      body: string; href: string;
      }
      const {
        title, href,
        body} =
        Astro.props; ---
    """)
  }

  override fun setUp() {
    super.setUp()

    addExplicitExtension(LanguageParserDefinitions.INSTANCE, AstroFrontmatterLanguage.INSTANCE, TypeScriptParserDefinition())

    registerExtensions(EmbeddedTokenTypesProvider.EXTENSION_POINT_NAME, EmbeddedTokenTypesProvider::class.java,
                       listOf(CssEmbeddedTokenTypesProvider()))

    addExplicitExtension(LanguageASTFactory.INSTANCE, CSSLanguage.INSTANCE, CssTreeElementFactory())
    addExplicitExtension(LanguageASTFactory.INSTANCE, JavascriptLanguage.INSTANCE, JavascriptASTFactory())

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

    HtmlEmbeddedContentSupport.register(application, testRootDisposable,
                                        CssHtmlEmbeddedContentSupport::class.java, JSHtmlEmbeddedContentSupport::class.java)

    registerExtensionPoint(StubElementTypeHolderEP.EP_NAME, StubElementTypeHolderEP::class.java)

    project.registerService(InjectedLanguageManager::class.java, MockInjectedLanguageManager())

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

  override fun getTestDataPath(): String = getAstroTestDataPath() + "/lang/parser"

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

  class MockInjectedLanguageManager internal constructor() : InjectedLanguageManager() {
    override fun getInjectionHost(injectedProvider: FileViewProvider): PsiLanguageInjectionHost? = null
    override fun getInjectionHost(injectedElement: PsiElement): PsiLanguageInjectionHost? = null
    override fun injectedToHost(injectedContext: PsiElement, injectedTextRange: TextRange): TextRange = injectedTextRange
    override fun injectedToHost(injectedContext: PsiElement, injectedOffset: Int): Int = 0
    override fun injectedToHost(injectedContext: PsiElement, injectedOffset: Int, minHostOffset: Boolean): Int = 0
    override fun registerMultiHostInjector(injector: MultiHostInjector, parentDisposable: Disposable) {}
    override fun getUnescapedText(injectedNode: PsiElement): String = injectedNode.text
    override fun intersectWithAllEditableFragments(injectedPsi: PsiFile, rangeToEdit: TextRange): List<TextRange> = listOf(rangeToEdit)
    override fun isInjectedFragment(injectedFile: PsiFile): Boolean = false
    override fun findInjectedElementAt(hostFile: PsiFile, hostDocumentOffset: Int): PsiElement? = null
    override fun getInjectedPsiFiles(host: PsiElement): List<Pair<PsiElement, TextRange>>? = null
    override fun dropFileCaches(file: PsiFile) {}
    override fun getTopLevelFile(element: PsiElement): PsiFile = element.containingFile
    override fun getCachedInjectedDocumentsInRange(hostPsiFile: PsiFile, range: TextRange): List<DocumentWindow> = emptyList()
    override fun enumerate(host: PsiElement, visitor: PsiLanguageInjectionHost.InjectedPsiVisitor) {}
    override fun enumerateEx(host: PsiElement,
                             containingFile: PsiFile,
                             probeUp: Boolean,
                             visitor: PsiLanguageInjectionHost.InjectedPsiVisitor) {
    }

    override fun getNonEditableFragments(window: DocumentWindow): List<TextRange> = emptyList()
    override fun mightHaveInjectedFragmentAtOffset(hostDocument: Document, hostOffset: Int): Boolean = false
    override fun freezeWindow(document: DocumentWindow): DocumentWindow = document
  }

}