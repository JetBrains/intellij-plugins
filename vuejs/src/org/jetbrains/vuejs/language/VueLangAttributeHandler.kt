package org.jetbrains.vuejs.language

import com.intellij.lexer.BaseHtmlLexer
import com.intellij.lexer.Lexer

class VueLangAttributeHandler : BaseHtmlLexer.TokenHandler {
  override fun handleElement(lexer: Lexer) {
    val handled = lexer as VueHandledLexer
    val seenScript = handled.seenScript()
    val seenStyle = handled.seenStyle()
    if (!handled.seenTag()) {
      if (seenScript) {
        if ("lang" == lexer.tokenText) {
          handled.setSeenScriptType()
        }
      }
      else if (seenStyle) {
        if ("lang" == lexer.tokenText) {
          handled.setSeenStyleType()
        }
      }
    }
  }
}
