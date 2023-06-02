package com.intellij.dts.completion

import com.intellij.dts.lang.psi.DtsTypes
import com.intellij.lang.BracePair
import com.intellij.lang.PairedBraceMatcher
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType

// only on brace can be structural otherwise EnterAfterUnmatchedBraceHandler will get confused
private val bracePairs = arrayOf(
    BracePair(DtsTypes.LBRACE, DtsTypes.RBRACE, true),
    BracePair(DtsTypes.LBRAC, DtsTypes.RBRAC, false),
    BracePair(DtsTypes.LANGL, DtsTypes.RANGL, false),
    BracePair(DtsTypes.LPAREN, DtsTypes.RPAREN, false),
)

class DtsBraceMatcher : PairedBraceMatcher {
    override fun getPairs(): Array<BracePair> = bracePairs

    override fun isPairedBracesAllowedBeforeType(lbraceType: IElementType, contextType: IElementType?) = true

    override fun getCodeConstructStart(file: PsiFile?, openingBraceOffset: Int): Int = openingBraceOffset
}
