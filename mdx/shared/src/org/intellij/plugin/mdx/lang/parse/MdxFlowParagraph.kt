package org.intellij.plugin.mdx.lang.parse

import org.intellij.markdown.parser.LookaheadText
import org.intellij.markdown.parser.ProductionHolder
import org.intellij.markdown.parser.constraints.MarkdownConstraints
import org.intellij.markdown.parser.markerblocks.impl.ParagraphMarkerBlock
import org.intellij.markdown.parser.sequentialparsers.SequentialParser

/** Keeps the standard paragraph marker while its JSX owner controls the output range. */
internal class MdxFlowParagraph(
  private val start: Int,
  end: Int,
  constraints: MarkdownConstraints,
  private val owner: MdxJsxBlockMarkerBlock,
  private val source: CharSequence,
  interruptsParagraph: (LookaheadText.Position, MarkdownConstraints) -> Boolean,
  private val onContinuation: (LookaheadText.Position) -> Unit,
) {
  private val firstLineEnd = end
  private val productionHolder = ProductionHolder().apply { updatePosition(start) }
  val marker = ParagraphMarkerBlock(constraints, productionHolder.mark()) { pos, nextConstraints ->
    if (owner.bodyEnd != null || interruptsParagraph(pos, nextConstraints)) {
      true
    }
    else {
      continueParagraph(pos)
      false
    }
  }

  init {
    advanceTo(end)
  }

  val production: List<SequentialParser.Node>
    get() = productionHolder.production

  private fun continueParagraph(pos: LookaheadText.Position) {
    onContinuation(pos)
    advanceTo(pos.nextLineOrEofOffset)
  }

  private fun advanceTo(end: Int) {
    var contentEnd = owner.bodyEnd?.coerceAtMost(end) ?: end
    val multiline = contentEnd > firstLineEnd
    while (contentEnd > start && if (multiline) source[contentEnd - 1] == '\n' || source[contentEnd - 1] == '\r'
      else source[contentEnd - 1].isWhitespace()) contentEnd--
    productionHolder.updatePosition(contentEnd)
  }
}
