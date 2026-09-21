package org.intellij.plugin.mdx.lang.parse

import com.intellij.openapi.util.TextRange

/** Finds code spans within paragraph segments supplied by the Markdown owner. */
internal object MdxMarkdownCodeSpanScanner {
  internal class Session(
    source: CharSequence,
    start: Int,
    private val scanEnd: Int = source.length,
    private val paragraphEnd: (Int) -> Int = { scanEnd },
  ) {
    private val text = mdxCancellableText(source)
    private var cursor = start
    private var lineStart = start
    private var precedingBackslashes = 0
    private var exposedEnd = start
    private var activeSegment: Segment? = null
    private var nextSpanIndex = 0

    fun advanceTo(limit: Int, opaqueRanges: MdxOpaqueRangeLookup = MdxTextRangeSet.EMPTY): List<TextRange> {
      mdxCancellableText(text)
      require(limit in exposedEnd..scanEnd) {
        "Code-span scan limit must advance from $exposedEnd to at most $scanEnd: $limit"
      }
      exposedEnd = limit
      val result = mutableListOf<TextRange>()

      while (true) {
        val segment = activeSegment
        if (segment != null) {
          while (nextSpanIndex < segment.spans.size && segment.spans[nextSpanIndex].startOffset < limit) {
            result.add(segment.spans[nextSpanIndex])
            nextSpanIndex++
          }
          if (limit < segment.endOffset) return result

          cursor = segment.endOffset
          lineStart = cursor
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
        activeSegment = indexParagraphSegment(cursor, lineStart, opaqueRanges)
      }
    }

    private fun indexParagraphSegment(
      startOffset: Int,
      initialLineStart: Int,
      opaqueRanges: MdxOpaqueRangeLookup,
    ): Segment {
      val runs = mutableListOf<BacktickRun>()
      val end = paragraphEnd(initialLineStart).coerceAtMost(scanEnd)
      check(end > startOffset) { "A Markdown segment must include its code-span opener" }
      var offset = startOffset
      var backslashes = 0
      while (offset < end) {
        val opaqueEnd = opaqueRanges.endOffsetContaining(offset)
        if (opaqueEnd != null) {
          offset = opaqueEnd.coerceAtMost(end)
          backslashes = 0
          continue
        }

        val char = text[offset]
        if (char == '\\') {
          backslashes++
          offset++
          continue
        }
        if (char != '`') {
          backslashes = 0
          offset++
          continue
        }

        val runStart = offset
        while (offset < end && text[offset] == '`') {
          offset++
        }
        runs.add(BacktickRun(runStart, offset - runStart, canOpen = backslashes % 2 == 0))
        backslashes = 0
      }
      return Segment(end, pairRuns(runs))
    }

    private fun pairRuns(runs: List<BacktickRun>): List<TextRange> {
      if (runs.isEmpty()) return emptyList()
      val nextEqualLength = IntArray(runs.size) { -1 }
      val latestByLength = HashMap<Int, Int>()
      for (index in runs.indices.reversed()) {
        nextEqualLength[index] = latestByLength.put(runs[index].length, index) ?: -1
      }

      val result = mutableListOf<TextRange>()
      var index = 0
      while (index < runs.size) {
        val closingIndex = nextEqualLength[index]
        if (!runs[index].canOpen || closingIndex == -1) {
          index++
          continue
        }
        val closing = runs[closingIndex]
        result.add(TextRange(runs[index].start, closing.start + closing.length))
        index = closingIndex + 1
      }
      return result
    }

    private fun consumeBacktickRun() {
      while (cursor < scanEnd && text[cursor] == '`') {
        cursor++
      }
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
          precedingBackslashes = 0
        }
        '\\' -> {
          precedingBackslashes++
        }
        else -> {
          precedingBackslashes = 0
        }
      }
    }

    private data class Segment(val endOffset: Int, val spans: List<TextRange>)

    private data class BacktickRun(val start: Int, val length: Int, val canOpen: Boolean)
  }
}
