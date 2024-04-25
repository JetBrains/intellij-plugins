package com.intellij.dts.pp.lang

import com.intellij.dts.pp.lang.parser.PpAdHocParser
import com.intellij.dts.pp.lang.psi.PpStatementPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet

abstract class PpTokenTypes {
  private val parser by lazy { PpAdHocParser(this) }

  abstract val statement: IElementType
  abstract val directive: IElementType

  abstract val lineBreak: IElementType
  abstract val comment: IElementType
  abstract val inactive: IElementType

  abstract val headerName: IElementType

  abstract val identifier: IElementType
  abstract val operatorOrPunctuator: IElementType
  abstract val integerLiteral: IElementType
  abstract val charLiteral: IElementType
  abstract val floatLiteral: IElementType
  abstract val stringLiteral: IElementType

  /**
   * Marker for the end of a preprocessor statement. Injected by the PpLexerAdapter.
   */
  abstract val statementEnd: IElementType

  /**
   * Marker for a preprocessor statement. Emitted by the lexer of the host language.
   */
  abstract val statementMarker: IElementType

  abstract fun createScopeSet(): TokenSet

  /**
   * Wrapper for the PsiElement factory of the host language.
   */
  fun createElement(node: ASTNode?, hostFactory: (ASTNode?) -> PsiElement): PsiElement {
    return when (node?.elementType) {
      statement -> PpStatementPsiElement(node, parser)
      else -> hostFactory(node)
    }
  }
}