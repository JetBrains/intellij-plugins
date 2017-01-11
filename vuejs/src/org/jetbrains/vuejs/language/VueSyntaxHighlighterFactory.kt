package org.jetbrains.vuejs.language

import com.intellij.ide.highlighter.HtmlFileHighlighter
import com.intellij.lexer.Lexer
import com.intellij.openapi.fileTypes.SingleLazyInstanceSyntaxHighlighterFactory
import com.intellij.openapi.fileTypes.SyntaxHighlighter

class VueSyntaxHighlighterFactory : SingleLazyInstanceSyntaxHighlighterFactory() {
  override fun createHighlighter(): SyntaxHighlighter {
    return object : HtmlFileHighlighter() {
      override fun getHighlightingLexer(): Lexer {
        return VueHighlightingLexer()
      }
    }
  }
}
