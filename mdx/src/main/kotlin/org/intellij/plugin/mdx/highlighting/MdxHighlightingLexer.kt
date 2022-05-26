package org.intellij.plugin.mdx.highlighting

import com.intellij.lexer.LayeredLexer
import com.intellij.psi.tree.IElementType
import org.intellij.plugin.mdx.lang.parse.MdxFlavourDescriptor
import org.intellij.plugin.mdx.lang.parse.MdxTokenTypes
import org.intellij.plugins.markdown.lang.MarkdownElementType
import org.intellij.plugins.markdown.lang.MarkdownElementTypes
import org.intellij.plugins.markdown.lang.MarkdownTokenTypeSets
import org.intellij.plugins.markdown.lang.lexer.MarkdownMergingLexer
import org.intellij.plugins.markdown.lang.lexer.MarkdownToplevelLexer

class MdxHighlightingLexer : LayeredLexer(MdxHighlightingLexerBase()) {
    init {
        registerSelfStoppingLayer(MarkdownMergingLexer(), MarkdownTokenTypeSets.INLINE_HOLDING_ELEMENT_TYPES.types, IElementType.EMPTY_ARRAY)
    }
}