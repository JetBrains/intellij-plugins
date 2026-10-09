// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.html

import com.intellij.lexer.Lexer
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.lexerFixture
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.projectFixture
import org.jetbrains.annotations.NonNls
import org.jetbrains.vuejs.lang.getVueTestDataPath
import org.jetbrains.vuejs.lang.html.parser.VueParserDefinition
import org.junit.jupiter.api.Test

// The test application is needed for various XML extension points registration
@TestApplication
open class VueLexerTest {
  companion object {
    private val projectFixture = projectFixture()
  }

  protected var interpolationConfig: Pair<String, String>? = null

  protected open val dirPath: String
    get() = "html/lexer"

  private val lexer by lexerFixture(getVueTestDataPath(), checkRestart = false) { createLexer() }

  protected open fun createLexer(): Lexer = VueParserDefinition.Util.createLexer(projectFixture.get(), interpolationConfig, false)

  @Test
  fun testEmptyFile() = doTestWithoutInterpolations("")

  @Test
  fun testScriptBlank() = doTestWithoutInterpolations("""
    |<script>
    |</script>
  """)

  @Test
  fun testScriptEmptyNested() = doTestWithoutInterpolations("""
    |<div :foo='something()'>
    |  <script></script>
    |  <div :foo='something()'>
    |  </div>
    |</div>
  """)

  @Test
  fun testScriptLangTemplate() {
    doTest("""
      |<script lang="template">
      |  <div v-if="true"></div>
      |</script>
    """, true)
  }

  @Test
  fun testScriptLangEmpty() {
    doTest("""
      |<script lang=''><</script>
    """, true)
  }

  @Test
  fun testScriptLangBoolean() {
    doTest("""
      |<script lang><</script>
    """, true)
  }

  @Test
  fun testScriptLangMissing() {
    doTest("""
      |<script><</script>
    """, true)
  }

  @Test
  fun testScriptTS() = doTest("""
    |<script lang="ts">
    |(() => {})();
    |</script>
  """)

  @Test
  fun testStyleEmpty() = doTest("""
    |<style>
    |</style>
  """)

  @Test
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

  @Test
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

  @Test
  fun testTemplateEmpty() = doTest("""
    |<template>
    |</template>
  """)

  @Test
  fun testTemplateInner() = doTest("""
    |<template>
    |  <template></template>
    |</template>
    |<script>
    |</script>
  """)

  @Test
  fun testTemplateInnerDouble() = doTest("""
    |<template>
    |  <template></template>
    |  <template></template>
    |</template>
    |<script>
    |</script>
  """)

  @Test
  fun testTemplateJade() = doTest("""
    |<template lang="jade">
    |#content
    |  .block
    |    input#bar.foo1.foo2
    |</template>
  """)

  @Test
  fun testTemplateNewLine() = doTest("""
    |<template>
    |    <q-drawer-link>
    |        text
    |    </q-drawer-link>
    |</template>
  """)

  @Test
  fun testBindingAttribute() = doTest("""
    |<template>
    |  <div :bound="{foo: bar}" v-bind:bound="{bar: foo}"></div>
    |</template>
  """)

  @Test
  fun testEventAttribute() = doTest("""
    |<template>
    |  <div @event="{foo: bar}" v-on:event="{bar: foo}"></div>
    |</template>
  """)

  @Test
  fun testHtmlLangTemplate() = doTest("""
    |<template lang="html">
    |  <toggle :item="item"/>
    |</template>
  """)

  @Test
  fun testVFor() = doTest("""
    |<template>
    |  <ul id="example-1">
    |    <li v-for="item in items"/>
    |    <li v-for="(item, key) in items"/>
    |  </ul>
    |</template>
  """)

  @Test
  fun testLangTag() = doTest("""
    |<template>
    |  <lang >inside </lang>
    |</template>
  """)

  @Test
  fun testAttributeValuesEmbedded() = doTest("""
    |<template>
    |  <div v-else class="one two three four" @click="someFun()">5</div>
    |</template>
  """)

  @Test
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

  @Test
  fun testScriptES6() = doTest("""
    |<script lang="typescript">
    | (() => {})();
    |</script>
  """)

  @Test
  fun testTemplateHtml() = doTest("""
    |<template>
    |  <h2>{{title}}</h2>
    |</template>
  """)

  @Test
  fun testBoundAttributes() = doTest("""
    |<template>
    | <a :src=bla() @click='event()'></a>
    |</template>
  """)

  @Test
  fun testComplex() = doTest("""
    |<template>
    |  <div v-for="let contact of value; index as i"
    |    @click="contact"
    |  </div>
    |  
    |  <li v-for="let user of userObservable | async as users; index as i; first as isFirst">
    |    {{i}}/{{users.length}}. {{user}} <span v-if="isFirst">default</span>
    |  </li>
    |  
    |  <tr :style="{'visible': con}" v-for="let contact of contacts; index as i">
    |    <td>{{i + 1}}</td>
    |  </tr>
    |</template>
  """)

  //region Following 3 tests require fixes in JS lexer for html
  @Suppress("TestFunctionName")
  fun _testEscapes() = doTest("""
    |<template>
    | <div :input="'test&quot;test\u1234\u123\n\r\t'">
    | <div :input='"ttt" + &apos;str\u1234ing&apos;'>
    |</template>
  """)

  @Suppress("TestFunctionName")
  fun _testTextInEscapedQuotes() = doTest("""
    |<template>
    | <div [foo]="&quot;test&quot; + 12">
    |</template>
  """)

  @Suppress("TestFunctionName")
  fun _testTextInEscapedApos() = doTest("""
    |<template>
    | <div [foo]="&apos;test&apos; + 12">
    |</template>
  """)
  //endregion

  @Test
  fun testScriptSrc() = doTest("""
    |<template>
    | <script src="">var i</script>
    | foo
    |</template>
  """)

  @Test
  fun testScript() = doTest("""
    |<template>
    | <script>var i</script>
    | foo
    |</template>
  """)

  @Test
  fun testScriptVueEvent() = doTest("""
    |<template>
    | <script @foo="">var i</script>
    | foo
    |</template>
  """)

  @Test
  fun testScriptWithEventAndAngularAttr() = doTest("""
    |<template>
    | <script src="//example.com" onerror="console.log(1)" @error='console.log(1)'onload="console.log(1)" @load='console.log(1)'>
    |   console.log(2)
    | </script>
    | <div></div>
    |</template>
  """)

  @Test
  fun testStyleTag() = doTest("""
    |<template>
    | <style>
    |   div {
    |   }
    | </style>
    | <div></div>
    |</template>
  """)

  @Test
  fun testStyleVueEvent() = doTest("""
    |<template>
    | <style @load='disabled=true'>
    |    div {
    |    }
    | </style>
    | <div></div>
    |</template>
  """)

  @Test
  fun testStyleWithEventAndBinding() = doTest("""
    |<template>
    | <style @load='disabled=true' onload="this.disabled=true" @load='disabled=true'>
    |   div {
    |   }
    | </style>
    | <div></div>
    |</template>
  """)

  @Test
  fun testStyleAfterBinding() = doTest("""
    |<template>
    | <div :foo style="width: 13px">
    |   <span @click="foo"></span>
    | </div>
    |</template>
  """)

  @Test
  fun testStyleAfterStyle() = doTest("""
    |<template>
    | <div style style v-foo='bar'>
    |   <span style='width: 13px' @click="foo"></span>
    | </div>
    |</template>
  """)

  @Test
  fun testBindingAfterStyle() = doTest("""
    |<template>
    | <div style :foo='bar'>
    |  <span style='width: 13px' @click="foo"></span>
    | </div>
    |</template>
  """)

  @Test
  fun testEmptyDirective() = doTest("""
    |<div v-foo :bar=""></div>
    |<div :foo="some"></div>
  """)

  @Test
  fun testEmptyHtmlEvent() = doTest("""
    |<div onclick onclick=""></div>
    |<div :bar="some"></div>
  """)


  @Test
  fun testInterpolation1() {
    doTest("<t a=\"{{v}}\" b=\"s{{m}}e\" c='s{{m//c}}e'>")
  }

  @Test
  fun testInterpolation2() {
    doTest("""{{ a }}b{{ c // comment }}""".trimIndent())
  }

  @Test
  fun testMultiLineSingleComment() {
    doTest("""
      |{{ a }}b{{ c // comment
      | + on
      | - multiple
      |lines }}
  """)
  }

  @Test
  fun testMultiLineComment() {
    doTest("""
      |{{ a }}b{{ c /* comment
      | + on
      | - multiple
      |lines */ }}
  """)
  }

  @Test
  fun testMultipleInterpolations() {
    doTest("{{test}} !=bbb {{foo() - bar()}}")
  }

  @Test
  fun testInterpolationIgnored() {
    doTest("<div> this is ignored {{<interpolation> }}")
  }

  @Test
  fun testInterpolationIgnored2() {
    doTest("this {{ is {{ <ignored/> interpolation }}")
  }

  @Test
  fun testInterpolationIgnored3() {
    doTest("<div foo=\"This {{ is {{ ignored interpolation\"> }}<a foo=\"{{\">")
  }

  @Test
  fun testInterpolationIgnored4() {
    doTest("<div foo='This {{ is {{ ignored interpolation'> }}<a foo='{{'>")
  }

  @Test
  fun testInterpolationEmpty() {
    doTest("{{}}<div foo='{{}}' foo='a{{}}b' bar=\"{{}}\" bar=\"a{{}}b\">{{}}</div>a{{}}b<div>a{{}}b</div>")
  }

  @Suppress("TestFunctionName")
  fun _testInterpolationCharEntityRefs() {
    doTest("&nbsp;{{foo&nbsp;bar}}{{&nbsp;}}<div foo='&nbsp;{{foo&nbsp;bar}}{{&nbsp;}}' bar=\"&nbsp;{{foo&nbsp;bar}}{{&nbsp;}}\">")
  }

  @Suppress("TestFunctionName")
  fun _testInterpolationEntityRefs() {
    doTest("&foo;{{foo&foo;bar}}{{&foo;}}<div foo='&foo;{{foo&foo;bar}}{{&foo;}}' bar=\"&foo;{{foo&foo;bar}}{{&foo;}}\">")
  }

  @Test
  fun testCustomInterpolation() {
    testCustomInterpolation(Pair("{%", "%}")) {
      doTest("{{ regular text }} {% custom interpolation %}")
    }
  }

  @Test
  fun testCustomInterpolation2() {
    testCustomInterpolation(Pair("abcd", "efgh")) {
      doTest("{{ regular text }} abcdcustom interpolationefgh")
    }
  }

  @Test
  fun testVueInnerScriptTag() {
    doTest("""
      |<template>
      |  <script type="text/x-template" id="foo">
      |    <div :foo="some - script()"></div>
      |  </script>
      |</template>
    """)
  }

  @Test
  fun testTextarea() {
    doTest("<textarea>with { some } {{wierd}} <stuff> in it</textarea>")
  }

  @Test
  fun testTitleComponent(){
    doTest("<head><title>This is <std>title</std></title></head><div><Title>This is <custom>title</custom></Title></div>")
  }

  protected fun doTest(@NonNls text: String) {
    doTest(text, false)
  }

  fun doTestWithoutInterpolations(@NonNls text: String) {
    doTest(text, true)
  }

  private fun getExpectedFilePath(): String {
    val extension = if (interpolationConfig != null)
      ".${interpolationConfig!!.first}.${interpolationConfig!!.second}.txt"
    else
      ".txt"
    return "${getVueTestDataPath()}/$dirPath/${lexer.testName}$extension"
  }

  private fun testCustomInterpolation(interpolationConfig: Pair<String, String>?, test: () -> Unit) {
    val old = this.interpolationConfig
    try {
      this.interpolationConfig = interpolationConfig
      test()
    }
    finally {
      this.interpolationConfig = old
    }
  }

  private fun doTest(@NonNls text: String, skipInterpolationCheck: Boolean) {
    val test = {
      val withoutMargin = text.trimMargin()
      PlatformTestUtil.assertSameLinesWithFile(getExpectedFilePath(), lexer.printTokens(withoutMargin, 0))
      lexer.checkCorrectRestart(withoutMargin)
    }
    test()
    if (!skipInterpolationCheck && interpolationConfig == null) {
      testCustomInterpolation(Pair("{{", "}}"), test)
    }
  }
}
