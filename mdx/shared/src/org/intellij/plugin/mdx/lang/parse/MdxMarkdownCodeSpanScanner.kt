package org.intellij.plugin.mdx.lang.parse

/** Finds Markdown code spans before paragraph inline parsing has run. */
internal object MdxMarkdownCodeSpanScanner {
  fun findRanges(
    text: CharSequence,
    start: Int,
    limit: Int,
    opaqueRanges: MdxOpaqueRanges = MdxOpaqueRanges.EMPTY,
  ): List<IntRange> {
    val result = mutableListOf<IntRange>()
    var offset = start
    while (offset < limit) {
      val opaqueEnd = opaqueRanges.endOffsetContaining(offset)
      if (opaqueEnd != null) {
        offset = opaqueEnd.coerceAtMost(limit)
        continue
      }
      if (text[offset] != '`' || isEscaped(text, offset, start)) {
        offset++
        continue
      }

      val delimiterLength = backtickRunLength(text, offset, limit)
      if (isFenceOpening(text, offset, delimiterLength, start, limit)) {
        offset += delimiterLength
        continue
      }
      var candidate = offset + delimiterLength
      var closingEnd = -1
      while (candidate < limit) {
        val candidateOpaqueEnd = opaqueRanges.endOffsetContaining(candidate)
        if (candidateOpaqueEnd != null) {
          candidate = candidateOpaqueEnd.coerceAtMost(limit)
          continue
        }
        if (text[candidate] == '\n' && startsPotentialMarkdownBlock(text, candidate + 1, limit)) {
          break
        }
        if (text[candidate] != '`') {
          candidate++
          continue
        }
        val candidateLength = backtickRunLength(text, candidate, limit)
        if (candidateLength == delimiterLength) {
          closingEnd = candidate + candidateLength
          break
        }
        candidate += candidateLength
      }

      if (closingEnd == -1) {
        offset += delimiterLength
      }
      else {
        result.add(offset..closingEnd)
        offset = closingEnd
      }
    }
    return result
  }

  private fun backtickRunLength(text: CharSequence, start: Int, limit: Int): Int {
    var offset = start
    while (offset < limit && text[offset] == '`') {
      offset++
    }
    return offset - start
  }

  private fun isEscaped(text: CharSequence, offset: Int, lowerBound: Int): Boolean {
    var backslashes = 0
    var index = offset - 1
    while (index >= lowerBound && text[index] == '\\') {
      backslashes++
      index--
    }
    return backslashes % 2 != 0
  }

  private fun isFenceOpening(
    text: CharSequence,
    offset: Int,
    delimiterLength: Int,
    lowerBound: Int,
    limit: Int,
  ): Boolean {
    if (delimiterLength < 3) return false
    var lineStart = offset
    while (lineStart > lowerBound && text[lineStart - 1] != '\n') {
      lineStart--
    }
    if (text.subSequence(lineStart, offset).any { it != ' ' && it != '\t' }) return false
    var infoOffset = offset + delimiterLength
    while (infoOffset < limit && text[infoOffset] != '\n') {
      if (text[infoOffset] == '`') return false
      infoOffset++
    }
    return true
  }

  /**
   * Stops provisional inline lookahead before syntax that can start a sibling block. False
   * positives are intentional: failing to hide a code-like range is safer than hiding a JSX
   * boundary across a block that the Markdown parser will own.
   */
  private fun startsPotentialMarkdownBlock(text: CharSequence, lineStart: Int, limit: Int): Boolean {
    var offset = lineStart
    while (offset < limit && (text[offset] == ' ' || text[offset] == '\t' || text[offset] == '\r')) {
      offset++
    }
    return offset >= limit ||
           text[offset] == '\n' ||
           when (text[offset]) {
             '<', '{', '>' -> true
             '#' -> offset + 1 >= limit || text[offset + 1].isWhitespace()
             '`', '~' -> repeatedCharacterCount(text, offset, limit) >= 3
             '-', '+', '*' -> offset + 1 < limit && text[offset + 1].isWhitespace()
             in '0'..'9' -> isOrderedListMarker(text, offset, limit)
             else -> false
           }
  }

  private fun repeatedCharacterCount(text: CharSequence, offset: Int, limit: Int): Int {
    val marker = text[offset]
    var end = offset
    while (end < limit && text[end] == marker) {
      end++
    }
    return end - offset
  }

  private fun isOrderedListMarker(text: CharSequence, start: Int, limit: Int): Boolean {
    var offset = start
    while (offset < limit && text[offset] in '0'..'9' && offset - start < 9) {
      offset++
    }
    return offset < limit &&
           (text[offset] == '.' || text[offset] == ')') &&
           offset + 1 < limit &&
           text[offset + 1].isWhitespace()
  }
}
