package org.intellij.plugin.mdx.lang.parse

import org.intellij.markdown.parser.LookaheadText
import org.intellij.markdown.parser.constraints.MarkdownConstraints

internal class MdxJsxMarkdownConstraints(
  private val parent: MarkdownConstraints,
  private val blockStartIndent: Int,
  override val charsEaten: Int = blockStartIndent,
) : MarkdownConstraints {
  override val indent: Int
    get() = charsEaten

  override val types: CharArray
    get() = parent.types

  override val isExplicit: BooleanArray
    get() = parent.isExplicit

  override fun startsWith(other: MarkdownConstraints): Boolean {
    if (other === this) return true
    if (other is MdxJsxMarkdownConstraints) {
      return parent.startsWith(other.parent) && blockStartIndent == other.blockStartIndent
    }
    return parent.startsWith(other)
  }

  override fun containsListMarkers(upToIndex: Int): Boolean {
    return parent.containsListMarkers(upToIndex)
  }

  override fun addModifierIfNeeded(pos: LookaheadText.Position?): MarkdownConstraints? {
    return null
  }

  override fun applyToNextLine(pos: LookaheadText.Position?): MarkdownConstraints {
    if (pos == null) {
      return parent.applyToNextLine(pos)
    }
    val lineIndent = leadingSpaces(pos.currentLine)
    val nonWhitespaceOffset = firstNonWhitespaceOffset(pos.currentLine)
    if (nonWhitespaceOffset == -1) {
      return copy(charsEaten = pos.currentLine.length)
    }
    if (lineIndent <= blockStartIndent && pos.currentLine.startsWith("</", nonWhitespaceOffset)) {
      return parent.applyToNextLine(pos)
    }
    if (lineIndent < blockStartIndent) {
      return parent.applyToNextLine(pos)
    }
    return copy(charsEaten = nonWhitespaceOffset)
  }

  private fun copy(charsEaten: Int): MdxJsxMarkdownConstraints {
    return MdxJsxMarkdownConstraints(parent, blockStartIndent, charsEaten)
  }

  private fun leadingSpaces(line: CharSequence): Int {
    var offset = 0
    while (offset < line.length && line[offset] == ' ') {
      offset++
    }
    return offset
  }

  private fun firstNonWhitespaceOffset(line: CharSequence): Int {
    var offset = 0
    while (offset < line.length) {
      if (line[offset] != ' ' && line[offset] != '\t') {
        return offset
      }
      offset++
    }
    return -1
  }
}
