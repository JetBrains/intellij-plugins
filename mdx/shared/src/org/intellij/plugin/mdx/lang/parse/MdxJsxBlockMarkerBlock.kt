package org.intellij.plugin.mdx.lang.parse

import com.intellij.openapi.util.TextRange
import org.intellij.markdown.MarkdownElementTypes
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
                                      elementIdentity: MdxJsxScanner.ElementIdentity?,
                                      source: CharSequence,
                                      initialText: String) : MdxBlockMarkerBlock(
  MdxJsxMarkdownConstraints(myConstraints, blockStartIndent, elementIdentity),
  productionHolder,
  blockStartOffset,
  source,
  initialText,
) {
  private val hasJsxParent = myConstraints is MdxJsxMarkdownConstraints
  private val jsxSession = MdxJsxScanner.Session(source, blockStartOffset)
  private val codeSpanSession = MdxMarkdownCodeSpanScanner.Session(source, blockStartOffset)
  private var suppressSubBlocks = false
  private var processedProductionCount = initialProductionCount
  private var scannedLimit = blockStartOffset
  private var scannedElement: MdxJsxScanner.Element? = null
  private var currentElement: MdxJsxScanner.Element? = null

  init {
    scanElement(currentEndOffset)
    retainCurrentElement()
  }

  override fun allowsSubBlocks(): Boolean {
    return !closeScheduled && !suppressSubBlocks && hasCompleteOpeningTag()
  }

  override fun prepareForLine() {
    suppressSubBlocks = false
  }

  override fun appendLine(candidateEndOffset: Int) {
    val hadCompleteOpeningTag = hasCompleteOpeningTag()
    super.appendLine(candidateEndOffset)
    retainCurrentElement()
    if (!hadCompleteOpeningTag && hasCompleteOpeningTag() && !isCurrentBlockTerminated()) {
      suppressSubBlocks = true
    }
  }

  override fun activeProcessingResult(): MarkerBlock.ProcessingResult {
    return if (hasJsxParent && closeScheduled) {
      MarkerBlock.ProcessingResult.PASS
    }
    else {
      MarkerBlock.ProcessingResult.CANCEL
    }
  }

  override fun shouldAppendLine(pos: LookaheadText.Position, candidateEndOffset: Int): Boolean {
    val element = scanElement(candidateEndOffset)
    return element?.termination?.let { it != MdxJsxScanner.Termination.UNTERMINATED } == true ||
           constraints.applyToNextLine(pos).extendsPrev(constraints) ||
           element != null
  }

  override fun isTerminated(candidateEndOffset: Int): Boolean {
    val termination = scanElement(candidateEndOffset)?.termination ?: return false
    return termination != MdxJsxScanner.Termination.UNTERMINATED
  }

  override fun createNodes(): List<SequentialParser.Node> {
    val element = scanElement(currentEndOffset)
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
          SequentialParser.Node(openingPrefix.toMarkdownRange(), MdxMarkdownLibElementTypes.MDX_JSX_OPENING_ELEMENT),
          SequentialParser.Node(openingPrefix.toMarkdownRange(), MdxMarkdownLibElementTypes.MDX_JSX_FLOW_ELEMENT),
        )
      }
    }
    if (element.termination == MdxJsxScanner.Termination.UNTERMINATED && currentEndOffset < source.length) {
      return emptyList()
    }
    return MdxBlockNodeFactory.createFlowElementNodes(source, element)
  }

  private fun hasCompleteOpeningTag(): Boolean {
    return currentElement != null
  }

  private fun scanElement(limit: Int): MdxJsxScanner.Element? {
    if (limit < scannedLimit) {
      return currentElement
    }
    ingestProductionOpacity()
    if (limit > scannedLimit) {
      val codeSpanRanges = codeSpanSession.advanceTo(limit, jsxSession.opaqueRangeLookup())
      jsxSession.addOpaqueRanges(codeSpanRanges)
    }
    scannedElement = jsxSession.advanceTo(limit)
    scannedLimit = limit
    if (limit == currentEndOffset) {
      currentElement = scannedElement
    }
    return scannedElement
  }

  private fun ingestProductionOpacity() {
    val productions = productions()
    if (processedProductionCount >= productions.size) return
    val ranges = mutableListOf<TextRange>()
    for (index in processedProductionCount..<productions.size) {
      val production = productions[index]
      if ((production.type == MarkdownElementTypes.CODE_FENCE || production.type == MarkdownElementTypes.HTML_BLOCK) &&
          production.range.first >= blockStartOffset) {
        ranges.add(production.range.toTextRange())
      }
    }
    processedProductionCount = productions.size
    jsxSession.addOpaqueRanges(ranges)
  }

  private fun retainCurrentElement() {
    check(scannedLimit == currentEndOffset)
    currentElement = scannedElement
  }

}
