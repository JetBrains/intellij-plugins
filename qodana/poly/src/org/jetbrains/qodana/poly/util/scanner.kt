package org.jetbrains.qodana.poly.util

/**
 * What one line keeps, after a comment and a string go away.
 *
 * [widestGap] is the widest whitespace run that separates code on the line. Trailing whitespace
 * separates nothing, so it is not a gap.
 */
internal data class CodeLine(val code: Int, val spaces: Int, val widestGap: Int)

/**
 * Measures one line at a time.
 *
 * A block comment is the only construct that crosses a line break. A line comment ends at the break
 * by definition, and [endOfString] closes a string there too. [inBlockComment] is therefore the only
 * state that one line hands to the next.
 */
internal class CodeLineScanner {
  private var inBlockComment = false

  fun scan(line: CharSequence): CodeLine {
    var code = 0
    var spaces = 0
    var widestGap = 0
    var offset = 0

    scan@ while (offset < line.length) {
      val c = line[offset]
      when {
        inBlockComment || line.startsWith("/*", offset) -> {
          offset = endOfBlockComment(line, offset)
          if (inBlockComment) break@scan
        }
        line.startsWith("//", offset) -> break@scan
        c == '"' || c == '\'' || c == '`' -> offset = endOfString(line, offset)
        c.isWhitespace() -> {
          val end = endOfWhitespace(line, offset)
          if (end < line.length) widestGap = maxOf(widestGap, end - offset)
          spaces += end - offset
          offset = end
        }
        else -> {
          code++
          offset++
        }
      }
    }

    return CodeLine(code, spaces, widestGap)
  }

  /**
   * Advances past a block comment, and sets [inBlockComment] when the comment stays open.
   */
  private fun endOfBlockComment(line: CharSequence, start: Int): Int {
    val opensHere = !inBlockComment
    val end = line.indexOf("*/", if (opensHere) start + 2 else start)
    inBlockComment = end < 0
    return if (end < 0) line.length else end + 2
  }

  private fun endOfWhitespace(line: CharSequence, start: Int): Int {
    var offset = start
    while (offset < line.length && line[offset].isWhitespace()) offset++
    return offset
  }

  /**
   * Ends a string at the matching quote, or at the end of [line].
   */
  private fun endOfString(line: CharSequence, start: Int): Int {
    val quote = line[start]
    var offset = start + 1
    while (offset < line.length) {
      when {
        line[offset] == '\\' -> offset += 2
        line[offset] == quote -> return offset + 1
        else -> offset++
      }
    }
    return line.length
  }
}
