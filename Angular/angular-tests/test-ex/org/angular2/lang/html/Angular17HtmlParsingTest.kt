// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang.html

import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Test

@TestApplication
open class Angular17HtmlParsingTest : Angular2HtmlParsingTest() {

  override val templateSyntax: Angular2TemplateSyntax
    get() = Angular2TemplateSyntax.V_17

  @Test
  fun testIncompleteBlock6() {
    doTestHtml("""
      @switch (user.name) {
          @c
      }
    """.trimIndent())
  }

  @Test
  fun testForBlockParens() {
    doTestHtml("""
      @for ((item of items) ; ) {
      }
    """.trimIndent())
  }

  @Test
  fun testForBlockParens2() {
    doTestHtml("""
      @for (((item of items ff bar 12) dd ) ) ff ; ) {
      }
    """.trimIndent())
  }

  @Test
  fun testForBlockParens3() {
    doTestHtml("""
      @for (((item of items ff bar 12) dd ; ) {
      }
    """.trimIndent())
  }

  @Test
  fun testForBlockParens4() {
    doTestHtml("""
      @for ((item of items); track trackingFn(item, compProp)) {{{item}}}
    """.trimIndent())
  }

  @Test
  fun testEmptyPrimaryExpressionBlock() {
    doTestHtml("""
      @for (; track ; ;) {{{item}}}
    """.trimIndent())
  }

  @Test
  fun testEmptyPrimaryExpressionBlockReparse() {
    doReparseTest("""
      @for (track) {}
    """.trimIndent(), """
      @for (;track) {}
    """.trimIndent())
  }

  @Test
  fun testDeferredBlockTimeLiteral() {
    doTestHtml("""
      @defer{} @placeholder(minimum 12s) {} @loading(after two; minimum 12 ms &&; maximum 1e2hr) {}
    """.trimIndent())
  }

  @Test
  fun testDeferBlockOnTriggers() {
    doTestHtml("""
      @defer(prefetch on viewport; on idle(var); on timer(12ms); on idle var; prefetch when foo == 12; prefetch ;)
    """.trimIndent())
  }

  @Test
  fun testDeferBlockHydrate() {
    doTestHtml("""
      @defer(hydrate on viewport; hydrate when foo = 12; hydrate never; hydrate ;)
    """.trimIndent())
  }

  @Test
  fun testIncompleteParameters() {
    doTestHtml("""
      @if(foo() {

      }
      @else if ( ) {
        
      } @else {
      
      }
    """.trimIndent())
  }

  @Test
  fun testEmptyBlockName() {
    doTestHtml("""
      An empty @ (block) {name}
    """.trimIndent())
  }

  @Test
  fun testTypeOfInsideIf() {
    doTestHtml("""
      @if (typeof value === 'string') {
        {{value.length}}
      } @else {
        {{value}}
      }
    """.trimIndent())
  }

  @Test
  fun testTopLevelBlockTextParsing() {
    doTestHtml("""
      foo
      @if (true) {
        test
        <div></div>
        test
        <div>
      }
      foo
      <div></div>
    """.trimIndent()
    )
  }

  @Test
  fun testNestedBlockTextParsing() {
    doTestHtml("""
      foo
      <div>
        @if (true) {
          test
          <div></div>
          test
          <div>
        }
        foo
        <div></div>
      </div>
      foo  
    """.trimIndent()
    )
  }

  @Test
  fun testSwitchBlock() {
    doTestHtml("""
      @switch (foo) {
        @case ("foo1")
        @case ("foo2") {
          <div></div>
        }
        @case ("foo3")
        @case ("foo4")
        @case ("foo5") {
          <div></div>
        }
        @case ("foo6")
        @default {
          <div></div>
        }
      }
      @case ("foo7")
      @else {
        <div></div>
      }
      @case ("foo8")
      
      @switch(foo) {
        @default
        @case (foo) {
        }
      }
      @switch(foo) {
        @default
        @default {
        }
      }
    """.trimIndent())
  }

  @Test
  fun testSwitchBlockExhaustive() {
    doTestHtml("""
      @switch (foo) {
        @case ("a") {
        }
        @default never;
      }
      @switch (foo) {
        @case ("a") {
        }
        @default never(foo.bar);
      }
      @switch (foo) {
        @case ("a") {
        }
        @default never
      }
    """.trimIndent())
  }

}
