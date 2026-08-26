package org.intellij.plugin.mdx.lang.parse

/** Finds Markdown code spans before paragraph inline parsing has run. */
internal object MdxMarkdownCodeSpanScanner {
  internal class Session(
    source: CharSequence,
    start: Int,
    private val scanEnd: Int = source.length,
  ) {
    private val text = mdxCancellableText(source)
    private var cursor = start
    private var lineStart = start
    private var onlyWhitespaceOnLine = true
    private var precedingBackslashes = 0
    private var exposedEnd = start
    private var activeSegment: Segment? = null
    private var nextSpanIndex = 0

    fun advanceTo(limit: Int, opaqueRanges: MdxOpaqueRangeLookup = MdxOpaqueRanges.EMPTY): List<IntRange> {
      mdxCancellableText(text)
      require(limit in exposedEnd..scanEnd) {
        "Code-span scan limit must advance from $exposedEnd to at most $scanEnd: $limit"
      }
      exposedEnd = limit
      val result = mutableListOf<IntRange>()

      while (true) {
        val segment = activeSegment
        if (segment != null) {
          while (nextSpanIndex < segment.spans.size && segment.spans[nextSpanIndex].first < limit) {
            result.add(segment.spans[nextSpanIndex])
            nextSpanIndex++
          }
          if (limit < segment.endOffset) return result

          cursor = segment.endOffset
          lineStart = cursor
          onlyWhitespaceOnLine = true
          precedingBackslashes = 0
          activeSegment = null
          nextSpanIndex = 0
          continue
        }
        if (cursor >= limit) return result

        val opaqueEnd = opaqueRanges.endOffsetContaining(cursor)
        if (opaqueEnd != null) {
          consumeThrough(opaqueEnd.coerceAtMost(limit))
          continue
        }

        val char = text[cursor]
        if (char != '`') {
          consume(char)
          continue
        }

        if (precedingBackslashes % 2 != 0) {
          consumeBacktickRun()
          continue
        }
        activeSegment = indexParagraphSegment(cursor, lineStart, onlyWhitespaceOnLine, opaqueRanges)
      }
    }

    private fun indexParagraphSegment(
      startOffset: Int,
      initialLineStart: Int,
      initialOnlyWhitespace: Boolean,
      opaqueRanges: MdxOpaqueRangeLookup,
    ): Segment {
      val runs = mutableListOf<BacktickRun>()
      var currentLineStart = initialLineStart
      var offset = startOffset
      var firstLine = true
      var onlyWhitespace = initialOnlyWhitespace
      while (offset < scanEnd) {
        val blockLine = opaqueRanges.endOffsetContaining(currentLineStart) == null &&
                        startsPotentialMarkdownBlock(text, currentLineStart, scanEnd)
        if (!firstLine && blockLine) break

        offset = collectLineRuns(offset, onlyWhitespace, opaqueRanges, runs)
        if (offset >= scanEnd || blockLine) break

        currentLineStart = offset
        onlyWhitespace = true
        firstLine = false
      }
      return Segment(offset, pairRuns(runs))
    }

    private fun collectLineRuns(
      startOffset: Int,
      initialOnlyWhitespace: Boolean,
      opaqueRanges: MdxOpaqueRangeLookup,
      runs: MutableList<BacktickRun>,
    ): Int {
      var offset = startOffset
      var onlyWhitespace = initialOnlyWhitespace
      var backslashes = 0
      var possibleFenceIndex = -1
      while (offset < scanEnd) {
        val opaqueEnd = opaqueRanges.endOffsetContaining(offset)
        if (opaqueEnd != null) {
          val end = opaqueEnd.coerceAtMost(scanEnd)
          while (offset < end) {
            val char = text[offset]
            offset++
            if (char == '\n') {
              if (possibleFenceIndex != -1) runs[possibleFenceIndex].canOpen = false
              return offset
            }
            onlyWhitespace = false
          }
          backslashes = 0
          continue
        }

        val char = text[offset]
        if (char == '\n') {
          if (possibleFenceIndex != -1) runs[possibleFenceIndex].canOpen = false
          return offset + 1
        }
        if (char == '\\') {
          onlyWhitespace = false
          backslashes++
          offset++
          continue
        }
        if (char != '`') {
          if (char != ' ' && char != '\t') onlyWhitespace = false
          backslashes = 0
          offset++
          continue
        }

        if (possibleFenceIndex != -1) possibleFenceIndex = -1
        val runStart = offset
        while (offset < scanEnd && text[offset] == '`') {
          offset++
        }
        val run = BacktickRun(runStart, offset - runStart, canOpen = backslashes % 2 == 0)
        runs.add(run)
        if (run.length >= 3 && onlyWhitespace) {
          possibleFenceIndex = runs.lastIndex
        }
        onlyWhitespace = false
        backslashes = 0
      }
      if (possibleFenceIndex != -1) runs[possibleFenceIndex].canOpen = false
      return offset
    }

    private fun pairRuns(runs: List<BacktickRun>): List<IntRange> {
      if (runs.isEmpty()) return emptyList()
      val nextEqualLength = IntArray(runs.size) { -1 }
      val latestByLength = HashMap<Int, Int>()
      for (index in runs.indices.reversed()) {
        nextEqualLength[index] = latestByLength.put(runs[index].length, index) ?: -1
      }

      val result = mutableListOf<IntRange>()
      var index = 0
      while (index < runs.size) {
        val closingIndex = nextEqualLength[index]
        if (!runs[index].canOpen || closingIndex == -1) {
          index++
          continue
        }
        val closing = runs[closingIndex]
        result.add(runs[index].start..closing.start + closing.length)
        index = closingIndex + 1
      }
      return result
    }

    private fun consumeBacktickRun() {
      while (cursor < scanEnd && text[cursor] == '`') {
        cursor++
      }
      onlyWhitespaceOnLine = false
      precedingBackslashes = 0
    }

    private fun consumeThrough(endOffset: Int) {
      while (cursor < endOffset) {
        consume(text[cursor])
      }
    }

    private fun consume(char: Char) {
      cursor++
      when (char) {
        '\n' -> {
          lineStart = cursor
          onlyWhitespaceOnLine = true
          precedingBackslashes = 0
        }
        '\\' -> {
          onlyWhitespaceOnLine = false
          precedingBackslashes++
        }
        else -> {
          if (char != ' ' && char != '\t') onlyWhitespaceOnLine = false
          precedingBackslashes = 0
        }
      }
    }

    private data class Segment(val endOffset: Int, val spans: List<IntRange>)

    private data class BacktickRun(val start: Int, val length: Int, var canOpen: Boolean)
  }

  /**
   * Stops provisional inline lookahead before syntax that can start a sibling block. False
   * positives are intentional: failing to hide a code-like range is safer than hiding a JSX
   * boundary across a block that the Markdown parser will own.
   */
  private fun startsPotentialMarkdownBlock(
    text: CharSequence,
    lineStart: Int,
    limit: Int,
  ): Boolean {
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
