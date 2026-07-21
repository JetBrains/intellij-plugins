package org.intellij.plugin.mdx.highlighting

import com.intellij.lexer.LayeredLexer
import com.intellij.psi.tree.IElementType
import org.intellij.plugins.markdown.lang.MarkdownTokenTypeSets

class MdxHighlightingLexer : LayeredLexer(MdxHighlightingLexerBase()) {
  init {
    registerSelfStoppingLayer(MdxInlineHighlightingLexer(), MarkdownTokenTypeSets.INLINE_HOLDING_ELEMENT_TYPES.types, IElementType.EMPTY_ARRAY)
  }
}