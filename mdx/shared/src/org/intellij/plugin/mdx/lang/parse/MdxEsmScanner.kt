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

    val tokens = tokenize(text, start, limit)
    val nesting = NestingState()
    var lastSignificantEnd = -1
    var statement = ModuleStatementState()
    var lexicalState = LexicalState()

    for ((index, token) in tokens.withIndex()) {
      val atTopLevel = nesting.atTopLevel
      lexicalState.accept(text, token)
      if (token.type == JSTokenTypes.WHITE_SPACE) {
        var lineBreak = text.indexOf('\n', token.start)
        while (lineBreak in token.start..<token.end) {
          if (atTopLevel) {
            if (hasBlankLineAfter(text, lineBreak + 1, text.length)) {
              return Block(start..lastSignificantEnd, terminated = false, recoveryBoundary = true)
            }
            if (lastSignificantEnd != -1 &&
                lexicalState.isTerminated &&
                statement.isTerminated &&
                isCompleteBeforeLineBreak(text, lastSignificantEnd - 1) &&
                !nextLineContinuesEsm(text, lineBreak + 1, text.length, lastSignificantEnd - 1)) {
              return Block(start..lastSignificantEnd, true)
            }
          }
          lineBreak = text.indexOf('\n', lineBreak + 1)
        }
      }

      if (token.type.isTrivia()) continue

      statement.accept(token.type, atTopLevel)
      nesting.accept(token.type)
      lastSignificantEnd = token.end

      if (token.type == JSTokenTypes.SEMICOLON && atTopLevel) {
        val next = tokens.nextSignificant(index + 1)
        if (next == null || hasLineBreakBefore(text, token.end, next.start) || !isEsmKeywordAt(text, next.start)) {
          return Block(start..token.end, true)
        }
        statement = ModuleStatementState()
        lexicalState = LexicalState()
        lastSignificantEnd = -1
      }
    }

    return Block(
      start..limit,
      nesting.atTopLevel && lexicalState.isTerminated && statement.isTerminated,
    )
  }

  /** Returns adjacent top-level ESM keyword offsets that have no whitespace or semicolon before them. */
  fun findMissingStatementSeparators(text: CharSequence, start: Int, end: Int): List<Int> {
    val nesting = NestingState()
    return buildList {
      for ((tokenType, tokenStart) in tokenize(text, start, end)) {
        if (tokenStart > start &&
            nesting.atTopLevel &&
            tokenType.isEsmKeyword() &&
            !hasStatementSeparatorBefore(text, tokenStart)) {
          add(tokenStart)
        }
        nesting.accept(tokenType)
      }
    }
  }

  private fun tokenize(text: CharSequence, start: Int, limit: Int): List<Token> {
    val lexer = JSFlexAdapter(DialectOptionHolder.JS_WITH_JSX)
    lexer.start(text, start, limit, 0)
    return buildList {
      while (lexer.tokenType != null) {
        add(Token(lexer.tokenType!!, lexer.tokenStart, lexer.tokenEnd))
        lexer.advance()
      }
    }
  }

  private fun List<Token>.nextSignificant(startIndex: Int): Token? {
    for (index in startIndex..<size) {
      if (!this[index].type.isTrivia()) return this[index]
    }
    return null
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

  private fun IElementType.isTrivia(): Boolean {
    return this == JSTokenTypes.WHITE_SPACE ||
           this == JSTokenTypes.END_OF_LINE_COMMENT ||
           this == JSTokenTypes.C_STYLE_COMMENT ||
           this == JSTokenTypes.XML_STYLE_COMMENT
  }

  private fun IElementType.isEsmKeyword(): Boolean {
    return this == JSTokenTypes.IMPORT_KEYWORD || this == JSTokenTypes.EXPORT_KEYWORD
  }

  private fun hasStatementSeparatorBefore(text: CharSequence, offset: Int): Boolean {
    val previous = text.getOrNull(offset - 1) ?: return true
    return previous.isWhitespace() || previous == ';'
  }

  private fun CharSequence.getOrNull(index: Int): Char? {
    return if (index in indices) this[index] else null
  }

  private data class Token(val type: IElementType, val start: Int, val end: Int)

  private class ModuleStatementState {
    private var first: IElementType? = null
    private var second: IElementType? = null
    private var significantCount = 0
    private var sawFrom = false
    private var sawModuleSpecifierAfterFrom = false
    private var requiresTopLevelBody = false
    private var sawTopLevelBody = false

    fun accept(tokenType: IElementType, atTopLevel: Boolean) {
      significantCount++
      if (first == null) first = tokenType
      else if (second == null) second = tokenType
      if (atTopLevel && (tokenType == JSTokenTypes.FUNCTION_KEYWORD || tokenType == JSTokenTypes.CLASS_KEYWORD)) {
        requiresTopLevelBody = true
      }
      else if (atTopLevel && requiresTopLevelBody && tokenType == JSTokenTypes.LBRACE) {
        sawTopLevelBody = true
      }
      if (tokenType == JSTokenTypes.FROM_KEYWORD) {
        sawFrom = true
      }
      else if (sawFrom && JSTokenTypes.STRING_LITERALS.contains(tokenType)) {
        sawModuleSpecifierAfterFrom = true
      }
    }

    val isTerminated: Boolean
      get() = (!requiresTopLevelBody || sawTopLevelBody) && when (first) {
        JSTokenTypes.IMPORT_KEYWORD -> when (second) {
          null -> false
          JSTokenTypes.STRING_LITERAL, JSTokenTypes.SINGLE_QUOTE_STRING_LITERAL, JSTokenTypes.LPAR, JSTokenTypes.DOT -> true
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

  private class LexicalState {
    private var templateDelimiters = 0
    private var hasUnterminatedToken = false

    fun accept(text: CharSequence, token: Token) {
      when (token.type) {
        JSTokenTypes.BACKQUOTE -> templateDelimiters++
        JSTokenTypes.STRING_LITERAL, JSTokenTypes.SINGLE_QUOTE_STRING_LITERAL -> {
          if (!isQuotedLiteralTerminated(text, token.start, token.end)) hasUnterminatedToken = true
        }
        JSTokenTypes.C_STYLE_COMMENT -> {
          if (token.end - token.start < 4 || text[token.end - 2] != '*' || text[token.end - 1] != '/') {
            hasUnterminatedToken = true
          }
        }
        JSTokenTypes.REGEXP_LITERAL -> {
          if (!isRegularExpressionTerminated(text, token.start, token.end)) hasUnterminatedToken = true
        }
      }
    }

    val isTerminated: Boolean
      get() = !hasUnterminatedToken && templateDelimiters % 2 == 0
  }

  private class NestingState {
    private var parenDepth = 0
    private var braceDepth = 0
    private var bracketDepth = 0
    private val jsxState = JsxState()

    fun accept(tokenType: IElementType) {
      jsxState.accept(tokenType)
      when (tokenType) {
        JSTokenTypes.LPAR -> parenDepth++
        JSTokenTypes.RPAR -> if (parenDepth > 0) parenDepth--
        JSTokenTypes.LBRACE -> braceDepth++
        JSTokenTypes.RBRACE -> if (braceDepth > 0) braceDepth--
        JSTokenTypes.LBRACKET -> bracketDepth++
        JSTokenTypes.RBRACKET -> if (bracketDepth > 0) bracketDepth--
      }
    }

    val atTopLevel: Boolean
      get() = parenDepth == 0 && braceDepth == 0 && bracketDepth == 0 && jsxState.isBalanced
  }

  private class JsxState {
    private var depth = 0
    private var inClosingTag = false

    fun accept(tokenType: IElementType) {
      when (tokenType) {
        JSTokenTypes.XML_START_TAG_START -> depth++
        JSTokenTypes.XML_END_TAG_START -> inClosingTag = true
        JSTokenTypes.XML_EMPTY_TAG_END -> {
          if (depth > 0) depth--
          inClosingTag = false
        }
        JSTokenTypes.XML_TAG_END -> {
          if (inClosingTag && depth > 0) depth--
          inClosingTag = false
        }
      }
    }

    val isBalanced: Boolean
      get() = depth == 0 && !inClosingTag
  }

  private fun isQuotedLiteralTerminated(text: CharSequence, start: Int, end: Int): Boolean {
    if (end - start < 2) return false
    val quote = text[start]
    if ((quote != '\'' && quote != '"') || text[end - 1] != quote) return false

    var precedingBackslashes = 0
    var offset = end - 2
    while (offset > start && text[offset] == '\\') {
      precedingBackslashes++
      offset--
    }
    return precedingBackslashes % 2 == 0
  }

  private fun isRegularExpressionTerminated(text: CharSequence, start: Int, end: Int): Boolean {
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

  private val LINE_END_CONTINUATION_CHARS = setOf(
    '=', '+', '-', '*', '/', '%', '&', '|', '^', '!', '~', '?', '.', ',', ':', '(', '[', '{', '<',
  )
  private val NEXT_LINE_CONTINUATION_CHARS = setOf(
    '.', '?', ':', ',', '+', '-', '*', '/', '%', '&', '|', ')', ']', '}', '(',
  )
}
