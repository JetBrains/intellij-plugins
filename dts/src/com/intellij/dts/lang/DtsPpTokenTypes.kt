package com.intellij.dts.lang

import com.intellij.dts.lang.psi.DtsTypes
import com.intellij.dts.pp.lang.PpTokenTypes
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet

object DtsPpTokenTypes : PpTokenTypes {
  override val defineStatement: IElementType = DtsTypes.PP_DEFINE_STATEMENT
  override val defineDirective: IElementType = DtsTypes.PP_DEFINE
  override val defineValue: IElementType = DtsTypes.PP_DEFINE_VALUE

  override val endifStatement: IElementType = DtsTypes.PP_ENDIF_STATEMENT
  override val endifDirective: IElementType = DtsTypes.PP_ENDIF

  override val ifStatement: IElementType = DtsTypes.PP_IF_STATEMENT
  override val ifDirective: IElementType = DtsTypes.PP_IF

  override val ifdefStatement: IElementType = DtsTypes.PP_IFDEF_STATEMENT
  override val ifdefDirective: IElementType = DtsTypes.PP_IFDEF

  override val ifndefStatement: IElementType = DtsTypes.PP_IFNDEF_STATEMENT
  override val ifndefDirective: IElementType = DtsTypes.PP_IFNDEF

  override val elifStatement: IElementType = DtsTypes.PP_ELIF_STATEMENT
  override val elifDirective: IElementType = DtsTypes.PP_ELIF

  override val elifdefStatement: IElementType = DtsTypes.PP_ELIFDEF_STATEMENT
  override val elifdefDirective: IElementType = DtsTypes.PP_ELIFDEF

  override val elifndefStatement: IElementType = DtsTypes.PP_ELIFNDEF_STATEMENT
  override val elifndefDirective: IElementType = DtsTypes.PP_ELIFNDEF

  override val elseStatement: IElementType = DtsTypes.PP_ELSE_STATEMENT
  override val elseDirective: IElementType = DtsTypes.PP_ELSE

  override val includeStatement: IElementType = DtsTypes.PP_INCLUDE_STATEMENT
  override val includeDirective: IElementType = DtsTypes.PP_INCLUDE
  override val includePath: IElementType = DtsTypes.PP_INCLUDE_PATH

  override val undefStatement: IElementType = DtsTypes.PP_UNDEF_STATEMENT
  override val undefDirective: IElementType = DtsTypes.PP_UNDEF

  override val symbol: IElementType = DtsTypes.PP_SYMBOL
  override val expression: IElementType = DtsTypes.PP_EXPRESSION

  override val statementEnd: IElementType = DtsTypes.PP_STATEMENT_END
  override val statementMarker: IElementType = DtsTypes.PP_STATEMENT_MARKER

  override fun createScopeSet(): TokenSet = TokenSet.create(DtsTypes.NODE_CONTENT)
}