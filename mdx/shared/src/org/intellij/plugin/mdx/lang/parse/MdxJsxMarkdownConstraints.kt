package org.intellij.plugin.mdx.lang.parse

import org.intellij.markdown.parser.LookaheadText
import org.intellij.markdown.parser.constraints.MarkdownConstraints

internal class MdxJsxMarkdownConstraints(
  private val parent: MarkdownConstraints,
  private val blockStartIndent: Int,
  private val elementIdentity: MdxJsxScanner.ElementIdentity?,
  private val opaqueBlock: Boolean = false,
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
      return parent.startsWith(other.parent) &&
             blockStartIndent == other.blockStartIndent &&
             elementIdentity == other.elementIdentity
    }
    return parent.startsWith(other)
  }

  override fun containsListMarkers(upToIndex: Int): Boolean {
    return parent.containsListMarkers(upToIndex)
  }

  override fun addModifierIfNeeded(pos: LookaheadText.Position?): MarkdownConstraints? {
    // Delegate modifier recognition, but retain every JSX wrapper. Returning the modified parent
    // directly loses the closing boundary of nested JSX and lets a list item consume ancestor tags.
    val modifiedParent = parent.addModifierIfNeeded(pos) ?: return null
    return copy(parent = modifiedParent, charsEaten = modifiedParent.charsEaten)
  }

  override fun applyToNextLine(pos: LookaheadText.Position?): MarkdownConstraints {
    if (pos == null) {
      return parent.applyToNextLine(pos)
    }
    if (opaqueBlock) {
      val modifiedParent = parent.applyToNextLine(pos)
      val contentOffset = firstNonWhitespaceOffset(pos.currentLine, modifiedParent.charsEaten)
      return copy(
        parent = modifiedParent,
        charsEaten = if (contentOffset == -1) pos.currentLine.length else contentOffset,
      )
    }
    val modifiedParent = parent.applyToNextLine(pos)
    val nonWhitespaceOffset = firstNonWhitespaceOffset(pos.currentLine, modifiedParent.charsEaten)
    if (nonWhitespaceOffset == -1) {
      return copy(parent = modifiedParent, charsEaten = pos.currentLine.length)
    }
    if (nonWhitespaceOffset <= blockStartIndent &&
        closingBoundary(pos) != MdxJsxClosingBoundary.NONE) {
      return modifiedParent
    }
    if (nonWhitespaceOffset < blockStartIndent) {
      return modifiedParent
    }
    return copy(parent = modifiedParent, charsEaten = nonWhitespaceOffset)
  }

  fun asOpaqueBlockConstraints(): MdxJsxMarkdownConstraints {
    val opaqueParent = if (parent is MdxJsxMarkdownConstraints) parent.asOpaqueBlockConstraints() else parent
    return copy(parent = opaqueParent, opaqueBlock = true, charsEaten = charsEaten)
  }

  fun closingBoundary(pos: LookaheadText.Position): MdxJsxClosingBoundary {
    val markdownConstraints = markdownConstraints().applyToNextLine(pos)
    val tagOffset = firstNonWhitespaceOffset(pos.currentLine, markdownConstraints.charsEaten)
    if (tagOffset == -1) return MdxJsxClosingBoundary.NONE
    val closingIdentity = MdxJsxScanner.closingElementIdentity(pos.currentLine, tagOffset, pos.currentLine.length)
                          ?: return MdxJsxClosingBoundary.NONE

    var constraints: MarkdownConstraints = this
    var current = true
    while (constraints is MdxJsxMarkdownConstraints) {
      if (constraints.elementIdentity == closingIdentity) {
        return if (current) MdxJsxClosingBoundary.CURRENT else MdxJsxClosingBoundary.ANCESTOR
      }
      constraints = constraints.parent
      current = false
    }
    return MdxJsxClosingBoundary.MISMATCHED
  }

  private fun markdownConstraints(): MarkdownConstraints {
    var constraints = parent
    while (constraints is MdxJsxMarkdownConstraints) {
      constraints = constraints.parent
    }
    return constraints
  }

  private fun copy(
    parent: MarkdownConstraints = this.parent,
    opaqueBlock: Boolean = this.opaqueBlock,
    charsEaten: Int,
  ): MdxJsxMarkdownConstraints {
    return MdxJsxMarkdownConstraints(parent, blockStartIndent, elementIdentity, opaqueBlock, charsEaten)
  }

  private fun firstNonWhitespaceOffset(line: CharSequence, start: Int = 0): Int {
    var offset = start.coerceIn(0, line.length)
    while (offset < line.length) {
      if (line[offset] != ' ' && line[offset] != '\t') {
        return offset
      }
      offset++
    }
    return -1
  }
}

internal enum class MdxJsxClosingBoundary {
  NONE,
  CURRENT,
  ANCESTOR,
  MISMATCHED,
}
