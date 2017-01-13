package org.jetbrains.vuejs.language

import com.intellij.lexer.BaseHtmlLexer
import com.intellij.lexer.Lexer

class VueTemplateTagHandler : BaseHtmlLexer.TokenHandler {
  companion object {
    val SEEN_TEMPLATE:Int = 0x2000
  }

  override fun handleElement(lexer: Lexer?) {
    val handled = lexer as VueHandledLexer
    if (!handled.seenTag() && handled.inTagState() && "template" == lexer.tokenText) {
      handled.setSeenTemplate(true)
    }
  }
}