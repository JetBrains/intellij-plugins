package org.intellij.plugin.mdx.lang.parse

/**
 * Finds code-fence boundaries before the nested Markdown AST exists.
 *
 * This is not a general Markdown parser. It mirrors the fence rules needed inside MDX JSX flow
 * bodies, where indentation is owned by the JSX constraints and therefore is not capped at three
 * spaces. Keep its shared marker and closing-line rules aligned with [MdxFlavourDescriptor].
 */
internal object MdxMarkdownFenceScanner {
  /**
   * If the line at [start] opens a fenced code block, returns the offset just past the closing fence,
   * or [limit] when the fence is unterminated. Returns `-1` when the line is not a fence opener.
   *
   * Any indentation is accepted because fences inside JSX flow bodies align with their container.
   */
  fun findEnd(text: CharSequence, start: Int, limit: Int = text.length): Int {
    val indent = fenceIndent(text, start, limit)
    val fenceStart = start + indent
    val fenceChar = text.getOrNull(fenceStart) ?: return -1
    if (fenceChar != '`' && fenceChar != '~') return -1

    var markerLength = 0
    while (fenceStart + markerLength < limit && text[fenceStart + markerLength] == fenceChar) {
      markerLength++
    }
    if (markerLength < 3) return -1

    // CommonMark disallows backticks in the info string of a backtick fence. Tilde fences have no
    // equivalent restriction. This also keeps an inline backtick span from looking like an opener.
    var infoEnd = fenceStart + markerLength
    while (infoEnd < limit && text[infoEnd] != '\n') {
      if (fenceChar == '`' && text[infoEnd] == '`') return -1
      infoEnd++
    }

    var lineStart = nextLineStart(text, infoEnd, limit)
    while (lineStart < limit) {
      val contentStart = lineStart + fenceIndent(text, lineStart, limit)
      var closeMarkerLength = 0
      while (contentStart + closeMarkerLength < limit && text[contentStart + closeMarkerLength] == fenceChar) {
        closeMarkerLength++
      }
      if (closeMarkerLength >= markerLength && isBlankUntilLineEnd(text, contentStart + closeMarkerLength, limit)) {
        return lineEnd(text, contentStart + closeMarkerLength, limit)
      }
      lineStart = nextLineStart(text, lineEnd(text, lineStart, limit), limit)
    }
    return limit
  }

  /** Returns whether [offset] is inside a fence that opens at or after the line start [from]. */
  fun containsOffset(text: CharSequence, from: Int, offset: Int): Boolean {
    var lineStart = from.coerceAtLeast(0)
    while (lineStart < offset && lineStart < text.length) {
      val fenceEnd = findEnd(text, lineStart)
      if (fenceEnd != -1) {
        if (offset < fenceEnd) return true
        lineStart = nextLineStart(text, fenceEnd, text.length)
      }
      else {
        lineStart = nextLineStart(text, lineEnd(text, lineStart, text.length), text.length)
      }
    }
    return false
  }

  private fun fenceIndent(text: CharSequence, lineStart: Int, limit: Int): Int {
    var indent = 0
    while (lineStart + indent < limit && text[lineStart + indent] == ' ') {
      indent++
    }
    return indent
  }

  private fun isBlankUntilLineEnd(text: CharSequence, start: Int, limit: Int): Boolean {
    var offset = start
    while (offset < limit && text[offset] != '\n') {
      if (!text[offset].isWhitespace()) return false
      offset++
    }
    return true
  }

  private fun lineEnd(text: CharSequence, start: Int, limit: Int): Int {
    var offset = start
    while (offset < limit && text[offset] != '\n') {
      offset++
    }
    return offset
  }

  private fun nextLineStart(text: CharSequence, lineEnd: Int, limit: Int): Int {
    return if (lineEnd < limit && text[lineEnd] == '\n') lineEnd + 1 else limit
  }

  private fun CharSequence.getOrNull(index: Int): Char? {
    return if (index in indices) this[index] else null
  }
}
