// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang.html

open class Angular17HtmlLexerTest : Angular2HtmlLexerTest() {

  override val templateSyntax: Angular2TemplateSyntax
    get() = Angular2TemplateSyntax.V_17

  fun testSingleCharBlockName() {
    doTest("@i")
  }

  fun testForBlockParens() {
    doTest("""@for ((item of items); track trackingFn(item, compProp)) {{{item}}}""")
  }

  fun testIncompleteParamsClosingParOnly() {
    doTest("""<div>@if)</div>""")
  }

}
