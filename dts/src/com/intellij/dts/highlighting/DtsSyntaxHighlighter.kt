package com.intellij.dts.highlighting

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import com.intellij.dts.lang.lexer.DtsLexerAdapter
import com.intellij.dts.lang.DtsTokenSets
import com.intellij.dts.lang.psi.DtsTypes

class DtsSyntaxHighlighter : SyntaxHighlighterBase() {
    class Factory : SyntaxHighlighterFactory() {
        override fun getSyntaxHighlighter(project: Project?, virtualFile: VirtualFile?): SyntaxHighlighter {
            return DtsSyntaxHighlighter()
        }
    }

    override fun getHighlightingLexer(): Lexer {
        return DtsLexerAdapter()
    }

    private fun pack(vararg attr: DtsTextAttributes): Array<TextAttributesKey> {
        return attr.map { it.attribute }.toTypedArray()
    }

    override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> {
        if (tokenType in DtsTokenSets.comments) return pack(DtsTextAttributes.COMMENT)
        if (tokenType in DtsTokenSets.strings) return pack(DtsTextAttributes.STRING)
        if (tokenType in DtsTokenSets.operators) return pack(DtsTextAttributes.OPERATOR)
        if (tokenType in DtsTokenSets.compilerDirectives) return pack(DtsTextAttributes.COMPILER_DIRECTIVE)

        return when (tokenType) {
            DtsTypes.LBRACE, DtsTypes.RBRACE -> pack(DtsTextAttributes.BRACES)
            DtsTypes.LBRAC, DtsTypes.RBRAC, DtsTypes.LANGL, DtsTypes.RANGL -> pack(DtsTextAttributes.BRACKETS)
            DtsTypes.DQUOTE, DtsTypes.SQUOTE -> pack(DtsTextAttributes.STRING)
            DtsTypes.INT_VALUE, DtsTypes.BYTE_VALUE -> pack(DtsTextAttributes.NUMBER)
            DtsTypes.SEMICOLON -> pack(DtsTextAttributes.SEMICOLON)
            DtsTypes.COMMA -> pack(DtsTextAttributes.COMMA)
            TokenType.BAD_CHARACTER -> pack(DtsTextAttributes.BAD_CHARACTER)
            else -> pack(null)
        }
    }
}