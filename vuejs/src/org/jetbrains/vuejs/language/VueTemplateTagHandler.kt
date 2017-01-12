package org.jetbrains.vuejs.language

import com.intellij.lexer.BaseHtmlLexer
import com.intellij.lexer.Lexer

class VueTemplateTagHandler : BaseHtmlLexer.TokenHandler {
  companion object {
    val SEEN_TEMPLATE:Int = 0x1000
  }

  override fun handleElement(lexer: Lexer?) {
    val handled = lexer as VueHandledLexer
    if (!handled.seenAttribute() && !handled.seenTag() && "template" == lexer.tokenText) {
      handled.setSeenTemplate(true)
    }
  }
}