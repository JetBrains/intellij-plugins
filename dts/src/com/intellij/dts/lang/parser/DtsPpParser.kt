package com.intellij.dts.lang.parser

import com.intellij.dts.lang.DtsTokenSets
import com.intellij.lang.PsiBuilder
import com.intellij.pp.lang.parser.PpStatementParser
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet

class DtsPpParser : PpStatementParser {
    override fun parseStatement(tokenType: IElementType, builderFactory: () -> PsiBuilder): Boolean {
        if (tokenType !in DtsTokenSets.ppDirectives) return false

        DtsParser.ppStatement(builderFactory(), 1)
        return true
    }

    override fun getStatementTokens(): TokenSet = DtsTokenSets.ppStatements
}