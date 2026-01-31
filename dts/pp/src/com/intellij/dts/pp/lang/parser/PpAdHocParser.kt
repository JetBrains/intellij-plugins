package com.intellij.dts.pp.lang.parser

import com.intellij.dts.pp.lang.PpTokenTypes
import com.intellij.dts.pp.lang.psi.PpElifDefStatement
import com.intellij.dts.pp.lang.psi.PpElifExprStatement
import com.intellij.dts.pp.lang.psi.PpElifNdefStatement
import com.intellij.dts.pp.lang.psi.PpElseStatement
import com.intellij.dts.pp.lang.psi.PpError
import com.intellij.dts.pp.lang.psi.PpIfDefStatement
import com.intellij.dts.pp.lang.psi.PpIfExprStatement
import com.intellij.dts.pp.lang.psi.PpIfNdefStatement
import com.intellij.dts.pp.lang.psi.PpIncludeStatement
import com.intellij.dts.pp.lang.psi.PpStatement
import com.intellij.dts.pp.lang.psi.PpStatementType
import com.intellij.dts.pp.lang.psi.PpStatementType.Elif
import com.intellij.dts.pp.lang.psi.PpStatementType.ElifDef
import com.intellij.dts.pp.lang.psi.PpStatementType.ElifNdef
import com.intellij.dts.pp.lang.psi.PpStatementType.Else
import com.intellij.dts.pp.lang.psi.PpStatementType.Endif
import com.intellij.dts.pp.lang.psi.PpStatementType.If
import com.intellij.dts.pp.lang.psi.PpStatementType.IfDef
import com.intellij.dts.pp.lang.psi.PpStatementType.IfNdef
import com.intellij.dts.pp.lang.psi.PpStatementType.Include
import com.intellij.dts.pp.lang.psi.PpStatementType.Unknown
import com.intellij.dts.pp.lang.psi.PpToken
import com.intellij.openapi.util.TextRange
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet

private class StatementImpl(
  override val tokens: List<PpToken>,
  override val tokenTypes: PpTokenTypes,
) : PpStatement {
  override var type: PpStatementType = Unknown

  override val errors: MutableList<PpError> = mutableListOf()
}

class PpAdHocParser(internal val tokenTypes: PpTokenTypes) {
  private lateinit var iterator: Iterator<PpToken>
  private lateinit var statement: StatementImpl

  private val directiveRx = "\\w+$".toRegex()
  private val commentOrWhitespace = TokenSet.create(tokenTypes.comment, tokenTypes.lineBreak, TokenType.WHITE_SPACE)

  private fun error(message: String, range: TextRange) {
    statement.errors.add(PpError(message, range))
  }

  private fun next(): PpToken? {
    while (iterator.hasNext()) {
      val token = iterator.next()
      if (token.type !in commentOrWhitespace) return token
    }

    return null
  }

  private fun next(type: IElementType): PpToken? {
    val token = next()

    if (token == null) {
      error("expected $type", TextRange.from(statement.range.endOffset, 0))
      return null
    }
    if (token.type != type) {
      error("expected $type, found ${token.type}", token.range)
      return null
    }

    return token
  }

  @Synchronized
  fun parse(tokens: List<PpToken>): PpStatement {
    iterator = tokens.iterator()
    statement = StatementImpl(tokens, tokenTypes)

    parseStatement()

    return when (statement.type) {
      If -> PpIfExprStatement(statement)
      IfDef -> PpIfDefStatement(statement)
      IfNdef -> PpIfNdefStatement(statement)
      Else -> PpElseStatement(statement)
      Elif -> PpElifExprStatement(statement)
      ElifDef -> PpElifDefStatement(statement)
      ElifNdef -> PpElifNdefStatement(statement)
      Include -> PpIncludeStatement(statement)
      else -> statement
    }
  }

  private fun parseStatement() {
    parseDirective()

    when (statement.type) {
      IfDef, IfNdef, ElifDef, ElifNdef -> {
        parseIdentifier()
        parseEnd()
      }
      Else, Endif -> {
        parseEnd()
      }
      Include -> {
        parseHeaderName()
        parseEnd()
      }
      else -> {}
    }
  }

  private fun parseDirective() {
    val token = next(tokenTypes.directive) ?: return
    val directive = directiveRx.find(token.text)?.value

    if (directive == null) {
      error("unknown directive", token.range)
    }
    else {
      statement.type = PpStatementType.entries.firstOrNull { directive == it.directive } ?: Unknown
    }
  }

  private fun parseIdentifier() {
    next(tokenTypes.identifier)
  }

  private fun parseHeaderName() {
    next(tokenTypes.headerName)
  }

  private fun parseEnd() {
    val token = next() ?: return

    if (token.type != tokenTypes.statementEnd) {
      error("expected end of statement", token.range)
    }
  }
}