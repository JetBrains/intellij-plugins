package com.intellij.dts.pp.lang.parser

import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiParser
import com.intellij.lang.parser.GeneratedParserUtilBase
import com.intellij.dts.pp.lang.PpTokenTypes
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet

/**
 * Can be added to a parser to support parsing of preprocessor statements.
 * Statement parsers should implement [PpStatementParser].
 */
class PpBuildAdapter(
  delegate: PsiBuilder,
  state: GeneratedParserUtilBase.ErrorState,
  parser: PsiParser,
  tokenTypes: PpTokenTypes,
  private val parsers: List<PpStatementParser>,
) : FixedGeneratedBuilder(delegate, state, parser) {
    val ppScopeSet = tokenTypes.createScopeSet()
    val ppStatementsSet = TokenSet.orSet(*parsers.map { it.getStatementTokens() }.toTypedArray())

    private fun builderFactory(): PsiBuilder {
        return FixedGeneratedBuilder(delegate,  GeneratedParserUtilBase.ErrorState(), parser)
    }

    private fun parseStatement() {
        parse@ while (true) {
            val token = super.getTokenType() ?: return

            for (statementParser in parsers) {
                if (statementParser.parseStatement(token, ::builderFactory)) continue@parse
            }

            break
        }
    }

    override fun error(messageText: String) {
        var rollback = false
        while (true) {
            val latest = latestDoneMarker

            if (latest !is PsiBuilder.Marker || latest != productions.last()) break
            if (latest.tokenType !in ppStatementsSet) break

            latest.rollbackTo()
            rollback = true
        }

        val latest = latestDoneMarker
        if (rollback && latest == productions.last()) {
            val backup = PpParserUtil.backupMarker(latest)
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