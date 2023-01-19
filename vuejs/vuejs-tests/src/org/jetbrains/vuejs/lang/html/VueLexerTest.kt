// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.html

import com.intellij.lang.javascript.dialects.JSLanguageLevel
import com.intellij.lexer.Lexer
import com.intellij.testFramework.LexerTestCase
import com.intellij.testFramework.LightProjectDescriptor
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import org.jetbrains.annotations.NonNls
import org.jetbrains.vuejs.lang.getVueTestDataPath
import org.jetbrains.vuejs.lang.html.parser.VueParserDefinition
import kotlin.properties.Delegates.notNull

open class VueLexerTest : LexerTestCase() {
  private var fixture: IdeaProjectTestFixture by notNull()

  protected var interpolationConfig: Pair<String, String>? = null

  override fun createLexer(): Lexer = VueParserDefinition.createLexer(fixture.project, interpolationConfig)

  override fun getDirPath() = "html/lexer"

  override fun getPathToTestDataFile(extension: String?): String = getVueTestDataPath() + "/$dirPath/" + getTestName(true) + extension

  override fun setUp() {
    super.setUp()

    // needed for various XML extension points registration
    fixture = IdeaTestFixtureFactory.getFixtureFactory()
      .createLightFixtureBuilder(LightProjectDescriptor.EMPTY_PROJECT_DESCRIPTOR, getTestName(false)).fixture
    fixture.setUp()
  }

  override fun tearDown() {
    try {
      fixture.tearDown()
    }
    catch (e: Throwable) {
      addSuppressedException(e)
    }
    finally {
      super.tearDown()
    }
  }

  fun testScriptBlank() = doTestWithoutInterpolations("""
    |<script>
    |</script>
  """)

  fun testScriptEmptyNested() = doTestWithoutInterpolations("""
    |<div :foo='something()'>
    |  <script></script>
    |  <div :foo='something()'>
    |  </div>
    |</div>
  """)

  fun testScriptLangTemplate() {
    doTest("""
      |<script lang="template">
      |  <div v-if="true"></div>
      |</script>
    """, true)
  }

  fun testScriptLangEmpty() {
    doTest("""
      |<script lang=''><</script>
    """, true)
  }

  fun testScriptLangBoolean() {
    doTest("""
      |<script lang><</script>
    """, true)
  }

  fun testScriptLangMissing() {
    doTest("""
      |<script><</script>
    """, true)
  }

  fun testScriptTS() = doTest("""
    |<script lang="ts">
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

  fun testScriptES6() = doTest("""
    |<script lang="typescript">
    | (() => {})();
    |</script>
  """)

  fun testTemplateHtml() = doTest("""
    |<template>
    |  <h2>{{title}}</h2>
    |</template>
  """)

  fun testBoundAttributes() = doTest("""
    |<template>
    | <a :src=bla() @click='event()'></a>
    |</template>
  """)

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

  fun testScriptSrc() = doTest("""
    |<template>
    | <script src="">var i</script>
    | foo
    |</template>
  """)

  fun testScript() = doTest("""
    |<template>
    | <script>var i</script>
    | foo
    |</template>
  """)

  fun testScriptVueEvent() = doTest("""
    |<template>
    | <script @foo="">var i</script>
    | foo
    |</template>
  """)

  fun testScriptWithEventAndAngularAttr() = doTest("""
    |<template>
    | <script src="//example.com" onerror="console.log(1)" @error='console.log(1)'onload="console.log(1)" @load='console.log(1)'>
    |   console.log(2)
    | </script>
    | <div></div>
    |</template>
  """)

  fun testStyleTag() = doTest("""
    |<template>
    | <style>
    |   div {
    |   }
    | </style>
    | <div></div>
    |</template>
  """)

  fun testStyleVueEvent() = doTest("""
    |<template>
    | <style @load='disabled=true'>
    |    div {
    |    }
    | </style>
    | <div></div>
    |</template>
  """)

  fun testStyleWithEventAndBinding() = doTest("""
    |<template>
    | <style @load='disabled=true' onload="this.disabled=true" @load='disabled=true'>
    |   div {
    |   }
    | </style>
    | <div></div>
    |</template>
  """)

  fun testStyleAfterBinding() = doTest("""
    |<template>
    | <div :foo style="width: 13px">
    |   <span @click="foo"></span>
    | </div>
    |</template>
  """)

  fun testStyleAfterStyle() = doTest("""
    |<template>
    | <div style style v-foo='bar'>
    |   <span style='width: 13px' @click="foo"></span>
    | </div>
    |</template>
  """)

  fun testBindingAfterStyle() = doTest("""
    |<template>
    | <div style :foo='bar'>
    |  <span style='width: 13px' @click="foo"></span>
    | </div>
    |</template>
  """)

  fun testEmptyDirective() = doTest("""
    |<div v-foo :bar=""></div>
    |<div :foo="some"></div>
  """)

  fun testEmptyHtmlEvent() = doTest("""
    |<div onclick onclick=""></div>
    |<div :bar="some"></div>
  """)


  fun testInterpolation1() {
    doTest("<t a=\"{{v}}\" b=\"s{{m}}e\" c='s{{m//c}}e'>")
  }

  fun testInterpolation2() {
    doTest("""{{ a }}b{{ c // comment }}""".trimIndent())
  }

  fun testMultiLineSingleComment() {
    doTest("""
      |{{ a }}b{{ c // comment
      | + on
      | - multiple
      |lines }}
  """)
  }

  fun testMultiLineComment() {
    doTest("""
      |{{ a }}b{{ c /* comment
      | + on
      | - multiple
      |lines */ }}
  """)
  }

  fun testMultipleInterpolations() {
    doTest("{{test}} !=bbb {{foo() - bar()}}")
  }

  fun testInterpolationIgnored() {
    doTest("<div> this is ignored {{<interpolation> }}")
  }

  fun testInterpolationIgnored2() {
    doTest("this {{ is {{ <ignored/> interpolation }}")
  }

  fun testInterpolationIgnored3() {
    doTest("<div foo=\"This {{ is {{ ignored interpolation\"> }}<a foo=\"{{\">")
  }

  fun testInterpolationIgnored4() {
    doTest("<div foo='This {{ is {{ ignored interpolation'> }}<a foo='{{'>")
  }

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

  fun testCustomInterpolation() {
    testCustomInterpolation(Pair("{%", "%}")) {
      doTest("{{ regular text }} {% custom interpolation %}")
    }
  }

  fun testCustomInterpolation2() {
    testCustomInterpolation(Pair("abcd", "efgh")) {
      doTest("{{ regular text }} abcdcustom interpolationefgh")
    }
  }

  fun testVueInnerScriptTag() {
    doTest("""
      |<template>
      |  <script type="text/x-template" id="foo">
      |    <div :foo="some - script()"></div>
      |  </script>
      |</template>
    """)
  }

  fun testTextarea() {
    doTest("<textarea>with { some } {{wierd}} <stuff> in it</textarea>")
  }

  override fun doTest(@NonNls text: String) {
    doTest(text, false)
  }

  fun doTestWithoutInterpolations(@NonNls text: String) {
    doTest(text, true)
  }

  override fun getExpectedFileExtension(): String {
    return if (interpolationConfig != null)
      ".${interpolationConfig!!.first}.${interpolationConfig!!.second}.txt"
    else
      super.getExpectedFileExtension()
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
      super.doTest(withoutMargin)
      checkCorrectRestart(withoutMargin)
    }
    test()
    if (!skipInterpolationCheck && interpolationConfig == null) {
      testCustomInterpolation(Pair("{{", "}}"), test)
    }
  }
}
