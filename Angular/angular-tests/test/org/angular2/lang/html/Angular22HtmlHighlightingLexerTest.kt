// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang.html

open class Angular22HtmlHighlightingLexerTest : Angular2HtmlHighlightingLexerTest() {

  override val templateSyntax: Angular2TemplateSyntax
    get() = Angular2TemplateSyntax.V_22

}
