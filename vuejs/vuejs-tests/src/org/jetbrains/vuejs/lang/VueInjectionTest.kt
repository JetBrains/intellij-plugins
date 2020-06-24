// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang

import com.intellij.codeInsight.CodeInsightSettings
import com.intellij.lang.html.HTMLLanguage
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.ModificationTracker
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.DebugUtil
import com.intellij.psi.util.CachedValueProvider
import com.intellij.testFramework.ParsingTestCase
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.ui.UIUtil
import junit.framework.TestCase
import org.jetbrains.vuejs.context.VueContextProvider
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
    CodeInsightSettings.getInstance().AUTOCOMPLETE_ON_CODE_COMPLETION = oldAutoComplete
    super.tearDown()
  }

  override fun getTestDataPath(): String = PathManager.getHomePath() + "/contrib/vuejs/vuejs-tests/testData/injection/"

  fun testSimpleInterpolationInHtml() {
    createPackageJsonWithVueDependency(myFixture)
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
    createPackageJsonWithVueDependency(myFixture)
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
    createPackageJsonWithVueDependency(myFixture)
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
    createPackageJsonWithVueDependency(myFixture)
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

  fun testStubbedAttribute() {
    myFixture.configureByFile(getTestName(false) + ".vue")
    checkParseTree()
  }

  fun testForbiddenVueContext() {
    createPackageJsonWithVueDependency(myFixture)

    myFixture.configureByFile(getTestName(false) + ".html")
    checkParseTree(".off")

    val disposable = Disposer.newDisposable()
    var forbid = true
    VueContextProvider.VUE_CONTEXT_PROVIDER_EP
      .point
      .registerExtension(object : VueContextProvider {
        override fun isVueContext(directory: PsiDirectory): CachedValueProvider.Result<Boolean> {
          return CachedValueProvider.Result.create(false, ModificationTracker.NEVER_CHANGED)
        }

        override fun isVueContextForbidden(contextFile: VirtualFile, project: Project): Boolean {
          return forbid
        }
      }, disposable)
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

  private fun checkParseTree(suffix: String = "") {
    ParsingTestCase.doCheckResult(testDataPath, getTestName(false) + "${suffix}.txt", toParseTreeText(myFixture.file))
  }

  private fun toParseTreeText(file: PsiFile): String {
    return DebugUtil.psiToString(file, false, false) { psiElement, consumer ->
      InjectedLanguageManager.getInstance(project).enumerate(psiElement) { injectedPsi, _ -> consumer.consume(injectedPsi) }
    }
  }
}
