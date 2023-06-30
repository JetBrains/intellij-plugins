package com.intellij.dts.lang.parser

import com.intellij.dts.lang.psi.DtsTypes
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiParser
import com.intellij.lang.parser.GeneratedParserUtilBase
import com.intellij.psi.tree.IElementType

class DtsIncludeBuildAdapter(
    delegate: PsiBuilder, state: GeneratedParserUtilBase.ErrorState, parser: PsiParser
) : FixedGeneratedBuilder(delegate, state, parser) {
    private fun preprocess() {
        while (super.getTokenType() == DtsTypes.INCLUDE) {
            val builder = GeneratedParserUtilBase.Builder(delegate, GeneratedParserUtilBase.ErrorState(), parser)
            DtsParser.includeStatement(builder, 1)
        }
    }

    override fun getTokenType(): IElementType? {
        preprocess()
        return super.getTokenType()
    }

    override fun advanceLexer() {
        preprocess()
        super.advanceLexer()
    }

    override fun getTokenText(): String? {
        preprocess()
        return super.getTokenText()
    }

    override fun eof(): Boolean {
        preprocess()
        return super.eof()
    }

    override fun getCurrentOffset(): Int {
        preprocess()
        return super.getCurrentOffset()
    }

    override fun lookAhead(steps: Int): IElementType? {
        preprocess()
        return super.lookAhead(steps)
    }

    override fun rawTokenIndex(): Int {
        preprocess()
        return super.rawTokenIndex()
    }

    override fun rawLookup(steps: Int): IElementType? {
        preprocess()
        return super.rawLookup(steps)
    }

    override fun rawTokenTypeStart(steps: Int): Int {
        preprocess()
        return super.rawTokenTypeStart(steps)
    }
}