package org.intellij.plugin.mdx.lang.parse

import com.intellij.lang.javascript.DialectOptionHolder
import com.intellij.lang.javascript.JSFlexAdapter
import com.intellij.lang.javascript.JSTokenTypes

/**
 * Locates JavaScript expression boundaries without duplicating JavaScript lexical rules.
 *
 * This is the same token-based boundary-scanning pattern used by Svelte. Strings, template literals,
 * comments, regular expressions, and nested JSX are consumed by the platform JavaScript lexer; this
 * class only counts ordinary JavaScript braces.
 */
internal object MdxExpressionBoundaryScanner {
  /** Returns the end offset after the top-level `}`, or `-1` if the expression reaches [end]. */
  fun findExpressionEnd(buffer: CharSequence, start: Int, end: Int): Int {
    if (buffer.getOrNull(start) != '{') return -1

    val lexer = JSFlexAdapter(DialectOptionHolder.JS_WITH_JSX)
    lexer.start(buffer, start + 1, end, 0)
    var depth = 0
    while (lexer.tokenType != null) {
      when (lexer.tokenType) {
        // JSX expression containers use XML_LBRACE/XML_RBRACE and are balanced by the JSX lexer.
        JSTokenTypes.LBRACE -> depth++
        JSTokenTypes.RBRACE -> {
          if (depth == 0) return lexer.tokenEnd
          depth--
        }
        JSTokenTypes.REGEXP_LITERAL -> {
          findHostEndInUnterminatedRegularExpression(buffer, lexer.tokenStart, lexer.tokenEnd, depth)?.let {
            return it
          }
        }
      }
      lexer.advance()
    }
    return -1
  }

  /**
   * Returns the normal JavaScript-lexer boundary, or recovery boundaries that become lexically valid
   * when the input ends at that host `}`. Recovery is intentionally offered only when the lexer found
   * no boundary in the full range: the caller must validate each candidate against its host grammar.
   * This resolves ambiguous malformed input without overriding valid JavaScript tokens such as `/}/`.
   */
  fun findExpressionEndCandidates(buffer: CharSequence, start: Int, end: Int): List<Int> {
    val expressionEnd = findExpressionEnd(buffer, start, end)
    if (expressionEnd != -1) return listOf(expressionEnd)

    val result = mutableListOf<Int>()
    var candidate = buffer.indexOf('}', start + 1)
    while (candidate in 0..<end) {
      val candidateEnd = candidate + 1
      if (findExpressionEnd(buffer, start, candidateEnd) == candidateEnd) {
        result.add(candidateEnd)
      }
      candidate = buffer.indexOf('}', candidateEnd)
    }
    return result
  }

  /**
   * The JavaScript lexer deliberately accepts a regular-expression token without its final `/`.
   * In embedded code that recovery can swallow the host `}` after malformed JavaScript. Once the
   * platform lexer has identified precisely that failure, prefer an unescaped brace outside a
   * character class as the host boundary. Valid regular expressions remain entirely lexer-owned.
   */
  private fun findHostEndInUnterminatedRegularExpression(buffer: CharSequence,
                                                         start: Int,
                                                         end: Int,
                                                         initialDepth: Int): Int? {
    var escaped = false
    var inCharacterClass = false
    var offset = start + 1
    while (offset < end) {
      val char = buffer[offset]
      when {
        escaped -> escaped = false
        char == '\\' -> escaped = true
        char == '[' -> inCharacterClass = true
        char == ']' -> inCharacterClass = false
        char == '/' && !inCharacterClass -> return null
      }
      offset++
    }

    var depth = initialDepth
    escaped = false
    inCharacterClass = false
    offset = start + 1
    while (offset < end) {
      val char = buffer[offset]
      when {
        escaped -> escaped = false
        char == '\\' -> escaped = true
        char == '[' -> inCharacterClass = true
        char == ']' -> inCharacterClass = false
        !inCharacterClass && char == '{' -> depth++
        !inCharacterClass && char == '}' -> {
          if (depth == 0) return offset + 1
          depth--
        }
      }
      offset++
    }
    return null
  }

  private fun CharSequence.getOrNull(index: Int): Char? {
    return if (index in indices) this[index] else null
  }
}
