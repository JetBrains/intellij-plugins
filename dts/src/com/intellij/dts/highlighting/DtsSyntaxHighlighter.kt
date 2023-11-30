package com.intellij.dts.highlighting

import com.intellij.dts.lang.DtsTokenSets
import com.intellij.dts.lang.lexer.DtsLexerAdapter
import com.intellij.dts.lang.psi.DtsTypes
import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType

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
    if (tokenType in DtsTokenSets.ppDirectives) return pack(DtsTextAttributes.COMPILER_DIRECTIVE)

    return when (tokenType) {
      DtsTypes.LBRACE, DtsTypes.RBRACE -> pack(DtsTextAttributes.BRACES)
      DtsTypes.LBRACKET, DtsTypes.RBRACKET, DtsTypes.LANGL, DtsTypes.RANGL -> pack(DtsTextAttributes.BRACKETS)
      DtsTypes.INT_LITERAL, DtsTypes.BYTE_LITERAL -> pack(DtsTextAttributes.NUMBER)
      DtsTypes.SEMICOLON -> pack(DtsTextAttributes.SEMICOLON)
      DtsTypes.COMMA -> pack(DtsTextAttributes.COMMA)
      TokenType.BAD_CHARACTER -> pack(DtsTextAttributes.BAD_CHARACTER)
      DtsTypes.INCLUDE_PATH, DtsTypes.PP_INCLUDE_PATH -> pack(DtsTextAttributes.STRING)

      else -> pack(null)
    }
  }
}