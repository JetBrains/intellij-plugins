package com.intellij.dts.pp.lang.psi

import com.intellij.dts.pp.lang.PpTokenTypes
import com.intellij.openapi.util.TextRange
import com.intellij.psi.tree.IElementType

data class PpToken(val type: IElementType, val text: CharSequence, val range: TextRange)

data class PpError(val message: String, val range: TextRange)

interface PpStatement {
  val tokens: List<PpToken>
  val errors: List<PpError>

  val type: PpStatementType

  val tokenTypes: PpTokenTypes

  val range: TextRange
    get() {
      return if (tokens.isEmpty()) {
        TextRange.EMPTY_RANGE
      }
      else {
        TextRange(tokens.first().range.startOffset, tokens.last().range.endOffset)
      }
    }
}

fun PpStatement.findFirstOrNullToken(type: IElementType): PpToken? = tokens.firstOrNull { it.type == type }

fun PpStatement.findFirstToken(type: IElementType): PpToken = tokens.first { it.type == type }

val PpStatement.directive: PpToken get() = findFirstToken(tokenTypes.directive)

val PpStatement.identifier: PpToken? get() = findFirstOrNullToken(tokenTypes.identifier)
