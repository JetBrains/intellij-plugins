package org.jetbrains.vuejs.language

import com.intellij.lexer.BaseHtmlLexer
import com.intellij.lexer.Lexer

class VueTemplateCleaner : BaseHtmlLexer.TokenHandler {
  override fun handleElement(lexer: Lexer?) {
    (lexer as VueHandledLexer).setSeenTemplate(false)
  }
}