package org.jetbrains.vuejs.language

import com.intellij.lexer.BaseHtmlLexer
import com.intellij.lexer.Lexer

class VueAttributesHandler : BaseHtmlLexer.TokenHandler{
  override fun handleElement(lexer: Lexer?) {
    val handled = lexer as VueHandledLexer
    if (!handled.inTagState()) {
      val text = lexer.tokenText
      handled.setSeenVueAttribute(text.startsWith(":") || text.startsWith("@") || text.startsWith("v-"))
    }
  }
}