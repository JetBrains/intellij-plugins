// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang.html

open class Angular17HtmlParsingTest : Angular2HtmlParsingTest() {

  override val templateSyntax: Angular2TemplateSyntax
    get() = Angular2TemplateSyntax.V_17

  fun testIncompleteBlock6() {
    doTestHtml("""
      @switch (user.name) {
          @c
      }
    """.trimIndent())
  }

  fun testForBlockParens() {
    doTestHtml("""
      @for ((item of items) ; ) {
      }
    """.trimIndent())
  }

  fun testForBlockParens2() {
    doTestHtml("""
      @for (((item of items ff bar 12) dd ) ) ff ; ) {
      }
    """.trimIndent())
  }

  fun testForBlockParens3() {
    doTestHtml("""
      @for (((item of items ff bar 12) dd ; ) {
      }
    """.trimIndent())
  }

  fun testForBlockParens4() {
    doTestHtml("""
      @for ((item of items); track trackingFn(item, compProp)) {{{item}}}
    """.trimIndent())
  }

  fun testEmptyPrimaryExpressionBlock() {
    doTestHtml("""
      @for (; track ; ;) {{{item}}}
    """.trimIndent())
  }

  fun testEmptyPrimaryExpressionBlockReparse() {
    doReparseTest("""
      @for (track) {}
    """.trimIndent(), """
      @for (;track) {}
    """.trimIndent())
  }

  fun testDeferredBlockTimeLiteral() {
    doTestHtml("""
      @defer{} @placeholder(minimum 12s) {} @loading(after two; minimum 12 ms &&; maximum 1e2hr) {}
    """.trimIndent())
  }

  fun testDeferBlockOnTriggers() {
    doTestHtml("""
      @defer(prefetch on viewport; on idle(var); on timer(12ms); on idle var; prefetch when foo == 12; prefetch ;)
    """.trimIndent())
  }

  fun testDeferBlockHydrate() {
    doTestHtml("""
      @defer(hydrate on viewport; hydrate when foo = 12; hydrate never; hydrate ;)
    """.trimIndent())
  }

  fun testIncompleteParameters() {
    doTestHtml("""
      @if(foo() {

      }
      @else if ( ) {
        
      } @else {
      
      }
    """.trimIndent())
  }

  fun testEmptyBlockName() {
    doTestHtml("""
      An empty @ (block) {name}
    """.trimIndent())
  }

  fun testTypeOfInsideIf() {
    doTestHtml("""
      @if (typeof value === 'string') {
        {{value.length}}
      } @else {
        {{value}}
      }
    """.trimIndent())
  }

}
