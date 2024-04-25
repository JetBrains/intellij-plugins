package com.intellij.dts.lang

import com.intellij.dts.lang.psi.DtsTypes
import com.intellij.dts.pp.lang.PpTokenTypes
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet

object DtsPpTokenTypes : PpTokenTypes() {
  override val statement: IElementType = DtsTypes.PP_STATEMENT
  override val directive: IElementType = DtsTypes.PP_DIRECTIVE

  override val lineBreak: IElementType = DtsTypes.PP_LINE_BRAK
  override val comment: IElementType = DtsTypes.PP_COMMENT
  override val inactive: IElementType = DtsTypes.PP_INACTIVE

  override val headerName: IElementType = DtsTypes.PP_HEADER_NAME

  override val identifier: IElementType = DtsTypes.PP_IDENTIFIER
  override val operatorOrPunctuator: IElementType = DtsTypes.PP_OPERATOR_OR_PUNCTUATOR
  override val integerLiteral: IElementType = DtsTypes.PP_INTEGER_LITERAL
  override val charLiteral: IElementType = DtsTypes.PP_CHAR_LITERAL
  override val floatLiteral: IElementType = DtsTypes.PP_FLOAT_LITERAL
  override val stringLiteral: IElementType = DtsTypes.PP_STRING_LITERAL

  override val statementEnd: IElementType = DtsTypes.PP_STATEMENT_END
  override val statementMarker: IElementType = DtsTypes.PP_STATEMENT_MARKER

  override fun createScopeSet(): TokenSet = TokenSet.create(DtsTypes.NODE_CONTENT)
}