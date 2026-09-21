package org.intellij.plugin.mdx.lang.parse

import com.intellij.lang.javascript.DialectOptionHolder
import com.intellij.lang.javascript.JSFlexAdapter
import com.intellij.psi.tree.IElementType

/**
 * Incrementally lexes only the source prefix exposed by the caller.
 *
 * Tokens are committed through `onStableToken` only after the JavaScript lexer reaches its base
 * state again. The remaining suffix is re-lexed on the next advance because an unterminated string,
 * template, comment, regular expression, or JSX token can change earlier token boundaries.
 */
internal class MdxPrefixJavaScriptLexer(
  private val text: CharSequence,
  start: Int,
  private val scanEnd: Int,
) {
  private var exposedEnd = start
  private var checkpointOffset = start

  fun advanceTo(
    limit: Int,
    onStableToken: (MdxJavaScriptToken) -> Boolean,
  ): List<MdxJavaScriptToken> {
    require(limit in exposedEnd..scanEnd) {
      "JavaScript lexer limit must advance from $exposedEnd to at most $scanEnd: $limit"
    }
    exposedEnd = limit
    if (checkpointOffset >= limit) return emptyList()

    val lexer = JSFlexAdapter(DialectOptionHolder.JS_WITH_JSX)
    lexer.start(text, checkpointOffset, limit, 0)
    val provisional = mutableListOf<MdxJavaScriptToken>()
    while (lexer.tokenType != null) {
      val tokenState = lexer.state
      val token = MdxJavaScriptToken(lexer.tokenType!!, lexer.tokenStart, lexer.tokenEnd)
      check(token.end <= limit) { "JavaScript lexer crossed the exposed prefix: ${token.end} > $limit" }
      if (token.start > checkpointOffset && tokenState == 0) {
        for (stableToken in provisional) {
          if (!onStableToken(stableToken)) return emptyList()
        }
        checkpointOffset = token.start
        provisional.clear()
      }
      provisional.add(token)
      lexer.advance()
    }
    return provisional
  }
}

internal data class MdxJavaScriptToken(
  val type: IElementType,
  val start: Int,
  val end: Int,
)

internal fun isMdxRegularExpressionTerminated(
  text: CharSequence,
  start: Int,
  end: Int,
): Boolean {
  var escaped = false
  var inCharacterClass = false
  var offset = start + 1
  while (offset < end) {
    val char = text[offset]
    when {
      escaped -> escaped = false
      char == '\\' -> escaped = true
      char == '[' -> inCharacterClass = true
      char == ']' -> inCharacterClass = false
      char == '/' && !inCharacterClass -> return true
    }
    offset++
  }
  return false
}
