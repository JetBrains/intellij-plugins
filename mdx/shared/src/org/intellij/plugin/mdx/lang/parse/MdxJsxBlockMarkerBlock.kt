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
                                      ownership: MdxMarkdownOwnership,
                                      productionHolder: ProductionHolder,
                                      blockStartOffset: Int,
                                      blockStartIndent: Int,
                                      elementIdentity: MdxJsxScanner.ElementIdentity?,
                                      source: CharSequence,
                                      initialText: String,
                                      private val jsxSession: MdxJsxScanner.Session = MdxJsxScanner.Session(source, blockStartOffset),
) : MdxBlockMarkerBlock(
  MdxJsxMarkdownConstraints(myConstraints, blockStartIndent, elementIdentity, { jsxSession.endOffset }),
  productionHolder,
  blockStartOffset,
  source,
  initialText,
) {
  private val hasJsxParent = myConstraints is MdxJsxMarkdownConstraints
  private val markdownConstraints = (myConstraints as? MdxJsxMarkdownConstraints)?.markdownConstraints() ?: myConstraints
  private val codeSpanSession = MdxMarkdownCodeSpanScanner.Session(source, blockStartOffset, paragraphEnd = ownership::paragraphEnd)
  private var suppressSubBlocks = false
  private var processedProductionCount = initialProductionCount
  private var scannedLimit = blockStartOffset
  private var scannedElement: MdxJsxScanner.Element? = null
  private var currentElement: MdxJsxScanner.Element? = null
  private val paragraphs = mutableListOf<MdxFlowParagraph>()
  private var openingParagraphStart: Int? = null

  val bodyEnd: Int?
    get() = currentElement?.elements?.lastOrNull()?.takeIf { it.index == 0 }?.bodyRange?.endOffset

  init {
    scanElement(currentEndOffset)
    retainCurrentElement()
    openingParagraphStart = findOpeningParagraphStart()
  }

  override fun allowsSubBlocks(): Boolean {
    if (openingParagraphStart != null) return true
    return (!closeScheduled || hasClosingLineContent()) && !suppressSubBlocks && hasCompleteOpeningTag()
  }

  fun takeOpeningParagraphStart(): Int? {
    val start = openingParagraphStart ?: return null
    openingParagraphStart = null
    return start
  }

  fun createParagraph(
    start: Int,
    end: Int,
    paragraphConstraints: MarkdownConstraints,
    interruptsParagraph: (LookaheadText.Position, MarkdownConstraints) -> Boolean,
    onContinuation: (LookaheadText.Position) -> Unit,
  ): MarkerBlock {
    val paragraph = MdxFlowParagraph(start, end, paragraphConstraints, this, source, interruptsParagraph, onContinuation)
    paragraphs.add(paragraph)
    return paragraph.marker
  }

  fun observeParagraphLine(pos: LookaheadText.Position) {
    if (pos.nextLineOrEofOffset <= currentEndOffset || closeScheduled) return
    val lineStart = pos.nextPosition(-pos.offsetInCurrentLine - 1) ?: return
    processToken(lineStart, constraints)
  }

  private fun findOpeningParagraphStart(): Int? {
    val opening = currentElement?.tags?.firstOrNull() ?: return null
    if (opening.kind != MdxJsxScanner.TagKind.OPENING) return null
    val start = opening.range.endOffset
    val end = bodyEnd?.coerceAtMost(currentEndOffset) ?: currentEndOffset
    if (start >= end || source.subSequence(start, end).isBlank()) return null
    if (source.subSequence(start, end).contains('\n')) return null
    return start
  }

  private fun hasClosingLineContent(): Boolean {
    val end = bodyEnd ?: return false
    val start = source.lastIndexOf('\n', currentEndOffset - 1) + 1
    return start < end && source.subSequence(start, end).isNotBlank()
  }

  fun addMarkdownOpacity(range: TextRange) {
    jsxSession.addOpaqueRanges(listOf(range))
  }

  override fun prepareForLine() {
    suppressSubBlocks = jsxSession.isInJavaScript
  }

  override fun appendLine(candidateEndOffset: Int) {
    val hadCompleteOpeningTag = hasCompleteOpeningTag()
    super.appendLine(candidateEndOffset)
    retainCurrentElement()
    if (!hadCompleteOpeningTag && hasCompleteOpeningTag()) {
      openingParagraphStart = findOpeningParagraphStart()
      if (!isCurrentBlockTerminated()) suppressSubBlocks = true
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
    val nextConstraints = constraints.applyToNextLine(pos)
    // A JSX block cannot continue after its Markdown container ends.
    if (!nextConstraints.extendsPrev(markdownConstraints)) return false
    val element = scanElement(candidateEndOffset)
    return element?.termination?.let { it != MdxJsxScanner.Termination.UNTERMINATED } == true ||
           nextConstraints.extendsPrev(constraints) ||
           element != null
  }

  override fun isTerminated(candidateEndOffset: Int): Boolean {
    val termination = scanElement(candidateEndOffset)?.termination ?: return false
    return termination != MdxJsxScanner.Termination.UNTERMINATED
  }

  override fun createNodes(): List<SequentialParser.Node> {
    val paragraphNodes = paragraphs.flatMap { it.production }
    val element = scanElement(currentEndOffset)
    // A marker may be finalized merely because a nested Markdown block takes over. Its current
    // range is then only a scanner snapshot and can cross the nested block's eventual JSX root.
    // Publish recovered roots only at boundaries that cannot later move: a consumed closing tag
    // or the actual end of input.
    if (element == null) {
      val openingPrefix = MdxJsxScanner.incompleteOpeningTagRange(source, blockStartOffset, currentEndOffset)
      return if (openingPrefix == null) {
        paragraphNodes
      }
      else {
        listOf(
          SequentialParser.Node(openingPrefix.toMarkdownRange(), MdxMarkdownLibElementTypes.MDX_JSX_OPENING_ELEMENT),
          SequentialParser.Node(openingPrefix.toMarkdownRange(), MdxMarkdownLibElementTypes.MDX_JSX_FLOW_ELEMENT),
        ) + paragraphNodes
      }
    }
    if (element.termination == MdxJsxScanner.Termination.UNTERMINATED && currentEndOffset < source.length) {
      return paragraphNodes
    }
    return MdxBlockNodeFactory.createFlowElementNodes(element) + paragraphNodes
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
