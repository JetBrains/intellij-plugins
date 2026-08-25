package org.intellij.plugin.mdx.lang.parse

import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.parser.LookaheadText
import org.intellij.markdown.parser.ProductionHolder
import org.intellij.markdown.parser.constraints.MarkdownConstraints
import org.intellij.markdown.parser.markerblocks.MarkerBlock
import org.intellij.markdown.parser.markerblocks.MarkerBlockImpl
import org.intellij.markdown.parser.sequentialparsers.SequentialParser

internal class ImmediateMdxBlockMarkerBlock(myConstraints: MarkdownConstraints,
                                            productionHolder: ProductionHolder,
                                            private val nodeType: IElementType,
                                            nodes: List<SequentialParser.Node>) : MarkerBlockImpl(myConstraints, productionHolder.mark()) {
  init {
    productionHolder.addProduction(nodes)
  }

  override fun allowsSubBlocks(): Boolean = false

  override fun isInterestingOffset(pos: LookaheadText.Position): Boolean = pos.offsetInCurrentLine == -1

  override fun getDefaultAction(): MarkerBlock.ClosingAction {
    // DONE, not DROP: a single-line block closed via flushMarkers at EOF must still emit its wrapping
    // node, or its children leak as detached siblings.
    return MarkerBlock.ClosingAction.DONE
  }

  override fun doProcessToken(pos: LookaheadText.Position, currentConstraints: MarkdownConstraints): MarkerBlock.ProcessingResult {
    return MarkerBlock.ProcessingResult.DEFAULT
  }

  override fun calcNextInterestingOffset(pos: LookaheadText.Position): Int {
    return pos.nextLineOrEofOffset
  }

  override fun getDefaultNodeType(): IElementType {
    return nodeType
  }
}

internal abstract class MdxBlockMarkerBlock(myConstraints: MarkdownConstraints,
                                            private val productionHolder: ProductionHolder,
                                            protected val blockStartOffset: Int,
                                            protected val source: CharSequence,
                                            initialText: String) : MarkerBlockImpl(myConstraints, productionHolder.mark()) {
  protected var currentEndOffset = blockStartOffset + initialText.length
    private set
  protected var closeScheduled = false
    private set
  private var finalized = false
  private var cachedOpacityLimit = -1
  private var cachedOpacityProductionCount = -1
  private var cachedOpacity = MdxOpaqueRanges.EMPTY

  override fun allowsSubBlocks(): Boolean = false

  override fun isInterestingOffset(pos: LookaheadText.Position): Boolean = pos.offsetInCurrentLine == -1

  override fun getDefaultAction(): MarkerBlock.ClosingAction {
    return MarkerBlock.ClosingAction.DROP
  }

  final override fun doProcessToken(pos: LookaheadText.Position,
                                    currentConstraints: MarkdownConstraints): MarkerBlock.ProcessingResult {
    if (finalized) {
      return MarkerBlock.ProcessingResult.DEFAULT
    }
    if (pos.offsetInCurrentLine != -1) {
      return activeProcessingResult()
    }
    prepareForLine()
    if (isCurrentBlockTerminated()) {
      scheduleClose(pos.offset)
      return activeProcessingResult()
    }
    val candidateEndOffset = pos.nextLineOrEofOffset
    if (!shouldAppendLine(pos, candidateEndOffset)) {
      return MarkerBlock.ProcessingResult.DEFAULT
    }

    appendLine(candidateEndOffset)
    if (isCurrentBlockTerminated()) {
      scheduleClose(pos.nextLineOrEofOffset)
    }
    return activeProcessingResult()
  }

  override fun acceptAction(action: MarkerBlock.ClosingAction): Boolean {
    if (action == MarkerBlock.ClosingAction.DONE || action == MarkerBlock.ClosingAction.DEFAULT) {
      finalizeProductions()
      return true
    }
    return super.acceptAction(action)
  }

  override fun calcNextInterestingOffset(pos: LookaheadText.Position): Int {
    return pos.nextLineOrEofOffset
  }

  override fun getDefaultNodeType(): IElementType {
    return MdxMarkdownLibElementTypes.MDX_BLOCK
  }

  protected open fun prepareForLine() = Unit

  protected open fun appendLine(candidateEndOffset: Int) {
    currentEndOffset = candidateEndOffset
  }

  protected fun isCurrentBlockTerminated(): Boolean {
    return isTerminated(currentEndOffset)
  }

  protected abstract fun activeProcessingResult(): MarkerBlock.ProcessingResult

  protected abstract fun shouldAppendLine(pos: LookaheadText.Position, candidateEndOffset: Int): Boolean

  protected abstract fun isTerminated(candidateEndOffset: Int): Boolean

  protected abstract fun createNodes(): List<SequentialParser.Node>

  protected fun opaqueMarkdownRanges(limit: Int): MdxOpaqueRanges {
    val productions = productionHolder.production
    val productionCount = productions.size
    if (limit == cachedOpacityLimit && productionCount == cachedOpacityProductionCount) {
      return cachedOpacity
    }
    val blockRanges = productions.asSequence()
      .filter { it.type == MarkdownElementTypes.CODE_FENCE || it.type == MarkdownElementTypes.HTML_BLOCK }
      .map { it.range }
      .filter { it.first >= blockStartOffset && it.last <= limit }
      .toList()
    val blockOpacity = MdxOpaqueRanges.of(blockRanges)
    val codeSpanRanges = MdxMarkdownCodeSpanScanner.findRanges(source, blockStartOffset, source.length, blockOpacity)
    cachedOpacity = MdxOpaqueRanges.of(blockRanges + codeSpanRanges)
    cachedOpacityLimit = limit
    cachedOpacityProductionCount = productionCount
    return cachedOpacity
  }

  protected fun productionCount(): Int = productionHolder.production.size

  private fun scheduleClose(offset: Int) {
    closeScheduled = true
    scheduleProcessingResult(offset, MarkerBlock.ProcessingResult.DEFAULT)
  }

  private fun finalizeProductions() {
    if (finalized) return
    finalized = true
    val nodes = createNodes()
    if (nodes.isNotEmpty()) {
      productionHolder.addProduction(nodes)
    }
  }
}
