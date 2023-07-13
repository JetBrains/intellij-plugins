// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang

import com.intellij.codeInsight.CodeInsightSettings
import com.intellij.lang.html.HTMLLanguage
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.javascript.JSTestUtils
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.DebugUtil
import com.intellij.testFramework.ParsingTestCase
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.ui.UIUtil
import com.intellij.webSymbols.context.WebSymbolsContext
import com.intellij.webSymbols.context.WebSymbolsContext.Companion.KIND_FRAMEWORK
import com.intellij.webSymbols.context.WebSymbolsContextProvider
import com.intellij.webSymbols.context.impl.WebSymbolsContextProviderExtensionPoint
import com.intellij.webcore.libraries.ScriptingLibraryModel
import junit.framework.TestCase
import org.jetbrains.vuejs.context.isVueContext
import org.jetbrains.vuejs.lang.expr.VueJSLanguage
import org.jetbrains.vuejs.lang.html.VueLanguage

class VueInjectionTest : BasePlatformTestCase() {
  private var oldAutoComplete = false

  override fun setUp() {
    super.setUp()
    val settings = CodeInsightSettings.getInstance()
    oldAutoComplete = settings.AUTOCOMPLETE_ON_CODE_COMPLETION
    settings.AUTOCOMPLETE_ON_CODE_COMPLETION = false
    myFixture.configureByText("a.vue", "")  // to enable vue in the project
  }

  override fun tearDown() {
    try {
      CodeInsightSettings.getInstance().AUTOCOMPLETE_ON_CODE_COMPLETION = oldAutoComplete
    }
    catch (e: Throwable) {
      addSuppressedException(e)
    }
    finally {
      super.tearDown()
    }
  }

  override fun getTestDataPath(): String = getVueTestDataPath() + "/injection/"

  fun testSimpleInterpolationInHtml() {
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
    TestCase.assertEquals(VueJSLanguage.INSTANCE, myFixture.file.language)
  }

  fun testSimpleInterpolationInVue() {
    myFixture.configureVueDependencies()
    myFixture.configureByText("SimpleInterpolationInVue.vue", """<template>
    <div>
      {{ 1 + <caret>2 }}
    </div>
</template>""")
    TestCase.assertEquals(VueJSLanguage.INSTANCE, myFixture.file.language)
  }

  fun testCustomDelimitersInterpolationInVue() {
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

  fun testCustomDelimitersInterpolationInVueVariant() {
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

  fun testCustomDelimitersInterpolationInVueVariantJS() {
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

  fun testNoCommentDelimitersInterpolationInVue() {
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
    TestCase.assertEquals(VueLanguage.INSTANCE, myFixture.file.language)
    checkParseTree()
  }

  fun testCustomDelimitersInterpolationInHtml() {
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
    TestCase.assertEquals(VueJSLanguage.INSTANCE, myFixture.file.language)
  }

  fun testCommentIntersectsDelimitersInterpolationInHtml() {
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
    TestCase.assertEquals(HTMLLanguage.INSTANCE, myFixture.file.language)
    checkParseTree()
  }

  fun testCustomDelimitersOldDoNotWorkInHtml() {
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
    TestCase.assertEquals(VueLanguage.INSTANCE, myFixture.file.language)
    checkParseTree()
  }

  fun testMultipleInterpolationsInVue() {
    val text = "This is {{interpolation}} in text, and {{another}}{{two}}"
    myFixture.configureByText("MultipleInterpolationsInVue.vue", "<template>${text}</template>")
    checkParseTree()
  }

  fun testMultipleInterpolationsWithNewLinesInVue() {
    val text = """
This is {{interpolation

}} in text, and {{
another}}{{two}}"""
    myFixture.configureByText("MultipleInterpolationsWithNewLinesInVue.vue", "<template>${text}</template>")
    checkParseTree()
  }

  fun testInjectionByConfigDelimitersAssignmentJS() {
    myFixture.configureByText("InjectionByConfigDelimitersAssignmentJS.js", "Vue.config.delimiters = ['<%', '%>']")
    myFixture.configureByText("InjectionByConfigDelimitersAssignmentJS.vue", "<template><% <caret> %></template>")
    TestCase.assertEquals(VueJSLanguage.INSTANCE, myFixture.file.language)
  }

  fun testInjectionByOptionsDelimitersAssignmentJS() {
    myFixture.configureByText("InjectionByConfigDelimitersAssignmentJS.js", "Vue.options.delimiters = ['<%', '%>']")
    myFixture.configureByText("InjectionByConfigDelimitersAssignmentJS.vue", "<template><% <caret> %></template>")
    TestCase.assertEquals(VueJSLanguage.INSTANCE, myFixture.file.language)
  }

  fun testInjectionByConfigDelimitersAssignment() {
    myFixture.configureByText("InjectionByConfigDelimitersAssignment.vue", """
<template><% <caret> %></template>
<script>
Vue.config.delimiters = ['<%', '%>']
</script>
""")
    TestCase.assertEquals(VueJSLanguage.INSTANCE, myFixture.file.language)
  }

  fun testInjectionByOptionsDelimitersAssignment() {
    myFixture.configureByText("InjectionByConfigDelimitersAssignment.vue", """
<template><% <caret> %></template>
<script>
Vue.options.delimiters = ['<%', '%>']
</script>
""")
    TestCase.assertEquals(VueJSLanguage.INSTANCE, myFixture.file.language)
  }

  fun testAttrValueInjection() {
    myFixture.configureByFile(getTestName(false) + ".vue")
    checkParseTree()
  }

  fun testPugAttrValueInjection() {
    myFixture.configureByFile(getTestName(false) + ".vue")
    checkParseTree()
  }

  fun testNoInjectionInXml() {
    myFixture.configureByFile(getTestName(false) + ".xml")
    checkParseTree()
  }

  fun testI18nTag() {
    myFixture.configureByFile(getTestName(false) + ".vue")
    checkParseTree()
  }

  fun testStubbedAttribute() {
    myFixture.configureByFile(getTestName(false) + ".vue")
    checkParseTree()
  }

  fun testTemplateInjectionVueLib() {
    myFixture.configureByFile(getTestName(false) + ".js")
    checkParseTree(".off")
    JSTestUtils.addJsLibrary(project, "vue", myFixture.testRootDisposable, null,
                             myFixture.tempDirFixture.createFile("foo.js"), null, ScriptingLibraryModel.LibraryLevel.GLOBAL)
    checkParseTree(".on")
  }

  fun testForbiddenVueContext() {
    myFixture.configureVueDependencies()

    myFixture.configureByFile(getTestName(false) + ".html")
    checkParseTree(".off")

    val disposable = Disposer.newDisposable()
    var forbid = true
    WebSymbolsContext.WEB_SYMBOLS_CONTEXT_EP
      .point
      ?.registerExtension(
        WebSymbolsContextProviderExtensionPoint(
          KIND_FRAMEWORK,
          "vue",
          object : WebSymbolsContextProvider {
            override fun isForbidden(contextFile: VirtualFile,
                                     project: Project): Boolean = forbid
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

  fun testCompletionInsideInjectedI18NContents() {
    myFixture.configureVueDependencies("vue-i18n" to "*")
    myFixture.configureByText("Test.vue", """
      <i18n lang="yaml">
      keep-a<caret>
      </i18n>
    """.trimIndent())
    myFixture.completeBasic()
    // non-ideal - here we test that completion actually works without throwing exceptions, but it shouldn't suggest any tags here
    assertContainsElements(myFixture.lookupElementStrings!!, "<keep-alive")
  }

  fun testTypingInI18NTag() {
    myFixture.configureByFile(getTestName(false) + ".vue")
    myFixture.type(':')
    myFixture.type('"')
    checkParseTree()
  }

  fun testTypingLangAttr() {
    myFixture.configureByFile(getTestName(false) + ".vue")
    toParseTreeText(myFixture.file) // expands lazy tokens deeply
    myFixture.type("\b\bts") // js -> ts
    PsiDocumentManager.getInstance(project).commitAllDocuments()
    checkParseTree()
  }

  private fun checkParseTree(suffix: String = "") {
    ParsingTestCase.doCheckResult(testDataPath, getTestName(false) + "${suffix}.txt", toParseTreeText(myFixture.file))
  }

  private fun toParseTreeText(file: PsiFile): String {
    return DebugUtil.psiToString(file, true, false) { psiElement, consumer ->
      InjectedLanguageManager.getInstance(project).enumerate(psiElement) { injectedPsi, _ -> consumer.consume(injectedPsi) }
    }
  }
}
