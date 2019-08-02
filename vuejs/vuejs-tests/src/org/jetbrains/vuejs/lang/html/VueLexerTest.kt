// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.html

import com.intellij.lang.javascript.dialects.JSLanguageLevel
import com.intellij.lexer.Lexer
import com.intellij.testFramework.LexerTestCase
import com.intellij.testFramework.LightProjectDescriptor
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import org.jetbrains.annotations.NonNls
import org.jetbrains.vuejs.lang.html.lexer.VueLexer

@TestDataPath("\$CONTENT_ROOT/testData/lexer")
open class VueLexerTest : LexerTestCase() {

  private var myFixture: IdeaProjectTestFixture? = null

  override fun setUp() {
    super.setUp()

    // needed for various XML extension points registration
    myFixture = IdeaTestFixtureFactory.getFixtureFactory()
      .createLightFixtureBuilder(LightProjectDescriptor.EMPTY_PROJECT_DESCRIPTOR).fixture
    myFixture!!.setUp()
  }

  override fun tearDown() {
    try {
      myFixture!!.tearDown()
    }
    catch (e: Throwable) {
      addSuppressedException(e)
    }
    finally {
      super.tearDown()
    }
  }

  fun testScriptEmpty() = doTest("""
    |<script>
    |</script>
  """)

  fun testScriptTS() = doTest("""
    |<script lang="typescript">
    |(() => {})();
    |</script>
  """)

  fun testStyleEmpty() = doTest("""
    |<style>
    |</style>
  """)

  fun testStyleSass() = doTest("""
    |<style lang="sass">
    |${'$'}font-stack:    Helvetica, sans-serif
    |${'$'}primary-color: #333
    |
    |body
    |  font: 100% ${'$'}font-stack
    |  color: ${'$'}primary-color
    |</style>
  """)

  fun testStyleSassAfterTemplate() = doTest("""
    |<template>
    |</template>
    |
    |<style lang="sass">
    |${'$'}font-stack:    Helvetica, sans-serif
    |${'$'}primary-color: #333
    |
    |body
    |  font: 100% ${'$'}font-stack
    |  color: ${'$'}primary-color
    |</style>
  """)

  fun testTemplateEmpty() = doTest("""
    |<template>
    |</template>
  """)

  fun testTemplateInner() = doTest("""
    |<template>
    |  <template></template>
    |</template>
    |<script>
    |</script>
  """)

  fun testTemplateInnerDouble() = doTest("""
    |<template>
    |  <template></template>
    |  <template></template>
    |</template>
    |<script>
    |</script>
  """)

  fun testTemplateJade() = doTest("""
    |<template lang="jade">
    |#content
    |  .block
    |    input#bar.foo1.foo2
    |</template>
  """)

  fun testTemplateNewLine() = doTest("""
    |<template>
    |    <q-drawer-link>
    |        text
    |    </q-drawer-link>
    |</template>
  """)

  fun testBindingAttribute() = doTest("""
    |<template>
    |  <div :bound="{foo: bar}" v-bind:bound="{bar: foo}"></div>
    |</template>
  """)

  fun testEventAttribute() = doTest("""
    |<template>
    |  <div @event="{foo: bar}" v-on:event="{bar: foo}"></div>
    |</template>
  """)

  fun testHtmlLangTemplate() = doTest("""
    |<template lang="html">
    |  <toggle :item="item"/>
    |</template>
  """)

  fun testVFor() = doTest("""
    |<template>
    |  <ul id="example-1">
    |    <li v-for="item in items"/>
    |    <li v-for="(item, key) in items"/>
    |  </ul>
    |</template>
  """)

  fun testLangTag() = doTest("""
    |<template>
    |  <lang >inside </lang>
    |</template>
  """)

  fun testAttributeValuesEmbedded() = doTest("""
    |<template>
    |  <div v-else class="one two three four" @click="someFun()">5</div>
    |</template>
  """)

  fun testTsxLang() = doTest("""
    |<script lang="tsx">
    |  let a = 1;
    |  export default {
    |    name: "with-tsx",
    |    render() {
    |      return <div></div>
    |    }
    |  }
    |</script>
  """)

  override fun createLexer(): Lexer = VueLexer(JSLanguageLevel.ES6)

  override fun getDirPath() = "/contrib/vuejs/vuejs-tests/testData/html/lexer"

  override fun doTest(@NonNls text: String) {
    doTest(text, true)
  }

  protected fun doTest(@NonNls text: String, checkRestartOnEveryToken: Boolean) {
    val withoutMargin = text.trimMargin()
    super.doTest(withoutMargin)
    //if (checkRestartOnEveryToken) {
    //  checkCorrectRestartOnEveryToken(text)
    //}
    //else {
    checkCorrectRestart(withoutMargin)
    //}
  }
}
