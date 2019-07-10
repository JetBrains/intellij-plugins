// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.parser

import com.intellij.html.HtmlParsingTest
import com.intellij.javascript.HtmlInlineJSScriptTokenTypesProvider
import com.intellij.javascript.JSScriptContentProvider
import com.intellij.lang.*
import com.intellij.lang.css.CSSLanguage
import com.intellij.lang.css.CSSParserDefinition
import com.intellij.lang.html.HTMLLanguage
import com.intellij.lang.html.HTMLParserDefinition
import com.intellij.lang.javascript.JavascriptLanguage
import com.intellij.lang.javascript.JavascriptParserDefinition
import com.intellij.lang.javascript.dialects.ECMA6ParserDefinition
import com.intellij.lang.javascript.dialects.JSLanguageLevel
import com.intellij.lang.javascript.settings.JSRootConfiguration
import com.intellij.lang.javascript.settings.JSRootConfigurationBase
import com.intellij.lexer.EmbeddedTokenTypesProvider
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.progress.EmptyProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import com.intellij.psi.css.CssElementDescriptorProvider
import com.intellij.psi.css.CssEmbeddedTokenTypesProvider
import com.intellij.psi.css.CssRulesetBlockEmbeddedTokenTypesProvider
import com.intellij.psi.css.impl.CssTreeElementFactory
import com.intellij.psi.css.impl.util.scheme.CssElementDescriptorFactory2
import com.intellij.psi.css.impl.util.scheme.CssElementDescriptorProviderImpl
import com.intellij.psi.impl.BlockSupportImpl
import com.intellij.psi.impl.DebugUtil
import com.intellij.psi.impl.source.html.TemplateHtmlScriptContentProvider
import com.intellij.testFramework.UsefulTestCase
import com.intellij.util.ObjectUtils
import org.jetbrains.vuejs.lang.expr.VueJSParserDefinition
import org.jetbrains.vuejs.lang.html.parser.VueParserDefinition

class VueParserTest : HtmlParsingTest("", "html",
                                      VueParserDefinition(),
                                      VueJSParserDefinition(),
                                      HTMLParserDefinition(),
                                      JavascriptParserDefinition(),
                                      CSSParserDefinition()) {

  override fun setUp() {
    super.setUp()
    addExplicitExtension<HtmlInlineScriptTokenTypesProvider>(LanguageHtmlInlineScriptTokenTypesProvider.INSTANCE,
                                                             JavascriptLanguage.INSTANCE,
                                                             HtmlInlineJSScriptTokenTypesProvider())
    addExplicitExtension<HtmlScriptContentProvider>(LanguageHtmlScriptContentProvider.INSTANCE, JavascriptLanguage.INSTANCE,
                                                    JSScriptContentProvider())
    registerExtensionPoint<EmbeddedTokenTypesProvider>(EmbeddedTokenTypesProvider.EXTENSION_POINT_NAME,
                                                       EmbeddedTokenTypesProvider::class.java)
    registerExtension<EmbeddedTokenTypesProvider>(EmbeddedTokenTypesProvider.EXTENSION_POINT_NAME, CssEmbeddedTokenTypesProvider())
    registerExtension<EmbeddedTokenTypesProvider>(EmbeddedTokenTypesProvider.EXTENSION_POINT_NAME,
                                                  CssRulesetBlockEmbeddedTokenTypesProvider())

    addExplicitExtension<ASTFactory>(LanguageASTFactory.INSTANCE, CSSLanguage.INSTANCE, CssTreeElementFactory())
    registerExtensionPoint<CssElementDescriptorProvider>(CssElementDescriptorProvider.EP_NAME, CssElementDescriptorProvider::class.java)
    registerExtension<CssElementDescriptorProvider>(CssElementDescriptorProvider.EP_NAME, CssElementDescriptorProviderImpl())
    registerApplicationService<CssElementDescriptorFactory2>(CssElementDescriptorFactory2::class.java,
                                                             CssElementDescriptorFactory2(ProgressManager.getInstance(),
                                                                                          "css-parsing-tests.xml"))

    // Update parser definition if version is changed
    assert(JSLanguageLevel.DEFAULT == JSLanguageLevel.ES6)
    addExplicitExtension<ParserDefinition>(LanguageParserDefinitions.INSTANCE, JSLanguageLevel.ES6.dialect, ECMA6ParserDefinition())

    addExplicitExtension(LanguageHtmlScriptContentProvider.INSTANCE, HTMLLanguage.INSTANCE, TemplateHtmlScriptContentProvider())

    registerApplicationService(JSRootConfiguration::class.java, MockJSRootConfiguration(project))
  }

  override fun checkResult(targetDataName: String, file: PsiFile) {
    super.checkResult(targetDataName, file)
    ensureReparsingConsistent(file)
  }

  private fun ensureReparsingConsistent(file: PsiFile) {
    DebugUtil.performPsiModification<RuntimeException>("ensureReparsingConsistent") {
      val fileText = file.text
      val diffLog = BlockSupportImpl(file.project).reparseRange(
        file, file.node, TextRange.allOf(fileText), fileText, EmptyProgressIndicator(), fileText)
      val event = diffLog.performActualPsiChange(file)
      UsefulTestCase.assertEmpty(event.changedElements)
    }
  }

  private fun doTestVue(text: String) {
    doTest(text, "test.vue")
  }

  override fun getTestDataPath(): String = PathManager.getHomePath() + "/contrib/vuejs/vuejs-tests/testData/html/parser"

  fun testVueInnerScriptTag() {
    doTestVue("""
      <template>
        <script type="text/template"><div></div></script>
      </template>
    """)
  }

  fun testVueInnerScriptTag2() {
    doTestVue("""
      <template>
        <script type="text/template"><div></script></div>
      </template>
    """)
  }

  private class MockJSRootConfiguration internal constructor(project: Project) : JSRootConfigurationBase(project) {

    override fun storeLanguageLevelAndUpdateCaches(languageLevel: JSLanguageLevel?) {
      if (myState == null) myState = State()
      myState.languageLevel = ObjectUtils.coalesce(languageLevel, JSLanguageLevel.DEFAULT).id
    }
  }
}
