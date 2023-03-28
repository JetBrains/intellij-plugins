// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.html

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
import com.intellij.psi.tree.CustomLanguageASTComparator
import com.intellij.util.ObjectUtils
import org.intellij.plugins.postcss.PostCssEmbeddedTokenTypesProvider
import org.intellij.plugins.postcss.PostCssLanguage
import org.intellij.plugins.postcss.descriptors.PostCssElementDescriptorProvider
import org.intellij.plugins.postcss.parser.PostCssParserDefinition
import org.intellij.plugins.postcss.psi.impl.PostCssTreeElementFactory
import org.jetbrains.vuejs.lang.expr.parser.VueJSParserDefinition
import org.jetbrains.vuejs.lang.html.lexer.VueEmbeddedContentSupport
import org.jetbrains.vuejs.lang.html.parser.VueASTComparator
import org.jetbrains.vuejs.lang.html.parser.VueParserDefinition

class VueParserTest : HtmlParsingTest("", "vue",
                                      VueParserDefinition(),
                                      VueJSParserDefinition(),
                                      HTMLParserDefinition(),
                                      JavascriptParserDefinition(),
                                      CSSParserDefinition(),
                                      PostCssParserDefinition()) {

  override fun setUp() {
    super.setUp()
    addExplicitExtension(CustomLanguageASTComparator.EXTENSION_POINT_NAME, VueLanguage.INSTANCE, VueASTComparator())

    registerExtensions(EmbeddedTokenTypesProvider.EXTENSION_POINT_NAME, EmbeddedTokenTypesProvider::class.java,
                       listOf(CssEmbeddedTokenTypesProvider(), PostCssEmbeddedTokenTypesProvider()))

    addExplicitExtension(LanguageASTFactory.INSTANCE, CSSLanguage.INSTANCE, CssTreeElementFactory())
    addExplicitExtension(LanguageASTFactory.INSTANCE, PostCssLanguage.INSTANCE, PostCssTreeElementFactory())

    registerExtensionPoint(CssElementDescriptorProvider.EP_NAME, CssElementDescriptorProvider::class.java)
    registerExtension(CssElementDescriptorProvider.EP_NAME, CssElementDescriptorProviderImpl())
    registerExtension(CssElementDescriptorProvider.EP_NAME, PostCssElementDescriptorProvider())
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

    HtmlEmbeddedContentSupport.register(application, testRootDisposable, VueEmbeddedContentSupport::class.java,
                                        CssHtmlEmbeddedContentSupport::class.java, JSHtmlEmbeddedContentSupport::class.java)
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

  private fun doTestVue(text: String) {
    doTest(text.trimIndent(), "test.vue")
  }

  override fun doTest(text: String, fileName: String) {
    super.doTest(if (fileName.endsWith(".vue")
                     || fileName.endsWith(VueFileElementType.INJECTED_FILE_SUFFIX)
                     || text.trimStart().startsWith("<!DOCTYPE")
                     || text.trimStart().startsWith("<?"))
                   text
                 else
                   "<template>\n$text\n</template>",
                 if (fileName.endsWith(VueFileElementType.INJECTED_FILE_SUFFIX))
                   fileName
                 else
                   "${fileName.substring(0, fileName.lastIndexOf('.'))}.vue")
  }

  override fun getTestDataPath(): String = PathManager.getHomePath() + "/contrib/vuejs/vuejs-tests/testData/html/parser"

  fun testScriptNoLang() {
    // classes have a different element type between JS & TS contexts
    doTestVue("""
      <script>
      class X {}
      </script>
      <template>
        <div v-if="class {}"></div>
      </template>
    """)
  }

  fun testScriptJs() {
    // classes have a different element type between JS & TS contexts
    doTestVue("""
      <script lang="js">
      class X {}
      </script>
      <template>
        <div v-if="class {}"></div>
      </template>
    """)
  }

  fun testScriptTs() {
    // classes have a different element type between JS & TS contexts
    doTestVue("""
      <script lang="ts">
      class X {}
      </script>
      <template>
        <div v-if="class {}"></div>
      </template>
    """)
  }

  fun testScriptTypo() {
    // classes have a different element type between JS & TS contexts
    doTestVue("""
      <script lang="tss">
      class X {}
      </script>
      <template>
        <div v-if="class {}"></div>
      </template>
    """)
  }

  fun testScriptsWithMixedLanguages() {
    // classes have a different element type between JS & TS contexts
    doTestVue("""
      <script lang="ts">
      export class X {}
      </script>
      <script setup>
      class XS {}
      </script>
      <template>
        <div v-if="class {}"></div>
      </template>
    """)
  }

  fun testScriptTsTemplateFirst() {
    // classes have a different element type between JS & TS contexts
    doTestVue("""
      <template>
        <div v-if="class {}"></div>
      </template>
      <script lang="ts">
      class X {}
      </script>
    """)
  }

  fun testScriptTsNoContent() {
    // classes have a different element type between JS & TS contexts
    doTestVue("""
      <template>
        <div v-if="class {}"></div>
      </template>
      <script lang="ts" />
    """)
  }

  fun testFirstEverScriptInTemplate() {
    // classes have a different element type between JS & TS contexts
    doTestVue("""
      <template>
        <script></script>
        <div v-if="class {}">This is buggy</div>
      </template>
      <script lang="ts">
      export class X {}
      </script>
      <script setup>
      class XS {}
      </script>
    """)
  }

  fun testVueInnerScriptTagTS() {
    // classes have a different element type between JS & TS contexts
    doTestVue("""
      <script lang="ts"></script>
      <template>
        <script type="text/x-template" id="foo">
          <div v-if="class {}"></div>
          <script>class {}</script>
        </script>
      </template>
    """)
  }

  fun testScriptStrangeLang1() {
    doTestVue("""
      <script lang="html">
        <div v-if="true"></div>
      </script>
    """)
  }

  fun testScriptStrangeLang2() {
    doTestVue("""
      <script lang="template">
        <div v-if="true"></div>
      </script>
    """)
  }

  fun testVue2FilterJs() {
    doTestVue("""
      <script lang="js"></script>
      <template>
        <div v-bind:title="1 | fooFilter"></div>
      </template>
    """)
  }

  fun testVue2FilterTs() {
    doTestVue("""
      <script lang="ts"></script>
      <template>
        <div v-bind:title="1 | fooFilter"></div>
      </template>
    """)
  }

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

  fun testVueInnerScriptTag3() {
    doTestVue("""
      <template>
        <script type="text/x-template" id="foo">
          <div :foo="some - script()"></div>
        </script>
      </template>
    """)
  }

  fun testSlotName() {
    doTestVue("""
      <template>
        <slot name='foo'></slot>
      </template>
    """)
  }

  fun testSlotName2() {
    doTestVue("""
      <template>
        <script type="text/x-template" id="foo">
          <slot name='foo'></slot>
        </script>
      </template>
    """)
  }

  fun testPropBinding() {
    doTestVue("""
      <template>
        <a v-bind:href="url"></a>
        <a :href="url"></a>
        <a :href=url></a>
        <a href="https://foo.bar"></a>
      </template>
    """)
  }

  fun testEventBinding() {
    doTestVue("""
      <template>
        <a v-on:click="doSomething"></a>
        <a @click="doSomething"></a>
        <a onclick="doSomething()"></a>
      </template>
    """)
  }

  fun testEmptyBinding() {
    doTestVue("""
      <template>
        <a @click=""></a>
        <a :href=""></a>
      </template>
    """)
  }

  fun testVFor() {
    doTestVue("""
      <template>
        <div v-for="(item, index) in items">
          {{ parentMessage }} - {{ index }} - {{ item.message }}
        </div>
      </template>
    """)
  }

  fun testCustomDirective() {
    doTestVue("""
      <template>
        <a v-foo:smth="{a: 12, b: true}"></a>
      </template>
    """)
  }

  fun testInterpolationParsing() {
    doTest("""
      {{foo.bar}}
      <div v-bind:foo="{{bar}}" foo="{{bar}}" foo2='{{bar}}'>
        {{foo.bar}}
      </div>
    """.trimIndent(), "test.{{.}}${VueFileElementType.INJECTED_FILE_SUFFIX}")
  }

  fun testCustomInterpolationParsing() {
    doTest("""
      {%foo.bar%}
      <div v-bind:foo="{{bar}}" foo="{{bar}}" foo2='{%bar%}'>
        {{foo.bar}}
      </div>
    """.trimIndent(), "test.{%.%}${VueFileElementType.INJECTED_FILE_SUFFIX}")
  }

  fun testSrcAttribute() {
    doTestVue("""
      <template src="foo"></template>
      <script src="foo"></script>
      <style src="foo"></style>
      <img src="foo">
      <template>
        <template src="foo"></template>
        <script src="foo"></script>
        <style src="foo"></style>
        <img src="foo">
      </template>
    """.trimIndent())
  }

  fun testLangAttribute() {
    doTestVue("""
      <template><div lang="ts"><span></span></div></template>
      <template lang="html"><div lang="ts"><span></span></div></template>
    """.trimIndent())
  }

  fun testDivNestedUnderP() {
    doTestVue("<template><p><div></div></p></template>")
  }

  fun testLangReparse() {
    val baseText = """
      <script lang="js">
      export class X1 {}
      </script>

      <script setup>
      class X2 {}
      </script>
      
      <template>
        <div v-text="class {}"></div>
      </template>
    """.trimIndent()
    val changedText = baseText.replace("js", "ts")

    val file = createFile("test.vue", baseText)
    val fileAfter = createFile("test.vue", changedText)

    val psiToStringDefault = DebugUtil.psiToString(fileAfter, true, false)
    DebugUtil.performPsiModification<RuntimeException>("ensureCorrectReparse") {
      val fileText = file.text
      val diffLog = BlockSupportImpl().reparseRange(
        file,
        file.node,
        TextRange.allOf(fileText),
        fileAfter.text,
        EmptyProgressIndicator(),
        fileText)
      diffLog.performActualPsiChange(file)
    }
    assertEquals(psiToStringDefault, DebugUtil.psiToString(file, true, false))
  }

  private class MockJSRootConfiguration constructor(project: Project) : JSRootConfigurationBase(project) {

    override fun storeLanguageLevelAndUpdateCaches(languageLevel: JSLanguageLevel?) {
      if (myState == null) myState = State()
      myState.languageLevel = ObjectUtils.coalesce(languageLevel, JSLanguageLevel.DEFAULT).id
    }
  }
}