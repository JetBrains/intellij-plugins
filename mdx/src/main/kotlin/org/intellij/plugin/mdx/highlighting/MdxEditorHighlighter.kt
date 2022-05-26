package org.intellij.plugin.mdx.highlighting

import com.intellij.lang.javascript.DialectOptionHolder
import com.intellij.lang.javascript.dialects.ECMA6SyntaxHighlighterFactory
import com.intellij.openapi.editor.colors.EditorColorsScheme
import com.intellij.openapi.editor.ex.util.LayerDescriptor
import com.intellij.openapi.editor.ex.util.LayeredLexerEditorHighlighter
import org.intellij.plugin.mdx.lang.parse.MdxTokenTypes
import org.intellij.plugins.markdown.lang.MarkdownElementType
import org.intellij.plugins.markdown.lang.MarkdownTokenTypes

class MdxEditorHighlighter(colors: EditorColorsScheme)
    : LayeredLexerEditorHighlighter(MdxSyntaxHighlighter(), colors) {
    init {
        val outerHighlighter = ECMA6SyntaxHighlighterFactory.ECMA6SyntaxHighlighter(DialectOptionHolder.JSX, false)
        registerLayer(MarkdownElementType.platformType(MdxTokenTypes.JSX_BLOCK_CONTENT), LayerDescriptor(outerHighlighter, "\n;"))
        registerLayer(MarkdownTokenTypes.HTML_TAG, LayerDescriptor(outerHighlighter, ""))
    }

}