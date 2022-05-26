package org.intellij.plugin.mdx.highlighting

import com.intellij.lexer.MergeFunction
import com.intellij.lexer.MergingLexerAdapterBase
import org.intellij.plugin.mdx.lang.parse.MdxFlavourDescriptor
import org.intellij.plugin.mdx.lang.parse.MdxTokenTypes
import org.intellij.plugins.markdown.lang.MarkdownElementType
import org.intellij.plugins.markdown.lang.lexer.MarkdownToplevelLexer

class MdxHighlightingLexerBase : MergingLexerAdapterBase(MarkdownToplevelLexer(MdxFlavourDescriptor)) {
    override fun getMergeFunction(): MergeFunction {
        return MergeFunction { type, originalLexer ->
            if (MarkdownElementType.platformType(MdxTokenTypes.JSX_BLOCK_CONTENT) != type)  {
            return@MergeFunction type
            }
            var lastTokenText: CharSequence? = null
            while (originalLexer.tokenType != null && originalLexer.tokenType === type) {
                val trim = originalLexer.tokenText.trim()
                if (!trim.isNullOrEmpty() || lastTokenText?.endsWith("\n") != true) {
                    lastTokenText = originalLexer.tokenText
                    originalLexer.advance()
                } else {
                    return@MergeFunction type
                }
            }
            type
        }
    }
}