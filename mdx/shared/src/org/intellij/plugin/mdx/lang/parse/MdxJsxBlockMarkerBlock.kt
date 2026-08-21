package org.intellij.plugin.mdx.lang.parse

import org.intellij.markdown.parser.LookaheadText
import org.intellij.markdown.parser.ProductionHolder
import org.intellij.markdown.parser.constraints.MarkdownConstraints
import org.intellij.markdown.parser.constraints.extendsPrev
import org.intellij.markdown.parser.markerblocks.MarkerBlock
import org.intellij.markdown.parser.sequentialparsers.SequentialParser

internal class MdxJsxBlockMarkerBlock(myConstraints: MarkdownConstraints,
                                      productionHolder: ProductionHolder,
                                      blockStartOffset: Int,
                                      blockStartIndent: Int,
                                      source: CharSequence,
                                      initialText: String) : MdxBlockMarkerBlock(
  MdxJsxMarkdownConstraints(myConstraints, blockStartIndent, blockStartOffset),
  productionHolder,
  blockStartOffset,
  source,
  initialText,
) {
  private val hasJsxParent = myConstraints is MdxJsxMarkdownConstraints
  private val terminatedElement = MdxJsxScanner.scanJsxElement(source, blockStartOffset)?.takeIf { it.terminated }
  private var suppressSubBlocks = false

  override fun allowsSubBlocks(): Boolean {
    return !closeScheduled && !suppressSubBlocks && hasCompleteOpeningTag()
  }

  override fun prepareForLine() {
    suppressSubBlocks = false
  }

  override fun appendLine(candidateEndOffset: Int) {
    val hadCompleteOpeningTag = hasCompleteOpeningTag()
    super.appendLine(candidateEndOffset)
    if (!hadCompleteOpeningTag && hasCompleteOpeningTag() && !isCurrentBlockTerminated()) {
      suppressSubBlocks = true
    }
  }

  override fun activeProcessingResult(): MarkerBlock.ProcessingResult {
    return if (hasJsxParent && terminatedElement != null) {
      MarkerBlock.ProcessingResult.PASS
    }
    else {
      MarkerBlock.ProcessingResult.CANCEL
    }
  }

  override fun shouldAppendLine(pos: LookaheadText.Position, candidateEndOffset: Int): Boolean {
    return terminatedElement != null ||
           isTerminated(candidateEndOffset) ||
           constraints.applyToNextLine(pos).extendsPrev(constraints) ||
           MdxJsxScanner.scanJsxElement(source, blockStartOffset, candidateEndOffset) != null
  }

  override fun isTerminated(candidateEndOffset: Int): Boolean {
    return terminatedElement?.range?.last?.let { candidateEndOffset >= it }
           ?: (MdxJsxScanner.scanJsxElement(source, blockStartOffset, candidateEndOffset)?.terminated == true)
  }

  override fun createNodes(): List<SequentialParser.Node> {
    val element = terminatedElement ?: MdxJsxScanner.scanJsxElement(source, blockStartOffset, currentEndOffset)
    // A marker may be finalized merely because a nested Markdown block takes over. Its current
    // range is then only a scanner snapshot and can cross the nested block's eventual JSX root.
    // Publish recovered roots only at boundaries that cannot later move: a consumed closing tag
    // or the actual end of input.
    if (element == null) {
      val openingPrefix = MdxJsxScanner.incompleteOpeningTagRange(source, blockStartOffset, currentEndOffset)
      return if (openingPrefix == null) {
        emptyList()
      }
      else {
        listOf(
          SequentialParser.Node(openingPrefix, MdxMarkdownLibElementTypes.MDX_JSX_OPENING_ELEMENT),
          SequentialParser.Node(openingPrefix, MdxMarkdownLibElementTypes.MDX_JSX_FLOW_ELEMENT),
        )
      }
    }
    if (!element.terminated && currentEndOffset < source.length) {
      return emptyList()
    }
    return MdxBlockNodeFactory.createFlowElementNodes(source, element)
  }

  private fun hasCompleteOpeningTag(): Boolean {
    val openingTagEnd = terminatedElement?.tags?.firstOrNull()?.range?.last
    return openingTagEnd?.let { currentEndOffset >= it }
           ?: (MdxJsxScanner.scanJsxElement(source, blockStartOffset, currentEndOffset) != null)
  }
}
