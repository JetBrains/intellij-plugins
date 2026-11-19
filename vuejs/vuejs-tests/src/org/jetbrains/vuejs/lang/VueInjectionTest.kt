// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang

import com.intellij.codeInsight.CodeInsightSettings
import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionContributorEP
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.lang.Language
import com.intellij.lang.html.HTMLLanguage
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.javascript.JSTestUtils
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.EDT
import com.intellij.openapi.extensions.DefaultPluginDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.polySymbols.context.PolyContext
import com.intellij.polySymbols.context.PolyContextProvider
import com.intellij.polySymbols.context.impl.PolyContextProviderExtensionPoint
import com.intellij.polySymbols.framework.PolySymbolFramework.Companion.KIND_FRAMEWORK
import com.intellij.polySymbols.testFramework.hybridFixture
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.DebugUtil
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.util.ui.UIUtil
import com.intellij.webcore.libraries.ScriptingLibraryModel
import kotlinx.coroutines.Dispatchers
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.vuejs.context.isVueContext
import org.jetbrains.vuejs.lang.expr.VueJSLanguage
import org.jetbrains.vuejs.lang.html.VueLanguage
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.minutes

@TestApplication
class VueInjectionTest {
  private val hybrid by hybridFixture(getVueTestDataPath() + "/injection/")
  private val myFixture get() = hybrid.fixture
  private val project get() = myFixture.project
  private val testRootDisposable get() = myFixture.testRootDisposable
  private val testDataPath get() = myFixture.testDataPath

  private var oldAutoComplete = false

  @BeforeEach
  fun setUp() {
    val settings = CodeInsightSettings.getInstance()
    oldAutoComplete = settings.AUTOCOMPLETE_ON_CODE_COMPLETION
    settings.AUTOCOMPLETE_ON_CODE_COMPLETION = false
    myFixture.configureByText("a.vue", "")  // to enable vue in the project
  }

  @AfterEach
  fun tearDown() {
    CodeInsightSettings.getInstance().AUTOCOMPLETE_ON_CODE_COMPLETION = oldAutoComplete
  }

  @Test
  fun testSimpleInterpolationInHtml(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureVueDependencies()
    myFixture.configureByText("SimpleInterpolationInHtml.html", """<!DOCTYPE html>
<html>
  <head>
    <meta charset="utf-8">
    <title>vue-app-gen</title>
  </head>
  <body>
    <div>
      {{ 1 + <caret>2 }}
    </div>
  </body>
</html>""")
    assertEquals(VueJSLanguage, myFixture.file.language)
  }

  @Test
  fun testSimpleInterpolationInVue(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureVueDependencies()
    myFixture.configureByText("SimpleInterpolationInVue.vue", """<template>
    <div>
      {{ 1 + <caret>2 }}
    </div>
</template>""")
    assertEquals(VueJSLanguage, myFixture.file.language)
  }

  @Test
  fun testCustomDelimitersInterpolationInVue(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureByText("CustomDelimitersInterpolationInVue.vue", """<template>
    <div>
      [[ 1 + 2 ]]
    </div>
</template>
<script>
new Vue({
  delimiters: ['[[', ']]']
})
</script>
""")
    checkParseTree()
  }

  @Test
  fun testCustomDelimitersInterpolationInVueVariant(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureByText("CustomDelimitersInterpolationInVue.vue", """<template>
    <div>
      <% 1 +2 %>
    </div>
</template>
<script>
new Vue({
  delimiters: ['<%', '%>']
})
</script>
""")
    checkParseTree()
  }

  @Test
  fun testCustomDelimitersInterpolationInVueVariantJS(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureByText("CustomDelimitersInterpolationInVueVariantJS.js", """
new Vue({
  delimiters: ['<%', '%>']
})
""")
    myFixture.configureByText("CustomDelimitersInterpolationInVue.vue", """<template>
    <div>
      <% 1 + 2 %>
    </div>
</template>
""")
    checkParseTree()
  }

  @Test
  fun testNoCommentDelimitersInterpolationInVue(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureByText("CustomDelimitersInterpolationInVue.vue", """<template>
    <div>
      [[ 1 + <caret>2 ]
    </div>
</template>
<script>
new Vue({
  delimiters: ['//', '//']
})
</script>
""")
    assertEquals(VueLanguage, myFixture.file.language)
    checkParseTree()
  }

  @Test
  fun testCustomDelimitersInterpolationInHtml(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureVueDependencies()
    myFixture.configureByText("CustomDelimitersInterpolationInHtml.html", """<!DOCTYPE html>
<html>
  <head>
    <meta charset="utf-8">
    <title>vue-app-gen</title>
  </head>
  <body>
    <div>
      $ 1 + <caret>2 $
    </div>
  </body>
<script>
new Vue({
  delimiters: ['$', '$']
})
</script>
</html>""")
    assertEquals(VueJSLanguage, myFixture.file.language)
  }

  @Test
  fun testCommentIntersectsDelimitersInterpolationInHtml(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureByText("CommentIntersectsDelimitersInterpolationInHtml.html", """<!DOCTYPE html>
<html>
  <head>
    <meta charset="utf-8">
    <title>vue-app-gen</title>
  </head>
  <body>
    <div>
      {{ <!-- <caret>readline + 1}} -->
    </div>
  </body>
</html>""")
    assertEquals(HTMLLanguage.INSTANCE, myFixture.file.language)
    checkParseTree()
  }

  @Test
  fun testCustomDelimitersOldDoNotWorkInHtml(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureVueDependencies()
    myFixture.configureByText("CustomDelimitersOldDoNotWorkInHtml.html", """<!DOCTYPE html>
<html>
  <head>
    <meta charset="utf-8">
    <title>vue-app-gen</title>
  </head>
  <body>
    <div>
      $ 1 + 2 $
      {{<caret>3 + 4}}
    </div>
  </body>
<script>
new Vue({
  delimiters: ['$', '$']
})
</script>
</html>""")
    assertEquals(VueLanguage, myFixture.file.language)
    checkParseTree()
  }

  @Test
  fun testMultipleInterpolationsInVue(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    val text = "This is {{interpolation}} in text, and {{another}}{{two}}"
    myFixture.configureByText("MultipleInterpolationsInVue.vue", "<template>${text}</template>")
    checkParseTree()
  }

  @Test
  fun testMultipleInterpolationsWithNewLinesInVue(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    val text = """
This is {{interpolation

}} in text, and {{
another}}{{two}}"""
    myFixture.configureByText("MultipleInterpolationsWithNewLinesInVue.vue", "<template>${text}</template>")
    checkParseTree()
  }

  @Test
  fun testInjectionByConfigDelimitersAssignmentJS(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureByText("InjectionByConfigDelimitersAssignmentJS.js", "Vue.config.delimiters = ['<%', '%>']")
    myFixture.configureByText("InjectionByConfigDelimitersAssignmentJS.vue", "<template><% <caret> %></template>")
    assertEquals(VueJSLanguage, myFixture.file.language)
  }

  @Test
  fun testInjectionByOptionsDelimitersAssignmentJS(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureByText("InjectionByConfigDelimitersAssignmentJS.js", "Vue.options.delimiters = ['<%', '%>']")
    myFixture.configureByText("InjectionByConfigDelimitersAssignmentJS.vue", "<template><% <caret> %></template>")
    assertEquals(VueJSLanguage, myFixture.file.language)
  }

  @Test
  fun testInjectionByConfigDelimitersAssignment(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureByText("InjectionByConfigDelimitersAssignment.vue", """
<template><% <caret> %></template>
<script>
Vue.config.delimiters = ['<%', '%>']
</script>
""")
    assertEquals(VueJSLanguage, myFixture.file.language)
  }

  @Test
  fun testInjectionByOptionsDelimitersAssignment(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureByText("InjectionByConfigDelimitersAssignment.vue", """
<template><% <caret> %></template>
<script>
Vue.options.delimiters = ['<%', '%>']
</script>
""")
    assertEquals(VueJSLanguage, myFixture.file.language)
  }

  @Test
  fun testAttrValueInjection(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureByFile(hybrid.getTestName(false) + ".vue")
    checkParseTree()
  }

  @Test
  fun testPugAttrValueInjection(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureByFile(hybrid.getTestName(false) + ".vue")
    checkParseTree()
  }

  @Test
  fun testNoInjectionInXml(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureByFile(hybrid.getTestName(false) + ".xml")
    checkParseTree()
  }

  @Test
  fun testI18nTag(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureByFile(hybrid.getTestName(false) + ".vue")
    checkParseTree()
  }

  @Test
  fun testStubbedAttribute(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureByFile(hybrid.getTestName(false) + ".vue")
    checkParseTree()
  }

  @Test
  fun testTemplateInjectionVueLib(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureByFile(hybrid.getTestName(false) + ".js")
    checkParseTree(".off")
    JSTestUtils.addJsLibrary(project, "vue", myFixture.testRootDisposable, null,
                             myFixture.tempDirFixture.createFile("foo.js"), null, ScriptingLibraryModel.LibraryLevel.GLOBAL)
    checkParseTree(".on")
  }

  @Test
  fun testForbiddenVueContext(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureVueDependencies()

    myFixture.configureByFile(hybrid.getTestName(false) + ".html")
    checkParseTree(".off")

    val disposable = Disposer.newDisposable()
    var forbid = true
    PolyContext.POLY_SYMBOLS_CONTEXT_EP
      .point
      ?.registerExtension(
        PolyContextProviderExtensionPoint(
          KIND_FRAMEWORK,
          "vue",
          object : PolyContextProvider {
            override fun isForbidden(
              contextFile: VirtualFile,
              project: Project,
            ): Boolean = forbid
          }), disposable)
    try {
      // Force reload of roots
      isVueContext(myFixture.file)
      UIUtil.dispatchAllInvocationEvents()

      checkParseTree(".on")

      forbid = false
      // Force reload of roots
      isVueContext(myFixture.file)
      UIUtil.dispatchAllInvocationEvents()

      checkParseTree(".off")
    }
    finally {
      Disposer.dispose(disposable)
    }
  }

  class InjectedYamlCompletionContributor : CompletionContributor() {
    override fun fillCompletionVariants(
      parameters: com.intellij.codeInsight.completion.CompletionParameters,
      result: com.intellij.codeInsight.completion.CompletionResultSet,
    ) {
      if (parameters.originalFile.language == Language.findLanguageByID("yaml")) {
        result.consume(LookupElementBuilder.create("keep-alive"))
      }
    }
  }

  @Test
  fun testCompletionInsideInjectedI18NContents(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    ApplicationManager.getApplication().extensionArea
      .getExtensionPoint(CompletionContributor.EP)
      .registerExtension(
        CompletionContributorEP(
          "yaml",
          InjectedYamlCompletionContributor::class.java.name,
          DefaultPluginDescriptor("testVuePluginDescriptor")),
        testRootDisposable
      )

    myFixture.configureVueDependencies("vue-i18n" to "*")
    myFixture.configureByText("Test.vue", """
      <i18n lang="yaml">
      keep-a<caret>
      </i18n>
    """.trimIndent())
    myFixture.completeBasic()
    // Check that completion actually works without throwing exceptions.
    // It should suggest the element from the test YAML contributor
    assertThat(myFixture.lookupElementStrings!!).contains("keep-alive")
  }

  @Test
  fun testTypingInI18NTag(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureByFile(hybrid.getTestName(false) + ".vue")
    myFixture.type(':')
    myFixture.type('"')
    checkParseTree()
  }

  @Test
  fun testTypingLangAttr(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureByFile(hybrid.getTestName(false) + ".vue")
    toParseTreeText(myFixture.file) // expands lazy tokens deeply
    myFixture.type("\b\bts") // js -> ts
    PsiDocumentManager.getInstance(project).commitAllDocuments()
    checkParseTree()
  }

  private fun checkParseTree(suffix: String = "") {
    PlatformTestUtil.assertSameLinesWithFile(testDataPath + "/" + hybrid.getTestName(false) + "${suffix}.txt",
                                             toParseTreeText(myFixture.file))
  }

  private fun toParseTreeText(file: PsiFile): String {
    return DebugUtil.psiToString(file, true, false) { psiElement, consumer ->
      InjectedLanguageManager.getInstance(project).enumerate(psiElement) { injectedPsi, _ -> consumer.consume(injectedPsi) }
    }
  }
}
