package com.intellij.dts.lang.parser

import com.intellij.dts.lang.psi.DtsTypes
import com.intellij.lang.PsiBuilder
import com.intellij.pp.lang.parser.PpStatementParser
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet

class DtsIncludeParser : PpStatementParser {
    override fun parseStatement(tokenType: IElementType, builderFactory: () -> PsiBuilder): Boolean {
        if (tokenType != DtsTypes.INCLUDE) return false

        DtsParser.includeStatement(builderFactory(), 1)
        return true
    }

    override fun getStatementTokens(): TokenSet = TokenSet.create(DtsTypes.INCLUDE_STATEMENT)
}