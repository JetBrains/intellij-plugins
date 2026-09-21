package org.intellij.plugin.mdx.highlighting

import com.intellij.lexer.MergeFunction
import com.intellij.lexer.MergingLexerAdapterBase
import org.intellij.plugin.mdx.lang.parse.MdxFlavourDescriptor
import org.intellij.plugin.mdx.lang.parse.MdxTokenTypes
import org.intellij.plugins.markdown.lang.MarkdownTokenTypeSets
import org.intellij.plugins.markdown.lang.MarkdownTokenTypes
import org.intellij.plugins.markdown.lang.lexer.MarkdownToplevelLexer

class MdxHighlightingLexerBase : MergingLexerAdapterBase(MarkdownToplevelLexer(MdxFlavourDescriptor)) {
  override fun getMergeFunction(): MergeFunction {
    return MergeFunction { type, originalLexer ->
      if (MarkdownTokenTypeSets.INLINE_HOLDING_ELEMENT_TYPES.contains(type) && originalLexer.tokenType === MarkdownTokenTypes.EOL) {
        originalLexer.advance()
        return@MergeFunction type
      }
      if (MdxTokenTypes.EMBEDDED_JS_CONTENT != type) {
        return@MergeFunction type
      }
      var lastTokenText: CharSequence? = null
      while (originalLexer.tokenType != null && originalLexer.tokenType === type) {
        val trim = originalLexer.tokenText.trim()
        if (trim.isNotEmpty() || lastTokenText?.endsWith("\n") != true) {
          lastTokenText = originalLexer.tokenText
          originalLexer.advance()
        }
        else {
          return@MergeFunction type
        }
      }
      type
    }
  }
}
