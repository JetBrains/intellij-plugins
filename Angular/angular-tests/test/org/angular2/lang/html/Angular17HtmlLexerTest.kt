// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang.html

import org.junit.jupiter.api.Test

open class Angular17HtmlLexerTest : Angular2HtmlLexerTest() {

  override val templateSyntax: Angular2TemplateSyntax
    get() = Angular2TemplateSyntax.V_17

  @Test
  fun testSingleCharBlockName() {
    doTest("@i")
  }

  @Test
  fun testEmptyBlockName() {
    doTest("An empty @ block")
  }

  @Test
  fun testEmptyBlockName2() {
    doTest("An empty @ (block) {}")
  }

  @Test
  fun testForBlockParens() {
    doTest("""@for ((item of items); track trackingFn(item, compProp)) {{{item}}}""")
  }

  @Test
  fun testIncompleteParamsClosingParOnly() {
    doTest("""<div>@if)</div>""")
  }

}
