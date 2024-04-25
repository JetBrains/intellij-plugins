package com.intellij.dts.highlighting

import com.intellij.dts.lang.DtsTokenSets
import com.intellij.dts.lang.lexer.DtsHighlightingLexerAdapter
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

  override fun getHighlightingLexer(): Lexer = DtsHighlightingLexerAdapter()

  private fun pack(vararg attr: DtsTextAttributes): Array<TextAttributesKey> {
    return attr.map { it.attribute }.toTypedArray()
  }

  override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> {
    if (tokenType in DtsTokenSets.comments) return pack(DtsTextAttributes.COMMENT)
    if (tokenType in DtsTokenSets.strings) return pack(DtsTextAttributes.STRING)
    if (tokenType in DtsTokenSets.numbers) return pack(DtsTextAttributes.NUMBER)
    if (tokenType in DtsTokenSets.operators) return pack(DtsTextAttributes.OPERATOR)
    if (tokenType in DtsTokenSets.compilerDirectives) return pack(DtsTextAttributes.COMPILER_DIRECTIVE)
    if (tokenType in DtsTokenSets.includePath) return pack(DtsTextAttributes.INCLUDE_PATH)

    return when (tokenType) {
      DtsTypes.LBRACE, DtsTypes.RBRACE -> pack(DtsTextAttributes.BRACES)
      DtsTypes.LBRACKET, DtsTypes.RBRACKET, DtsTypes.LANGL, DtsTypes.RANGL -> pack(DtsTextAttributes.BRACKETS)
      DtsTypes.SEMICOLON -> pack(DtsTextAttributes.SEMICOLON)
      DtsTypes.COMMA -> pack(DtsTextAttributes.COMMA)
      DtsTypes.PP_INACTIVE -> pack(DtsTextAttributes.INACTIVE)
      TokenType.BAD_CHARACTER -> pack(DtsTextAttributes.BAD_CHARACTER)

      else -> pack(null)
    }
  }
}