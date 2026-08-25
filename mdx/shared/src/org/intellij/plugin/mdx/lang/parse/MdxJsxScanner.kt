package org.intellij.plugin.mdx.lang.parse

internal object MdxJsxScanner {
  private const val MAX_SCAN_LENGTH = 250_000

  enum class TagKind {
    OPENING,
    CLOSING,
    SELF_CLOSING
  }

  data class Tag(
    val range: IntRange,
    val kind: TagKind,
    val name: String?,
    val attributes: List<IntRange>,
    val expressions: List<IntRange>,
  )

  data class Element(
    val range: IntRange,
    val tags: List<Tag>,
    val expressions: List<IntRange>,
    val incomplete: Boolean,
    val terminated: Boolean,
  )

  fun isLineStartJsx(text: CharSequence, start: Int): Boolean {
    val lineStart = lineStart(text, start)
    val indent = smallIndent(text, lineStart, start)
    return indent != -1 &&
           lineStart + indent == start &&
           (parseTag(text, start, text.length) != null || isIncompleteOpeningTagStart(text, start, text.length))
  }

  /**
   * If a `<Name…` opening tag that has not yet been closed with `>` begins at [start] (see
   * [isIncompleteOpeningTagStart]), returns its range up to the logical end of a "clean" prefix — a
   * `<Name` with optional, fully-formed attributes, ending at a line break or end of input. A dangling
   * `{…` expression or unterminated quote (e.g. the static `<Broken attr={"unterminated}`) yields
   * `null` so it keeps its existing outer-language parse rather than being reinterpreted as JSX.
   * Used to project a freshly-typed `<My` into the MdxJS layer so the platform JSX tag-name completion
   * runs while the tag is still unbalanced. Ranges use an exclusive `.last` (a text offset), matching
   * the other MdxJsxScanner ranges. WEB-78468.
   */
  fun incompleteOpeningTagRange(text: CharSequence, start: Int, limit: Int = text.length): IntRange? {
    if (text.getOrNull(start) != '<') return null
    if (start + 1 >= limit || text[start + 1] == '\n' || text[start + 1] == '\r') {
      return start..start + 1
    }
    if (!isIncompleteOpeningTagStart(text, start, limit)) return null
    var offset = start + 1
    while (offset < limit && isNamePart(text[offset])) {
      offset++
    }
    while (offset < limit) {
      when (text[offset]) {
        '\n' -> return start..offset
        '{' -> {
          val expressionEnd = MdxExpressionBoundaryScanner.findExpressionEnd(text, offset, limit)
          if (expressionEnd == -1) return null
          offset = expressionEnd
        }
        '\'', '"' -> {
          val quoteEnd = scanQuoted(text, offset, limit, text[offset])
          if (quoteEnd == -1) return null
          offset = quoteEnd
        }
        else -> offset++
      }
    }
    return start..limit
  }

  fun scanJsxElement(
    text: CharSequence,
    start: Int,
    limit: Int = text.length,
    opaqueRanges: MdxOpaqueRanges = MdxOpaqueRanges.EMPTY,
  ): Element? {
    if (limit - start > MAX_SCAN_LENGTH) return null
    val opening = parseTag(text, start, limit) ?: return null
    if (opening.kind == TagKind.CLOSING) return null
    if (opening.kind == TagKind.SELF_CLOSING) {
      return Element(start..opening.range.last, listOf(opening), opening.expressions, incomplete = false, terminated = true)
    }

    val tags = mutableListOf(opening)
    val expressions = opening.expressions.toMutableList()
    val stack = mutableListOf(opening.name)
    var incomplete = false
    var offset = opening.range.last
    while (offset < limit) {
      val opaqueEnd = opaqueRanges.endOffsetContaining(offset)
      if (opaqueEnd != null) {
        offset = opaqueEnd.coerceAtMost(limit)
        continue
      }
      val commentEnd = MdxHtmlCommentBoundary.findClosedMultilineCommentEnd(text, offset, limit)
      if (commentEnd != -1) {
        offset = commentEnd
        continue
      }
      // Skip fenced code blocks whole: their {/}/< are code, not MDX expressions or tags.
      if (isAtLineStart(text, offset)) {
        val fenceEnd = MdxMarkdownFenceScanner.findEnd(text, offset, limit)
        if (fenceEnd != -1) {
          offset = fenceEnd
          continue
        }
      }
      when (text[offset]) {
        '{' -> {
          val expressionEnd = MdxExpressionBoundaryScanner.findExpressionEnd(text, offset, limit)
          if (expressionEnd == -1) {
            return Element(start..limit, tags, expressions, incomplete = true, terminated = false)
          }
          expressions.add(offset..expressionEnd)
          offset = expressionEnd
        }
        '<' -> {
          val tag = parseTag(text, offset, limit)
          if (tag == null) {
            offset++
            continue
          }
          tags.add(tag)
          expressions.addAll(tag.expressions)
          when (tag.kind) {
            TagKind.OPENING -> stack.add(tag.name)
            TagKind.SELF_CLOSING -> Unit
            TagKind.CLOSING -> {
              val top = stack.lastOrNull()
              if (top == tag.name || top == null || tag.name == null) {
                stack.removeAt(stack.lastIndex)
              }
              else {
                val ancestorIndex = stack.indexOfLast { it == tag.name }
                if (ancestorIndex >= 0) {
                  while (stack.lastIndex > ancestorIndex) {
                    stack.removeAt(stack.lastIndex)
                    incomplete = true
                  }
                  stack.removeAt(stack.lastIndex)
                }
                else {
                  // JSXmlTokensParser consumes an unrelated closer as "matches nothing" and completes
                  // the current element as not closed. This gives the surrounding language a stable
                  // recovery boundary instead of discarding JSX or consuming unrelated Markdown.
                  stack.removeAt(stack.lastIndex)
                  incomplete = true
                }
              }
            }
          }
          offset = tag.range.last
          if (stack.isEmpty()) {
            return Element(start..offset, tags, expressions, incomplete, terminated = true)
          }
        }
        else -> offset++
      }
    }
    return Element(start..limit, tags, expressions, incomplete = true, terminated = false)
  }

  private fun isAtLineStart(text: CharSequence, offset: Int): Boolean {
    return offset == 0 || text.getOrNull(offset - 1) == '\n'
  }

  private fun parseTag(text: CharSequence, start: Int, limit: Int): Tag? {
    if (text.getOrNull(start) != '<' || start + 1 >= limit) return null
    var offset = start + 1
    val closing = text.getOrNull(offset) == '/'
    if (closing) offset++

    val name: String?
    if (!closing && text.getOrNull(offset) == '>') {
      name = null
      return Tag(start..offset + 1, TagKind.OPENING, name, emptyList(), emptyList())
    }
    else if (closing && text.getOrNull(offset) == '>') {
      name = null
      return Tag(start..offset + 1, TagKind.CLOSING, name, emptyList(), emptyList())
    }
    else {
      val nameStart = offset
      if (!isNameStart(text.getOrNull(offset))) return null
      offset++
      while (offset < limit && isNamePart(text[offset])) {
        offset++
      }
      name = text.subSequence(nameStart, offset).toString()
    }

    if (closing) {
      offset = skipSpaces(text, offset, limit)
      return if (text.getOrNull(offset) == '>') {
        Tag(start..offset + 1, TagKind.CLOSING, name, emptyList(), emptyList())
      }
      else null
    }

    return parseOpeningTagTail(text, start, name, offset, limit, emptyList(), emptyList())
  }

  private fun parseOpeningTagTail(text: CharSequence,
                                  start: Int,
                                  name: String,
                                  initialOffset: Int,
                                  limit: Int,
                                  initialAttributes: List<IntRange>,
                                  initialExpressions: List<IntRange>): Tag? {
    val attributes = initialAttributes.toMutableList()
    val expressions = initialExpressions.toMutableList()
    var offset = initialOffset
    var selfClosing = false
    while (offset < limit) {
      offset = skipSpaces(text, offset, limit)
      when (text.getOrNull(offset)) {
        null -> return null
        '>' -> return Tag(start..offset + 1, if (selfClosing) TagKind.SELF_CLOSING else TagKind.OPENING, name, attributes, expressions)
        '/' -> {
          val next = skipSpaces(text, offset + 1, limit)
          if (text.getOrNull(next) == '>') {
            return Tag(start..next + 1, TagKind.SELF_CLOSING, name, attributes, expressions)
          }
          selfClosing = true
          offset++
        }
        '{' -> {
          val expressionEnds = MdxExpressionBoundaryScanner.findExpressionEndCandidates(text, offset, limit)
          if (expressionEnds.isEmpty()) return null
          if (expressionEnds.size > 1) {
            for (expressionEnd in expressionEnds) {
              parseOpeningTagTail(
                text,
                start,
                name,
                expressionEnd,
                limit,
                attributes,
                expressions + listOf(offset..expressionEnd),
              )?.let { return it }
            }
            return null
          }
          val expressionEnd = expressionEnds.single()
          expressions.add(offset..expressionEnd)
          offset = expressionEnd
        }
        else -> {
          val attributeStart = offset
          if (!isAttributeNameStart(text.getOrNull(offset))) return null
          offset++
          while (offset < limit && isAttributeNamePart(text[offset])) {
            offset++
          }
          offset = skipSpaces(text, offset, limit)
          if (text.getOrNull(offset) == '=') {
            offset = skipSpaces(text, offset + 1, limit)
            when (text.getOrNull(offset)) {
              '\'', '"' -> offset = scanQuoted(text, offset, limit, text[offset])
              '{' -> {
                val expressionEnds = MdxExpressionBoundaryScanner.findExpressionEndCandidates(text, offset, limit)
                if (expressionEnds.isEmpty()) return null
                if (expressionEnds.size > 1) {
                  for (expressionEnd in expressionEnds) {
                    parseOpeningTagTail(
                      text,
                      start,
                      name,
                      expressionEnd,
                      limit,
                      attributes + listOf(attributeStart..expressionEnd),
                      expressions + listOf(offset..expressionEnd),
                    )?.let { return it }
                  }
                  return null
                }
                val expressionEnd = expressionEnds.single()
                expressions.add(offset..expressionEnd)
                offset = expressionEnd
              }
              null -> return null
              else -> {
                while (offset < limit && !text[offset].isWhitespace() && text[offset] != '>' && text[offset] != '/') {
                  offset++
                }
              }
            }
            if (offset == -1) return null
          }
          attributes.add(attributeStart..offset)
        }
      }
    }
    return null
  }

  private fun isIncompleteOpeningTagStart(text: CharSequence, start: Int, limit: Int): Boolean {
    if (text.getOrNull(start) != '<' || start + 1 >= limit || text.getOrNull(start + 1) == '/') return false
    var offset = start + 1
    if (text.getOrNull(offset) == '>') return false
    if (!isNameStart(text.getOrNull(offset))) return false
    offset++
    while (offset < limit && isNamePart(text[offset])) {
      offset++
    }
    // A `>`/`<` outside an expression breaks the tag; one inside an unterminated `{...}` attribute
    // (e.g. an `=>` arrow) does not, so expressions are skipped as a unit rather than scanned char-by-char.
    while (offset < limit) {
      when (text[offset]) {
        '>', '<' -> return false
        '\n' -> return true
        '{' -> {
          val expressionEnd = MdxExpressionBoundaryScanner.findExpressionEnd(text, offset, limit)
          if (expressionEnd == -1) return true
          offset = expressionEnd
        }
        '\'', '"' -> {
          val quoteEnd = scanQuoted(text, offset, limit, text[offset])
          if (quoteEnd == -1) return true
          offset = quoteEnd
        }
        else -> offset++
      }
    }
    return true
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

  private fun skipSpaces(text: CharSequence, start: Int, limit: Int): Int {
    var offset = start
    while (offset < limit && text[offset].isWhitespace()) {
      offset++
    }
    return offset
  }

  private fun isNameStart(char: Char?): Boolean {
    return char != null && (char.isLetter() || char == '_')
  }

  private fun isNamePart(char: Char): Boolean {
    return char.isLetterOrDigit() || char == '_' || char == '-' || char == '.' || char == ':'
  }

  private fun isAttributeNameStart(char: Char?): Boolean {
    return char != null && (char.isLetter() || char == '_' || char == ':' || char == '$')
  }

  private fun isAttributeNamePart(char: Char): Boolean {
    return char.isLetterOrDigit() || char == '_' || char == '-' || char == '.' || char == ':' || char == '$'
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

}
