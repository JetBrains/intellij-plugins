package org.jetbrains.vuejs.language

import com.intellij.lexer.BaseHtmlLexer
import com.intellij.lexer.Lexer

class VueTagClosedHandler : BaseHtmlLexer.TokenHandler{
  override fun handleElement(lexer: Lexer?) {
    val handled = lexer as VueHandledLexer
    if (handled.seenTemplate() && handled.seenScript()) {
      handled.setSeenTag(true)
    }
  }
}