// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package org.jetbrains.vuejs.language

import com.intellij.codeInsight.CodeInsightSettings
import com.intellij.lang.html.HTMLLanguage
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.application.PathManager
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.DebugUtil
import com.intellij.testFramework.ParsingTestCase
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase
import junit.framework.TestCase
import org.jetbrains.vuejs.VueLanguage

class VueInjectionTest : LightPlatformCodeInsightFixtureTestCase() {
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
      [[ 1 + <caret>2 ]
    </div>
</template>
<script>
new Vue({
  delimiters: ['[[', ']']
})
</script>
""")
    TestCase.assertEquals(VueJSLanguage.INSTANCE, myFixture.file.language)
  }

  fun testCustomDelimitersInterpolationInVueVariant() {
    myFixture.configureByText("CustomDelimitersInterpolationInVue.vue", """<template>
    <div>
      <% 1 + <caret>2 %>
    </div>
</template>
<script>
new Vue({
  delimiters: ['<%', '%>']
})
</script>
""")
    TestCase.assertEquals(VueJSLanguage.INSTANCE, myFixture.file.language)
  }

  fun testCustomDelimitersInterpolationInVueVariantJS() {
    myFixture.configureByText("CustomDelimitersInterpolationInVueVariantJS.js", """
new Vue({
  delimiters: ['<%', '%>']
})
""")
    myFixture.configureByText("CustomDelimitersInterpolationInVue.vue", """<template>
    <div>
      <% 1 + <caret>2 %>
    </div>
</template>
""")
    TestCase.assertEquals(VueJSLanguage.INSTANCE, myFixture.file.language)
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
  }

  fun testCustomDelimitersInterpolationInHtml() {
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
  }

  fun testCustomDelimitersOldDoNotWorkInHtml() {
    myFixture.configureByText("CustomDelimitersOldDoNotWorkInHtml.html", """<!DOCTYPE html>
<html>
  <head>
    <meta charset="utf-8">
    <title>vue-app-gen</title>
  </head>
  <body>
    <div>
      $ 1 + 2 $
      {{<caret>1 + 2}}
    </div>
  </body>
<script>
new Vue({
  delimiters: ['$', '$']
})
</script>
</html>""")
    TestCase.assertEquals(HTMLLanguage.INSTANCE, myFixture.file.language)
  }

  fun testMultipleInterpolationsInVue() {
    val text = "This is {{<caret>interpolation}} in text, and {{another}}{{two}}"
    myFixture.configureByText("MultipleInterpolationsInVue.vue", "<template>${text}</template>")
    TestCase.assertEquals(VueJSLanguage.INSTANCE, myFixture.file.language)
    val host = com.intellij.psi.impl.source.tree.injected.InjectedLanguageUtil.findInjectionHost(myFixture.file.virtualFile)
    TestCase.assertNotNull(host)
    TestCase.assertEquals(text.replace("<caret>", ""), host!!.text)
    val expected = mutableSetOf("interpolation", "another", "two")
    InjectedLanguageManager.getInstance(project).enumerate(host, { injectedPsi, _ -> expected.remove(injectedPsi.text) })
    TestCase.assertEquals(emptySet<String>(), expected)
  }

  fun testMultipleInterpolationsWithNewLinesInVue() {
    val text = """
This is {{<caret>interpolation

}} in text, and {{
another}}{{two}}"""
    myFixture.configureByText("MultipleInterpolationsWithNewLinesInVue.vue", "<template>${text}</template>")
    TestCase.assertEquals(VueJSLanguage.INSTANCE, myFixture.file.language)
    val host = com.intellij.psi.impl.source.tree.injected.InjectedLanguageUtil.findInjectionHost(myFixture.file.virtualFile)
    TestCase.assertNotNull(host)
    TestCase.assertEquals(text.replace("<caret>", ""), host!!.text)
    val expected = mutableSetOf(
      """interpolation

""", """
another""", "two")
    InjectedLanguageManager.getInstance(project).enumerate(host, { injectedPsi, _ -> expected.remove(injectedPsi.text) })
    TestCase.assertEquals(emptySet<String>(), expected)
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
    ParsingTestCase.doCheckResult(testDataPath, getTestName(false) + "." + "txt", toParseTreeText(myFixture.file))
  }

  fun testPugAttrValueInjection() {
    myFixture.configureByFile(getTestName(false) + ".vue")
    ParsingTestCase.doCheckResult(testDataPath, getTestName(false) + "." + "txt", toParseTreeText(myFixture.file))
  }

  private fun toParseTreeText(file: PsiFile): String {
    return DebugUtil.psiToString(file, false, false, { psiElement, consumer ->
      InjectedLanguageManager.getInstance(project).enumerate(psiElement) { injectedPsi, _ -> consumer.consume(injectedPsi) }
    })
  }
}
