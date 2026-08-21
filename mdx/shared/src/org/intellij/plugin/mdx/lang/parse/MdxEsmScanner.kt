package org.intellij.plugin.mdx.lang.parse

import com.intellij.lang.javascript.DialectOptionHolder
import com.intellij.lang.javascript.JSFlexAdapter
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.psi.tree.IElementType

object MdxEsmScanner {
  private const val MAX_SCAN_LENGTH = 250_000

  data class Block(
    val range: IntRange,
    val terminated: Boolean,
    val recoveryBoundary: Boolean = false,
  )

  fun isLineStart(text: CharSequence, start: Int): Boolean {
    val lineStart = lineStart(text, start)
    val indent = smallIndent(text, lineStart, start)
    return indent != -1 && lineStart + indent == start && isEsmKeywordAt(text, start)
  }

  fun scanBlock(text: CharSequence, start: Int, limit: Int = text.length): Block? {
    if (limit - start > MAX_SCAN_LENGTH || !isEsmKeywordAt(text, start)) return null
    var offset = start
    var parenDepth = 0
    var braceDepth = 0
    var bracketDepth = 0
    var lastSignificantOffset = -1
    var statementStart = start
    while (offset < limit) {
      when (text[offset]) {
        '\'' -> {
          offset = scanQuoted(text, offset, limit, '\'')
          if (offset != -1) lastSignificantOffset = offset - 1
        }
        '"' -> {
          offset = scanQuoted(text, offset, limit, '"')
          if (offset != -1) lastSignificantOffset = offset - 1
        }
        '`' -> {
          offset = scanTemplate(text, offset, limit)
          if (offset != -1) lastSignificantOffset = offset - 1
        }
        '/' -> {
          val end = scanSlash(text, offset, limit)
          if (end == offset + 1) {
            lastSignificantOffset = offset
          }
          offset = end
        }
        '<' -> {
          val element = MdxJsxScanner.scanJsxElement(text, offset, limit)
          if (element != null && element.terminated) {
            offset = element.range.last
            lastSignificantOffset = offset - 1
          }
          else {
            lastSignificantOffset = offset
            offset++
          }
        }
        '(' -> {
          parenDepth++
          lastSignificantOffset = offset
          offset++
        }
        ')' -> {
          if (parenDepth > 0) parenDepth--
          lastSignificantOffset = offset
          offset++
        }
        '{' -> {
          braceDepth++
          lastSignificantOffset = offset
          offset++
        }
        '}' -> {
          if (braceDepth > 0) braceDepth--
          lastSignificantOffset = offset
          offset++
        }
        '[' -> {
          bracketDepth++
          lastSignificantOffset = offset
          offset++
        }
        ']' -> {
          if (bracketDepth > 0) bracketDepth--
          lastSignificantOffset = offset
          offset++
        }
        ';' -> {
          lastSignificantOffset = offset
          offset++
          if (parenDepth == 0 && braceDepth == 0 && bracketDepth == 0) {
            val nextStatement = nextEsmStatementOnSameLine(text, offset, limit)
            if (nextStatement == -1) {
              return Block(start..offset, true)
            }
            statementStart = nextStatement
            lastSignificantOffset = -1
            offset = nextStatement
          }
        }
        '\n' -> {
          if (parenDepth == 0 && braceDepth == 0 && bracketDepth == 0 && hasBlankLineAfter(text, offset + 1, text.length)) {
            return Block(start..lastSignificantOffset + 1, terminated = false, recoveryBoundary = true)
          }
          if (parenDepth == 0 &&
              braceDepth == 0 &&
              bracketDepth == 0 &&
              isCompleteBeforeLineBreak(text, lastSignificantOffset) &&
              isModuleStatementTerminated(text, statementStart, lastSignificantOffset + 1) &&
              !nextLineContinuesEsm(text, offset + 1, text.length, lastSignificantOffset)) {
            return Block(start..lastSignificantOffset + 1, true)
          }
          offset++
        }
        else -> {
          if (!text[offset].isWhitespace()) {
            lastSignificantOffset = offset
          }
          offset++
        }
      }
      if (offset == -1) return Block(start..limit, false)
    }
    val delimitersBalanced = parenDepth == 0 && braceDepth == 0 && bracketDepth == 0
    return Block(
      start..limit,
      delimitersBalanced && isModuleStatementTerminated(text, statementStart, limit),
    )
  }

  private fun scanQuoted(text: CharSequence, start: Int, limit: Int, quote: Char): Int {
    var offset = start + 1
    while (offset < limit) {
      when (text[offset]) {
        '\\' -> offset += 2
        quote -> return offset + 1
        else -> offset++
      }
    }
    return -1
  }

  private fun scanTemplate(text: CharSequence, start: Int, limit: Int): Int {
    var offset = start + 1
    while (offset < limit) {
      when (text[offset]) {
        '\\' -> offset += 2
        '`' -> return offset + 1
        '$' -> {
          if (text.getOrNull(offset + 1) == '{') {
            val expressionEnd = MdxJsxScanner.scanExpression(text, offset + 1, limit)
            if (expressionEnd == -1) return -1
            offset = expressionEnd
          }
          else {
            offset++
          }
        }
        else -> offset++
      }
    }
    return -1
  }

  private fun scanSlash(text: CharSequence, start: Int, limit: Int): Int {
    if (text.getOrNull(start + 1) == '/') {
      var offset = start + 2
      while (offset < limit && text[offset] != '\n') {
        offset++
      }
      return offset
    }
    if (text.getOrNull(start + 1) == '*') {
      var offset = start + 2
      while (offset + 1 < limit) {
        if (text[offset] == '*' && text[offset + 1] == '/') return offset + 2
        offset++
      }
      return -1
    }
    return start + 1
  }

  private fun isCompleteBeforeLineBreak(text: CharSequence, lastSignificantOffset: Int): Boolean {
    if (lastSignificantOffset == -1) return false
    val char = text[lastSignificantOffset]
    return char !in LINE_END_CONTINUATION_CHARS &&
           (char != '>' || text.getOrNull(lastSignificantOffset - 1) != '=')
  }

  private fun nextLineContinuesEsm(text: CharSequence, start: Int, limit: Int, previousSignificantOffset: Int): Boolean {
    val next = firstNonWhitespaceOffset(text, start, limit)
    if (next == -1) return false
    if (hasLineBreakBefore(text, start, next)) return false
    val lineStart = lineStart(text, next)
    if (smallIndent(text, lineStart, next) == -1) return true
    if (keywordAt(text, next, "from")) return true
    if (keywordEndsAt(text, previousSignificantOffset, "from")) {
      return text[next] == '\'' || text[next] == '"' || text[next] == '`'
    }
    if (keywordEndsAt(text, previousSignificantOffset, "import")) {
      return isImportContinuationStart(text[next])
    }
    if (keywordEndsAt(text, previousSignificantOffset, "export")) {
      return isExportContinuationStart(text[next])
    }
    return text[next] in NEXT_LINE_CONTINUATION_CHARS
  }

  private fun nextEsmStatementOnSameLine(text: CharSequence, start: Int, limit: Int): Int {
    val next = firstNonWhitespaceOffset(text, start, limit)
    if (next == -1 || hasLineBreakBefore(text, start, next)) return -1
    return if (isEsmKeywordAt(text, next)) next else -1
  }

  private fun keywordEndsAt(text: CharSequence, endOffset: Int, keyword: String): Boolean {
    val start = endOffset - keyword.length + 1
    return start >= 0 && keywordAt(text, start, keyword)
  }

  private fun isImportContinuationStart(char: Char): Boolean {
    return char == '{' || char == '*' || char == '\'' || char == '"' || char == '`' || isNameStart(char)
  }

  private fun isExportContinuationStart(char: Char): Boolean {
    return char == '{' || char == '*' || isNameStart(char)
  }

  private fun hasLineBreakBefore(text: CharSequence, start: Int, end: Int): Boolean {
    var offset = start
    while (offset < end) {
      if (text[offset] == '\n') return true
      offset++
    }
    return false
  }

  private fun hasBlankLineAfter(text: CharSequence, start: Int, limit: Int): Boolean {
    var offset = start
    while (offset < limit && text[offset] != '\n') {
      if (!text[offset].isWhitespace()) return false
      offset++
    }
    return start < limit
  }

  private fun firstNonWhitespaceOffset(text: CharSequence, start: Int, limit: Int): Int {
    var offset = start
    while (offset < limit) {
      if (!text[offset].isWhitespace()) {
        return offset
      }
      offset++
    }
    return -1
  }

  private fun isModuleStatementTerminated(buffer: CharSequence, start: Int, end: Int): Boolean {
    val lexer = JSFlexAdapter(DialectOptionHolder.JS_WITH_JSX)
    lexer.start(buffer, start, end, 0)

    var first: IElementType? = null
    var second: IElementType? = null
    var significantCount = 0
    var sawFrom = false
    var sawModuleSpecifierAfterFrom = false
    while (lexer.tokenType != null) {
      val tokenType = lexer.tokenType!!
      if (!tokenType.isTrivia()) {
        significantCount++
        if (first == null) first = tokenType
        else if (second == null) second = tokenType
        if (tokenType == JSTokenTypes.FROM_KEYWORD) {
          sawFrom = true
        }
        else if (sawFrom && tokenType == JSTokenTypes.STRING_LITERAL) {
          sawModuleSpecifierAfterFrom = true
        }
      }
      lexer.advance()
    }

    return when (first) {
      JSTokenTypes.IMPORT_KEYWORD -> when (second) {
        null -> false
        JSTokenTypes.STRING_LITERAL, JSTokenTypes.LPAR, JSTokenTypes.DOT -> true
        else -> sawModuleSpecifierAfterFrom
      }
      JSTokenTypes.EXPORT_KEYWORD -> when {
        second == null -> false
        sawFrom -> sawModuleSpecifierAfterFrom
        second == JSTokenTypes.MULT -> false
        second == JSTokenTypes.DEFAULT_KEYWORD && significantCount == 2 -> false
        else -> true
      }
      else -> true
    }
  }

  private fun IElementType.isTrivia(): Boolean {
    return this == JSTokenTypes.WHITE_SPACE ||
           this == JSTokenTypes.END_OF_LINE_COMMENT ||
           this == JSTokenTypes.C_STYLE_COMMENT ||
           this == JSTokenTypes.XML_STYLE_COMMENT
  }

  private fun isEsmKeywordAt(text: CharSequence, start: Int): Boolean {
    return keywordAt(text, start, "import") || keywordAt(text, start, "export")
  }

  private fun keywordAt(text: CharSequence, start: Int, keyword: String): Boolean {
    if (start + keyword.length > text.length) return false
    for (index in keyword.indices) {
      if (text[start + index] != keyword[index]) return false
    }
    val before = text.getOrNull(start - 1)
    val after = text.getOrNull(start + keyword.length)
    return before?.let { !isNamePart(it) } != false && after?.let { !isNamePart(it) } != false
  }

  private fun isNameStart(char: Char?): Boolean {
    return char != null && (char.isLetter() || char == '_')
  }

  private fun isNamePart(char: Char): Boolean {
    return char.isLetterOrDigit() || char == '_' || char == '-' || char == '.' || char == ':'
  }

  private fun lineStart(text: CharSequence, offset: Int): Int {
    var lineStart = offset.coerceAtMost(text.length)
    while (lineStart > 0 && text[lineStart - 1] != '\n') {
      lineStart--
    }
    return lineStart
  }

  private fun smallIndent(text: CharSequence, lineStart: Int, offset: Int): Int {
    var indent = 0
    while (lineStart + indent < offset && text[lineStart + indent] == ' ') {
      indent++
    }
    return if (indent <= 3) indent else -1
  }

  private fun CharSequence.getOrNull(index: Int): Char? {
    return if (index in indices) this[index] else null
  }

  private val LINE_END_CONTINUATION_CHARS = setOf('=', '+', '-', '*', '/', '%', '&', '|', '^', '!', '~', '?', '.', ',', ':', '(', '[', '{', '<')
  private val NEXT_LINE_CONTINUATION_CHARS = setOf('.', '?', ':', ',', '+', '-', '*', '/', '%', '&', '|', ')', ']', '}', '(')
}
