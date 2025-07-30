package org.angular2.lang.expr

import org.angular2.lang.html.Angular2TemplateSyntax

class Angular20LexerTest : Angular2LexerTest() {

  override val templateSyntax: Angular2TemplateSyntax
    get() = Angular2TemplateSyntax.V_20
}