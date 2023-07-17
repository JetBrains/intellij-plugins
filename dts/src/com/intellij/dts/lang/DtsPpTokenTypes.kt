package com.intellij.dts.lang

import com.intellij.dts.lang.psi.DtsTypes
import com.intellij.pp.lang.PpTokenTypes
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet

object DtsPpTokenTypes : PpTokenTypes {
    override val defineStatement: IElementType = DtsTypes.PP_DEFINE_STATEMENT
    override val define: IElementType = DtsTypes.PP_DEFINE
    override val defineValue: IElementType = DtsTypes.PP_DEFINE_VALUE

    override val endifStatement: IElementType = DtsTypes.PP_ENDIF_STATEMENT
    override val endif: IElementType = DtsTypes.PP_ENDIF

    override val ifdefStatement: IElementType = DtsTypes.PP_IFDEF_STATEMENT
    override val ifdef: IElementType = DtsTypes.PP_IFDEF

    override val ifndefStatement: IElementType = DtsTypes.PP_IFNDEF_STATEMENT
    override val ifndef: IElementType = DtsTypes.PP_IFNDEF

    override val includeStatement: IElementType = DtsTypes.PP_INCLUDE_STATEMENT
    override val include: IElementType = DtsTypes.PP_INCLUDE

    override val undefStatement: IElementType = DtsTypes.PP_UNDEF_STATEMENT
    override val undef: IElementType = DtsTypes.PP_UNDEF

    override val symbol: IElementType = DtsTypes.PP_SYMBOL
    override val header: IElementType = DtsTypes.PP_HEADER
    override val path: IElementType = DtsTypes.PP_PATH

    override val dQuote: IElementType = DtsTypes.PP_DQUOTE
    override val lAngle: IElementType = DtsTypes.PP_LANGLE
    override val rAngle: IElementType = DtsTypes.PP_RANGLE

    override val statementEnd: IElementType = DtsTypes.PP_STATEMENT_END
    override val statementMarker: IElementType = DtsTypes.PP_STATEMENT_MARKER

    override fun createScopeSet(): TokenSet = TokenSet.create(DtsTypes.NODE_CONTENT)
}