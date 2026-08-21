package org.intellij.plugin.mdx.lang.parse

import org.intellij.markdown.parser.LookaheadText
import org.intellij.markdown.parser.MarkerProcessor
import org.intellij.markdown.parser.ProductionHolder
import org.intellij.markdown.parser.constraints.MarkdownConstraints
import org.intellij.markdown.parser.markerblocks.MarkerBlock
import org.intellij.markdown.parser.markerblocks.MarkerBlockProvider
import org.intellij.markdown.parser.markerblocks.impl.HtmlBlockMarkerBlock

internal object MdxHtmlCommentBoundary {
  fun findClosedMultilineCommentEnd(text: CharSequence, start: Int, limit: Int = text.length): Int {
    if (start < 0 || start + COMMENT_START.length > limit || !text.startsWith(COMMENT_START, start)) {
      return -1
    }
    val endMarker = text.indexOf(COMMENT_END, start + COMMENT_START.length)
    if (endMarker < 0 || endMarker + COMMENT_END.length > limit) {
      return -1
    }
    for (offset in start + COMMENT_START.length..<endMarker) {
      if (text[offset] == '\n') {
        return endMarker + COMMENT_END.length
      }
    }
    return -1
  }

  private const val COMMENT_START = "<!--"
  private const val COMMENT_END = "-->"
}

/**
 * Gives closed multiline HTML comments the same opaque block ownership as CommonMark. Single-line
 * comments remain ordinary MDX input so the JSX layer can continue reporting them as invalid MDX.
 */
internal class MdxHtmlCommentBlockProvider : MarkerBlockProvider<MarkerProcessor.StateInfo> {
  override fun createMarkerBlocks(pos: LookaheadText.Position,
                                  productionHolder: ProductionHolder,
                                  stateInfo: MarkerProcessor.StateInfo): List<MarkerBlock> {
    if (!isClosedMultilineCommentStart(pos, stateInfo.currentConstraints)) {
      return emptyList()
    }
    return listOf(HtmlBlockMarkerBlock(stateInfo.currentConstraints, productionHolder, COMMENT_END, pos))
  }

  override fun interruptsParagraph(pos: LookaheadText.Position, constraints: MarkdownConstraints): Boolean {
    return isClosedMultilineCommentStart(pos, constraints)
  }

  private fun isClosedMultilineCommentStart(pos: LookaheadText.Position, constraints: MarkdownConstraints): Boolean {
    if (!MarkerBlockProvider.isStartOfLineWithConstraints(pos, constraints)) {
      return false
    }
    val line = pos.currentLineFromPosition
    val indent = MarkerBlockProvider.passSmallIndent(line)
    val start = pos.offset + indent
    return MdxHtmlCommentBoundary.findClosedMultilineCommentEnd(pos.originalText, start) != -1
  }

  private companion object {
    private val COMMENT_END = Regex("-->")
  }
}
