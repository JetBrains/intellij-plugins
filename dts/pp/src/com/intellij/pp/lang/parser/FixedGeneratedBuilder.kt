package com.intellij.pp.lang.parser

import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiParser
import com.intellij.lang.impl.PsiBuilderAdapter
import com.intellij.lang.impl.PsiBuilderImpl
import com.intellij.lang.parser.GeneratedParserUtilBase
import com.intellij.lexer.Lexer

// workaround for PsiBuilderImpl casts in GeneratedParserUtilBase.Builder
open class FixedGeneratedBuilder(
    delegate: PsiBuilder,
    state: GeneratedParserUtilBase.ErrorState,
    parser: PsiParser
) : GeneratedParserUtilBase.Builder(delegate, state, parser) {

    private fun getImplFromDelegate(builder: PsiBuilder): PsiBuilderImpl {
        if (builder is PsiBuilderImpl) return builder
        if (builder is PsiBuilderAdapter) return getImplFromDelegate(builder.delegate)

        throw UnsupportedOperationException("${builder.javaClass.name} is not PsiBuilderImpl or PsiBuilderAdapter")
    }

    override fun getLexer(): Lexer = getImplFromDelegate(myDelegate).lexer

    override fun getProductions(): List<PsiBuilderImpl.ProductionMarker> = getImplFromDelegate(myDelegate).productions

    override fun getDelegate(): PsiBuilder = getImplFromDelegate(myDelegate)
}