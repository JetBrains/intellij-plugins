package org.intellij.plugin.mdx.lang.parse

import com.intellij.lang.javascript.JSTokenTypes

/**
 * Locates JavaScript expression boundaries without duplicating JavaScript lexical rules.
 *
 * This is the same token-based boundary-scanning pattern used by Svelte. Strings, template literals,
 * comments, regular expressions, and nested JSX are consumed by the platform JavaScript lexer; this
 * class only counts ordinary JavaScript braces.
 */
internal object MdxExpressionBoundaryScanner {
  /**
   * Caps speculative reparses after the normal JavaScript lexer failed to find a boundary.
   * Valid JavaScript never consumes this budget; the cutoff only makes malformed editor input
   * degrade to an unterminated expression instead of repeatedly reparsing the rest of the file.
   */
  internal const val MALFORMED_RECOVERY_ATTEMPT_LIMIT = 64

  internal class RecoveryBudget(
    private var remainingAttempts: Int = MALFORMED_RECOVERY_ATTEMPT_LIMIT,
  ) {
    val exhausted: Boolean
      get() = remainingAttempts == 0

    fun tryAcquire(): Boolean {
      if (exhausted) return false
      remainingAttempts--
      return true
    }

    fun copy(): RecoveryBudget = RecoveryBudget(remainingAttempts)
  }

  /** Incrementally validates host-brace candidates after normal JavaScript lexing failed. */
  internal class RecoveryCandidatesSession(
    source: CharSequence,
    private val start: Int,
    private val scanEnd: Int = source.length,
    private val recoveryBudget: RecoveryBudget = RecoveryBudget(),
  ) {
    private val buffer = mdxCancellableText(source)
    private var exposedEnd = start
    private var cursor = start + 1

    fun advanceTo(limit: Int): List<Int> {
      mdxCancellableText(buffer)
      require(limit in exposedEnd..scanEnd) {
        "Expression recovery limit must advance from $exposedEnd to at most $scanEnd: $limit"
      }
      exposedEnd = limit
      return buildList {
        while (cursor < limit) {
          val candidateEnd = cursor + 1
          if (buffer[cursor] == '}' &&
              recoveryBudget.tryAcquire() &&
              findExpressionEnd(buffer, start, candidateEnd) == candidateEnd) {
            add(candidateEnd)
          }
          cursor++
        }
      }
    }
  }

  internal class Session(
    source: CharSequence,
    start: Int,
    private val scanEnd: Int = source.length,
  ) {
    private val buffer = mdxCancellableText(source)
    private val lexer = if (buffer.getOrNull(start) == '{') MdxPrefixJavaScriptLexer(buffer, start + 1, scanEnd) else null
    private var exposedEnd = start
    private var depth = 0
    private var expressionEnd = -1

    fun advanceTo(limit: Int): Int = advanceToBoundary(limit).end

    fun advanceToBoundary(limit: Int): Boundary {
      mdxCancellableText(buffer)
      require(limit in exposedEnd..scanEnd) {
        "Expression scan limit must advance from $exposedEnd to at most $scanEnd: $limit"
      }
      exposedEnd = limit
      val lexer = lexer ?: return Boundary.NOT_FOUND
      if (expressionEnd != -1) return Boundary(expressionEnd, stable = true)

      val provisional = lexer.advanceTo(limit, ::acceptStableToken)
      if (expressionEnd != -1) return Boundary(expressionEnd, stable = true)
      return Boundary(scanTokens(provisional, depth).expressionEnd, stable = false)
    }

    private fun acceptStableToken(token: MdxJavaScriptToken): Boolean {
      val result = scanToken(token, depth)
      depth = result.depth
      expressionEnd = result.expressionEnd
      return expressionEnd == -1
    }

    private fun scanToken(token: MdxJavaScriptToken, initialDepth: Int): TokenScanResult {
      if (token.type == JSTokenTypes.REGEXP_LITERAL && !isMdxRegularExpressionTerminated(buffer, token.start, token.end)) {
        val recoveredEnd = UnterminatedRegularExpressionRecovery(buffer, token.start, initialDepth).advanceTo(token.end)
        if (recoveredEnd != null) return TokenScanResult(initialDepth, recoveredEnd)
      }

      return when (token.type) {
        // JSX expression containers use XML_LBRACE/XML_RBRACE and are balanced by the JSX lexer.
        JSTokenTypes.LBRACE -> TokenScanResult(initialDepth + 1)
        JSTokenTypes.RBRACE -> {
          if (initialDepth == 0) TokenScanResult(initialDepth, token.end)
          else TokenScanResult(initialDepth - 1)
        }
        else -> TokenScanResult(initialDepth)
      }
    }

    private fun scanTokens(tokens: List<MdxJavaScriptToken>, initialDepth: Int): TokenScanResult {
      var result = TokenScanResult(initialDepth)
      for (token in tokens) {
        result = scanToken(token, result.depth)
        if (result.expressionEnd != -1) return result
      }
      return result
    }

    private data class TokenScanResult(val depth: Int, val expressionEnd: Int = -1)
  }

  internal data class Boundary(val end: Int, val stable: Boolean) {
    companion object {
      val NOT_FOUND = Boundary(-1, stable = false)
    }
  }

  /** Returns the end offset after the top-level `}`, or `-1` if the expression reaches [end]. */
  fun findExpressionEnd(buffer: CharSequence, start: Int, end: Int): Int {
    return Session(buffer, start, end).advanceTo(end)
  }

  /**
   * Returns the normal JavaScript-lexer boundary, or recovery boundaries that become lexically valid
   * when the input ends at that host `}`. Recovery is intentionally offered only when the lexer found
   * no boundary in the full range: the caller must validate each candidate against its host grammar.
   * This resolves ambiguous malformed input without overriding valid JavaScript tokens such as `/}/`.
   */
  fun findExpressionEndCandidates(
    buffer: CharSequence,
    start: Int,
    end: Int,
    recoveryBudget: RecoveryBudget = RecoveryBudget(),
  ): List<Int> {
    val text = mdxCancellableText(buffer)
    val expressionEnd = findExpressionEnd(text, start, end)
    if (expressionEnd != -1) return listOf(expressionEnd)

    return RecoveryCandidatesSession(text, start, end, recoveryBudget).advanceTo(end)
  }

  /**
   * The JavaScript lexer deliberately accepts a regular-expression token without its final `/`.
   * In embedded code that recovery can swallow the host `}` after malformed JavaScript. Once the
   * platform lexer has identified precisely that failure, prefer an unescaped brace outside a
   * character class as the host boundary. Valid regular expressions remain entirely lexer-owned.
   */
  private class UnterminatedRegularExpressionRecovery(
    private val buffer: CharSequence,
    start: Int,
    private var depth: Int,
  ) {
    private var offset = start + 1
    private var escaped = false
    private var inCharacterClass = false
    private var expressionEnd = -1

    fun advanceTo(limit: Int): Int? {
      if (expressionEnd != -1) return expressionEnd
      while (offset < limit) {
        val char = buffer[offset]
        when {
          escaped -> escaped = false
          char == '\\' -> escaped = true
          char == '[' -> inCharacterClass = true
          char == ']' -> inCharacterClass = false
          !inCharacterClass && char == '{' -> depth++
          !inCharacterClass && char == '}' -> {
            if (depth == 0) {
              expressionEnd = offset + 1
              return expressionEnd
            }
            depth--
          }
        }
        offset++
      }
      return null
    }
  }

  private fun CharSequence.getOrNull(index: Int): Char? {
    return if (index in indices) this[index] else null
  }
}
