package org.jetbrains.vuejs.language

import com.intellij.lang.html.HTMLLanguage
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase
import junit.framework.TestCase
import org.jetbrains.vuejs.VueLanguage

/**
 * @author Irina.Chernushina on 7/21/2017.
 */
class VueInjectionTest : LightPlatformCodeInsightFixtureTestCase() {
  override fun setUp() {
    super.setUp()
    myFixture.configureByText("a.vue", "")  // to enable vue in the project
  }

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
      * 1 + <caret>2 *
    </div>
  </body>
<script>
new Vue({
  delimiters: ['*', '*']
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
      * 1 + 2 *
      {{<caret>1 + 2}}
    </div>
  </body>
<script>
new Vue({
  delimiters: ['*', '*']
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
    com.intellij.psi.impl.source.tree.injected.InjectedLanguageUtil.enumerate(host,
                                                                              { injectedPsi, places -> expected.remove(injectedPsi.text) })
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
    com.intellij.psi.impl.source.tree.injected.InjectedLanguageUtil.enumerate(host,
                                                                              { injectedPsi, places -> expected.remove(injectedPsi.text) })
    TestCase.assertEquals(emptySet<String>(), expected)
  }
}