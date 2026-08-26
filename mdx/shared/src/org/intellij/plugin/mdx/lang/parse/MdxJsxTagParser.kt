package org.intellij.plugin.mdx.lang.parse

import com.intellij.openapi.util.TextRange

/** Incrementally parses one JSX tag without replaying an already observed prefix. */
internal class MdxJsxTagParser(
  source: CharSequence,
  val start: Int,
  private val scanEnd: Int = source.length,
  private val recoveryBudget: MdxExpressionBoundaryScanner.RecoveryBudget = MdxExpressionBoundaryScanner.RecoveryBudget(),
) {
  private val text = mdxCancellableText(source)
  private var exposedEnd = start
  private var cursor = start
  private var phase = Phase.TAG_START
  private var closing = false
  private var nameStart = -1
  private var name: String? = null
  private var openingTail: OpeningTagTail? = null
  private var terminalResult: Result? = null

  fun advanceTo(limit: Int): Result {
    mdxCancellableText(text)
    require(limit in exposedEnd..scanEnd) {
      "JSX tag scan limit must advance from $exposedEnd to at most $scanEnd: $limit"
    }
    exposedEnd = limit
    terminalResult?.let { return it }

    while (true) {
      when (phase) {
        Phase.TAG_START -> {
          if (cursor >= limit) return Result.Pending
          if (text[cursor] != '<') return finish(Result.Invalid)
          cursor++
          phase = Phase.AFTER_TAG_START
        }
        Phase.AFTER_TAG_START -> {
          if (cursor >= limit) return Result.Pending
          if (text[cursor] == '/') {
            closing = true
            cursor++
          }
          phase = Phase.NAME
        }
        Phase.NAME -> {
          if (cursor >= limit) return Result.Pending
          if (nameStart == -1) {
            if (text[cursor] == '>') {
              cursor++
              return finish(completeFragment())
            }
            if (!isNameStart(text[cursor])) return finish(Result.Invalid)
            nameStart = cursor
            cursor++
          }
          while (cursor < limit && isNamePart(text[cursor])) {
            cursor++
          }
          if (cursor >= limit) return Result.Pending

          name = text.subSequence(nameStart, cursor).toString()
          if (closing) {
            phase = Phase.CLOSING_TAG_END
          }
          else {
            openingTail = OpeningTagTail(text, start, checkNotNull(name), cursor, scanEnd, recoveryBudget)
            phase = Phase.OPENING_TAG_TAIL
          }
        }
        Phase.CLOSING_TAG_END -> {
          while (cursor < limit && text[cursor].isWhitespace()) {
            cursor++
          }
          if (cursor >= limit) return Result.Pending
          if (text[cursor] != '>') return finish(Result.Invalid)
          cursor++
          return finish(Result.Complete(tag(MdxJsxScanner.TagKind.CLOSING)))
        }
        Phase.OPENING_TAG_TAIL -> {
          val result = checkNotNull(openingTail).advanceTo(limit)
          return if (result == Result.Pending) result else finish(result)
        }
      }
    }
  }

  private fun completeFragment(): Result.Complete {
    val kind = if (closing) MdxJsxScanner.TagKind.CLOSING else MdxJsxScanner.TagKind.OPENING
    return Result.Complete(tag(kind))
  }

  private fun tag(kind: MdxJsxScanner.TagKind): MdxJsxScanner.Tag {
    return MdxJsxScanner.Tag(TextRange(start, cursor), kind, name, emptyList(), emptyList())
  }

  private fun finish(result: Result): Result {
    terminalResult = result
    return result
  }

  private enum class Phase {
    TAG_START,
    AFTER_TAG_START,
    NAME,
    CLOSING_TAG_END,
    OPENING_TAG_TAIL,
  }

  private class OpeningTagTail(
    private val text: CharSequence,
    private val tagStart: Int,
    private val name: String,
    initialOffset: Int,
    private val scanEnd: Int,
    private val recoveryBudget: MdxExpressionBoundaryScanner.RecoveryBudget,
    initialAttributes: List<TextRange> = emptyList(),
    initialExpressions: List<TextRange> = emptyList(),
    private var selfClosing: Boolean = false,
  ) {
    private var cursor = initialOffset
    private var phase = Phase.BETWEEN_ATTRIBUTES
    private val attributes = initialAttributes.toMutableList()
    private val expressions = initialExpressions.toMutableList()
    private var attributeStart = -1
    private var quote = '\u0000'
    private var escaped = false
    private var activeExpression: ExpressionState? = null
    private var delegatedTail: OpeningTagTail? = null
    private var terminalResult: Result? = null

    fun advanceTo(limit: Int): Result {
      terminalResult?.let { return it }
      delegatedTail?.let { return forward(it.advanceTo(limit)) }
      while (true) {
        when (phase) {
          Phase.BETWEEN_ATTRIBUTES -> {
            skipWhitespace(limit)
            if (cursor >= limit) return Result.Pending
            when (text[cursor]) {
              '>' -> {
                cursor++
                val kind = if (selfClosing) MdxJsxScanner.TagKind.SELF_CLOSING else MdxJsxScanner.TagKind.OPENING
                return finish(Result.Complete(tag(kind)))
              }
              '/' -> {
                selfClosing = true
                cursor++
              }
              '{' -> beginExpression(attributeStart = null)
              else -> {
                if (!isAttributeNameStart(text[cursor])) return finish(Result.Invalid)
                attributeStart = cursor
                cursor++
                phase = Phase.ATTRIBUTE_NAME
              }
            }
          }
          Phase.ATTRIBUTE_NAME -> {
            while (cursor < limit && isAttributeNamePart(text[cursor])) {
              cursor++
            }
            if (cursor >= limit) return Result.Pending
            phase = Phase.AFTER_ATTRIBUTE_NAME
          }
          Phase.AFTER_ATTRIBUTE_NAME -> {
            skipWhitespace(limit)
            if (cursor >= limit) return Result.Pending
            if (text[cursor] == '=') {
              cursor++
              phase = Phase.BEFORE_ATTRIBUTE_VALUE
            }
            else {
              finishAttribute(cursor)
              phase = Phase.BETWEEN_ATTRIBUTES
            }
          }
          Phase.BEFORE_ATTRIBUTE_VALUE -> {
            skipWhitespace(limit)
            if (cursor >= limit) return Result.Pending
            when (text[cursor]) {
              '\'', '"' -> {
                quote = text[cursor]
                escaped = false
                cursor++
                phase = Phase.QUOTED_ATTRIBUTE_VALUE
              }
              '{' -> beginExpression(attributeStart)
              else -> phase = Phase.UNQUOTED_ATTRIBUTE_VALUE
            }
          }
          Phase.QUOTED_ATTRIBUTE_VALUE -> {
            if (!scanQuotedAttribute(limit)) return Result.Pending
          }
          Phase.UNQUOTED_ATTRIBUTE_VALUE -> {
            while (cursor < limit && !text[cursor].isWhitespace() && text[cursor] != '>' && text[cursor] != '/') {
              cursor++
            }
            if (cursor >= limit) return Result.Pending
            finishAttribute(cursor)
            phase = Phase.BETWEEN_ATTRIBUTES
          }
          Phase.EXPRESSION -> return advanceExpression(limit)
        }
      }
    }

    private fun scanQuotedAttribute(limit: Int): Boolean {
      while (cursor < limit) {
        val char = text[cursor]
        cursor++
        when {
          escaped -> escaped = false
          char == '\\' -> escaped = true
          char == quote -> {
            finishAttribute(cursor)
            phase = Phase.BETWEEN_ATTRIBUTES
            return true
          }
        }
      }
      return false
    }

    private fun beginExpression(attributeStart: Int?) {
      activeExpression = ExpressionState(text, cursor, scanEnd, attributeStart, recoveryBudget)
      phase = Phase.EXPRESSION
    }

    private fun advanceExpression(limit: Int): Result {
      val expression = checkNotNull(activeExpression)
      val boundary = expression.boundary.advanceToBoundary(limit)
      if (boundary.end == -1) {
        expression.provisional = ProvisionalTail.None
        return advanceRecoveryAlternatives(expression, limit)
      }

      if (boundary.stable) {
        val continuation = when (val provisional = expression.provisional) {
          is ProvisionalTail.Candidate -> provisional.tail.takeIf { provisional.expressionEnd == boundary.end }
          is ProvisionalTail.Rejected -> if (provisional.expressionEnd == boundary.end) return finish(Result.Invalid) else null
          ProvisionalTail.None -> null
        } ?: forkAfterExpression(expression, boundary.end)
        delegatedTail = continuation
        activeExpression = null
        return forward(continuation.advanceTo(limit))
      }

      val continuation = when (val provisional = expression.provisional) {
        is ProvisionalTail.Candidate -> provisional.tail.takeIf { provisional.expressionEnd == boundary.end }
        is ProvisionalTail.Rejected -> if (provisional.expressionEnd == boundary.end) return Result.Pending else null
        ProvisionalTail.None -> null
      } ?: forkAfterExpression(expression, boundary.end).also {
        expression.provisional = ProvisionalTail.Candidate(boundary.end, it)
      }
      return when (val result = continuation.advanceTo(limit)) {
        Result.Pending -> result
        Result.Invalid -> {
          expression.provisional = ProvisionalTail.Rejected(boundary.end)
          Result.Pending
        }
        is Result.Complete -> finish(result)
      }
    }

    private fun advanceRecoveryAlternatives(expression: ExpressionState, limit: Int): Result {
      for (candidateEnd in expression.recovery.advanceTo(limit)) {
        expression.alternatives.add(forkAfterExpression(expression, candidateEnd))
      }
      var alternativeIndex = 0
      while (alternativeIndex < expression.alternatives.size) {
        when (val result = expression.alternatives[alternativeIndex].advanceTo(limit)) {
          Result.Pending -> alternativeIndex++
          Result.Invalid -> expression.alternatives.removeAt(alternativeIndex)
          is Result.Complete -> return finish(result)
        }
      }
      return Result.Pending
    }

    private fun forkAfterExpression(expression: ExpressionState, expressionEnd: Int): OpeningTagTail {
      val recoveredAttributes = expression.attributeStart?.let { attributes + TextRange(it, expressionEnd) } ?: attributes
      return OpeningTagTail(
        text,
        tagStart,
        name,
        expressionEnd,
        scanEnd,
        recoveryBudget,
        recoveredAttributes,
        expressions + TextRange(expression.start, expressionEnd),
        selfClosing,
      )
    }

    private fun finishAttribute(end: Int) {
      attributes.add(TextRange(attributeStart, end))
      attributeStart = -1
    }

    private fun skipWhitespace(limit: Int) {
      while (cursor < limit && text[cursor].isWhitespace()) {
        cursor++
      }
    }

    private fun tag(kind: MdxJsxScanner.TagKind): MdxJsxScanner.Tag {
      return MdxJsxScanner.Tag(TextRange(tagStart, cursor), kind, name, attributes.toList(), expressions.toList())
    }

    private fun finish(result: Result): Result {
      terminalResult = result
      return result
    }

    private fun forward(result: Result): Result {
      return if (result == Result.Pending) result else finish(result)
    }

    private class ExpressionState(
      text: CharSequence,
      val start: Int,
      scanEnd: Int,
      val attributeStart: Int?,
      recoveryBudget: MdxExpressionBoundaryScanner.RecoveryBudget,
    ) {
      val boundary = MdxExpressionBoundaryScanner.Session(text, start, scanEnd)
      val recovery = MdxExpressionBoundaryScanner.RecoveryCandidatesSession(text, start, scanEnd, recoveryBudget)
      val alternatives = mutableListOf<OpeningTagTail>()
      var provisional: ProvisionalTail = ProvisionalTail.None
    }

    private sealed interface ProvisionalTail {
      data object None : ProvisionalTail
      data class Candidate(val expressionEnd: Int, val tail: OpeningTagTail) : ProvisionalTail
      data class Rejected(val expressionEnd: Int) : ProvisionalTail
    }

    private enum class Phase {
      BETWEEN_ATTRIBUTES,
      ATTRIBUTE_NAME,
      AFTER_ATTRIBUTE_NAME,
      BEFORE_ATTRIBUTE_VALUE,
      QUOTED_ATTRIBUTE_VALUE,
      UNQUOTED_ATTRIBUTE_VALUE,
      EXPRESSION,
    }
  }

  sealed interface Result {
    data object Pending : Result
    data object Invalid : Result
    data class Complete(val tag: MdxJsxScanner.Tag) : Result
  }

  companion object {
    fun parse(
      text: CharSequence,
      start: Int,
      limit: Int,
      recoveryBudget: MdxExpressionBoundaryScanner.RecoveryBudget = MdxExpressionBoundaryScanner.RecoveryBudget(),
    ): MdxJsxScanner.Tag? {
      return when (val result = MdxJsxTagParser(text, start, limit, recoveryBudget).advanceTo(limit)) {
        Result.Pending, Result.Invalid -> null
        is Result.Complete -> result.tag
      }
    }

    fun isNameStart(char: Char?): Boolean {
      return char != null && (char.isLetter() || char == '_')
    }

    fun isNamePart(char: Char): Boolean {
      return char.isLetterOrDigit() || char == '_' || char == '-' || char == '.' || char == ':'
    }

    fun scanQuoted(text: CharSequence, start: Int, limit: Int, quote: Char): Int {
      var offset = start + 1
      while (offset < limit) {
        when (text[offset]) {
          '\\' -> offset = (offset + 2).coerceAtMost(limit)
          quote -> return offset + 1
          else -> offset++
        }
      }
      return -1
    }

    private fun isAttributeNameStart(char: Char): Boolean {
      return char.isLetter() || char == '_' || char == ':' || char == '$'
    }

    private fun isAttributeNamePart(char: Char): Boolean {
      return char.isLetterOrDigit() || char == '_' || char == '-' || char == '.' || char == ':' || char == '$'
    }
  }
}
