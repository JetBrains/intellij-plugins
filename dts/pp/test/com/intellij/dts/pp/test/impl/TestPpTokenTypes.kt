package com.intellij.dts.pp.test.impl

import com.intellij.dts.pp.lang.PpTokenTypes
import com.intellij.dts.pp.test.impl.psi.TestTypes
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet

object TestPpTokenTypes : PpTokenTypes() {
  override val statement: IElementType = TestTypes.PP_STATEMENT
  override val directive: IElementType = TestTypes.PP_DIRECTIVE

  override val lineBreak: IElementType = TestTypes.PP_LINE_BREAK
  override val comment: IElementType = TestTypes.PP_COMMENT
  override val inactive: IElementType = TestTypes.PP_INACTIVE

  override val headerName: IElementType = TestTypes.PP_HEADER_NAME

  override val identifier: IElementType = TestTypes.PP_IDENTIFIER
  override val operatorOrPunctuator: IElementType = TestTypes.PP_OPERATOR_OR_PUNCTUATOR
  override val integerLiteral: IElementType = TestTypes.PP_INTEGER_LITERAL
  override val charLiteral: IElementType = TestTypes.PP_CHAR_LITERAL
  override val floatLiteral: IElementType = TestTypes.PP_FLOAT_LITERAL
  override val stringLiteral: IElementType = TestTypes.PP_STRING_LITERAL

  override val statementEnd: IElementType = TestTypes.PP_STATEMENT_END
  override val statementMarker: IElementType = TestTypes.PP_STATEMENT_MARKER

  override fun createScopeSet(): TokenSet = TokenSet.EMPTY
}