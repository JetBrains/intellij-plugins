package org.intellij.plugin.mdx.lang.parse

import com.intellij.openapi.util.TextRange
import org.intellij.markdown.parser.LookaheadText
import org.intellij.markdown.parser.MarkerProcessor
import org.intellij.markdown.parser.ProductionHolder
import org.intellij.markdown.parser.constraints.MarkdownConstraints
import org.intellij.markdown.parser.markerblocks.MarkerBlock
import org.intellij.markdown.parser.markerblocks.MarkerBlockProvider
import org.intellij.markdown.parser.markerblocks.impl.ParagraphMarkerBlock
import java.util.TreeMap

/** Shares the block parser's constraints and providers with provisional Markdown scans. */
internal class MdxMarkdownOwnership(
  private val providers: () -> List<MarkerBlockProvider<MarkerProcessor.StateInfo>>,
) {
  private val lines = TreeMap<Int, Line>()

  fun observeLine(pos: LookaheadText.Position, constraints: MarkdownConstraints): TextRange? {
    var current = constraints
    while (current.charsEaten < pos.currentLine.length) {
      val content = pos.nextPosition(current.charsEaten - pos.offsetInCurrentLine) ?: break
      current = current.addModifierIfNeeded(content) ?: break
    }
    val lineStart = pos.offset - pos.offsetInCurrentLine
    lines[lineStart] = Line(pos, current)
    val content = contentPosition(pos, current) ?: return null
    return if (providers().any { it is MdxCodeFenceProvider && it.interruptsParagraph(content, current) }) {
      TextRange(lineStart, pos.nextLineOrEofOffset)
    }
    else null
  }

  fun paragraphEnd(lineStart: Int): Int {
    val line = checkNotNull(lines.floorEntry(lineStart)).value
    val content = contentPosition(line.position, line.constraints)
    if (content == null || interruptsParagraph(content, line.constraints)) return line.position.nextLineOrEofOffset
    return paragraphEnd(line.position, line.constraints, line.position.originalText.length)
  }

  fun flowParagraphRange(
    element: MdxJsxScanner.Element,
    constraints: MarkdownConstraints,
    shift: Int = 0,
  ): TextRange? {
    val opening = element.tags.firstOrNull() ?: return null
    val closing = element.tags.lastOrNull() ?: return null
    if (opening.kind != MdxJsxScanner.TagKind.OPENING || closing.kind != MdxJsxScanner.TagKind.CLOSING) return null
    val start = opening.range.endOffset + shift
    val end = closing.range.startOffset + shift
    if (start >= end) return null

    val line = checkNotNull(lines.floorEntry(start)).value
    val text = line.position.originalText
    val firstLineEnd = line.position.nextLineOrEofOffset.coerceAtMost(end)
    if (text.subSequence(start, firstLineEnd).isBlank()) return null
    var paragraphEnd = paragraphEnd(line.position, constraints, end)
    val multiline = paragraphEnd > firstLineEnd
    while (paragraphEnd > start && if (multiline) text[paragraphEnd - 1] == '\n' || text[paragraphEnd - 1] == '\r'
      else text[paragraphEnd - 1].isWhitespace()) {
      paragraphEnd--
    }
    return TextRange(start, paragraphEnd).takeUnless { it.isEmpty }
  }

  private fun paragraphEnd(pos: LookaheadText.Position, constraints: MarkdownConstraints, limit: Int): Int {
    val paragraph = ParagraphMarkerBlock(constraints, ProductionHolder().mark(), ::interruptsParagraph)
    var next = pos.nextLinePosition()
    while (next != null && next.offset < limit) {
      if (paragraph.processToken(next, constraints) != MarkerBlock.ProcessingResult.CANCEL) {
        return (next.offset + 1).coerceAtMost(limit)
      }
      next = next.nextLinePosition()
    }
    return limit
  }

  private fun interruptsParagraph(pos: LookaheadText.Position, constraints: MarkdownConstraints): Boolean {
    return providers().any { it.interruptsParagraph(pos, constraints) }
  }

  private fun contentPosition(pos: LookaheadText.Position, constraints: MarkdownConstraints): LookaheadText.Position? {
    if (constraints.charsEaten >= pos.currentLine.length) return null
    return pos.nextPosition(constraints.charsEaten - pos.offsetInCurrentLine)
  }

  private data class Line(val position: LookaheadText.Position, val constraints: MarkdownConstraints)
}
