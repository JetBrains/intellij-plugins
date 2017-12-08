package org.jetbrains.vuejs.language

import com.intellij.lexer.BaseHtmlLexer
import com.intellij.lexer.Lexer

class VueLangAttributeHandler : BaseHtmlLexer.TokenHandler {
  override fun handleElement(lexer: Lexer) {
    val handled = lexer as VueHandledLexer
    val seenScript = handled.seenScript()
    val seenTemplate = handled.seenTemplate()
    val seenStyle = handled.seenStyle()
    if (!handled.seenTag() && !handled.inTagState()) {
      if (seenScript || seenTemplate) {
        if ("lang" == lexer.tokenText) {
          handled.setSeenScriptType()
          handled.setSeenScript()
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
