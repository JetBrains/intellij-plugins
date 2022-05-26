package org.intellij.plugin.mdx.highlighting

import com.intellij.lexer.Lexer
import org.intellij.plugins.markdown.highlighting.MarkdownSyntaxHighlighter


class MdxSyntaxHighlighter : MarkdownSyntaxHighlighter() {
    private val myHighlightingLexer = MdxHighlightingLexer()

    override fun getHighlightingLexer(): Lexer {
        return myHighlightingLexer
    }
}