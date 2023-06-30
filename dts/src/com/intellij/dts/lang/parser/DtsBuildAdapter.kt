package com.intellij.dts.lang.parser

import com.intellij.dts.lang.DtsTokenSets
import com.intellij.dts.lang.psi.DtsTypes
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiBuilder.Marker
import com.intellij.lang.PsiParser
import com.intellij.lang.parser.GeneratedParserUtilBase
import com.intellij.openapi.util.Key
import com.intellij.psi.tree.IElementType

class DtsBuildAdapter(
    delegate: PsiBuilder, state: GeneratedParserUtilBase.ErrorState, parser: PsiParser
) : FixedGeneratedBuilder(delegate, state, parser) {
    companion object {
        val lastParsedStatementIndex = Key.create<Int>("dts.lastParsedStatementIndex")
    }

    private fun parseStatement() {
        while (true) {
            when (super.getTokenType()) {
                DtsTypes.INCLUDE -> {
                    val builder = GeneratedParserUtilBase.Builder(delegate, GeneratedParserUtilBase.ErrorState(), parser)
                    DtsParser.includeStatement(builder, 1)

                    putUserData(lastParsedStatementIndex, rawTokenIndex())
                }
                in DtsTokenSets.ppDirectives -> {
                    val builder = GeneratedParserUtilBase.Builder(delegate, GeneratedParserUtilBase.ErrorState(), parser)
                    DtsParser.ppStatement(builder, 1)

                    putUserData(lastParsedStatementIndex, rawTokenIndex())
                }
                else -> break
            }
        }
    }

    override fun error(messageText: String) {
        var rollback = false
        while (true) {
            val latest = latestDoneMarker

            if (latest !is Marker || latest != productions.last()) break
            if (latest.tokenType !in DtsTokenSets.preprocessorStatements) break

            latest.rollbackTo()
            rollback = true
        }

        val latest = latestDoneMarker
        if (rollback && latest == productions.last()) {
            val backup = DtsParserUtil.backupMarker(latest)
            super.error(messageText)
            backup?.let { (marker, type) -> marker.done(type) }
        } else {
            super.error(messageText)
        }
    }

    override fun getTokenType(): IElementType? {
        parseStatement()
        return super.getTokenType()
    }

    override fun advanceLexer() {
        parseStatement()
        super.advanceLexer()
    }

    override fun getTokenText(): String? {
        parseStatement()
        return super.getTokenText()
    }

    override fun eof(): Boolean {
        parseStatement()
        return super.eof()
    }

    override fun getCurrentOffset(): Int {
        parseStatement()
        return super.getCurrentOffset()
    }

    override fun lookAhead(steps: Int): IElementType? {
        parseStatement()
        return super.lookAhead(steps)
    }

    override fun rawTokenIndex(): Int {
        parseStatement()
        return super.rawTokenIndex()
    }

    override fun rawLookup(steps: Int): IElementType? {
        parseStatement()
        return super.rawLookup(steps)
    }

    override fun rawTokenTypeStart(steps: Int): Int {
        parseStatement()
        return super.rawTokenTypeStart(steps)
    }
}