package org.intellij.plugin.mdx.lang.parse

import org.intellij.markdown.IElementType
import org.intellij.markdown.parser.LookaheadText
import org.intellij.markdown.parser.ProductionHolder
import org.intellij.markdown.parser.constraints.MarkdownConstraints
import org.intellij.markdown.parser.constraints.extendsPrev
import org.intellij.markdown.parser.markerblocks.MarkerBlock
import org.intellij.markdown.parser.markerblocks.MarkerBlockImpl
import org.intellij.markdown.parser.sequentialparsers.SequentialParser

internal class ImmediateJsxBlockMarkerBlock(myConstraints: MarkdownConstraints,
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

internal class JsxBlockMarkerBlock(myConstraints: MarkdownConstraints,
                                   private val productionHolder: ProductionHolder,
                                   private val kind: MdxBlockKind,
                                   private val blockStartOffset: Int,
                                   blockStartIndent: Int,
                                   private val source: CharSequence,
                                   initialText: String) : MarkerBlockImpl(
  if (kind == MdxBlockKind.JSX) MdxJsxMarkdownConstraints(myConstraints, blockStartIndent, blockStartOffset) else myConstraints,
  productionHolder.mark()
) {
  private var currentEndOffset = blockStartOffset + initialText.length
  private var finalized = false
  private var closeScheduled = false
  private var suppressSubBlocks = false

  override fun allowsSubBlocks(): Boolean {
    return kind == MdxBlockKind.JSX && !closeScheduled && !suppressSubBlocks && hasCompleteOpeningTag()
  }

  override fun isInterestingOffset(pos: LookaheadText.Position): Boolean = pos.offsetInCurrentLine == -1

  override fun getDefaultAction(): MarkerBlock.ClosingAction {
    return MarkerBlock.ClosingAction.DROP
  }

  override fun doProcessToken(pos: LookaheadText.Position, currentConstraints: MarkdownConstraints): MarkerBlock.ProcessingResult {
    if (finalized) {
      return MarkerBlock.ProcessingResult.DEFAULT
    }
    if (pos.offsetInCurrentLine != -1) {
      return MarkerBlock.ProcessingResult.CANCEL
    }
    suppressSubBlocks = false
    if (isTerminated()) {
      scheduleClose(pos.offset)
      return MarkerBlock.ProcessingResult.CANCEL
    }
    val candidateEndOffset = pos.nextLineOrEofOffset
    if (!shouldAppendLine(pos, candidateEndOffset)) {
      return MarkerBlock.ProcessingResult.DEFAULT
    }

    val hadCompleteOpeningTag = hasCompleteOpeningTag()
    currentEndOffset = candidateEndOffset
    if (kind == MdxBlockKind.JSX && !hadCompleteOpeningTag && hasCompleteOpeningTag() && !isTerminated()) {
      suppressSubBlocks = true
    }
    if (isTerminated()) {
      scheduleClose(pos.nextLineOrEofOffset)
    }
    return MarkerBlock.ProcessingResult.CANCEL
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

  private fun scheduleClose(offset: Int) {
    closeScheduled = true
    scheduleProcessingResult(offset, MarkerBlock.ProcessingResult.DEFAULT)
  }

  private fun shouldAppendLine(pos: LookaheadText.Position, candidateEndOffset: Int): Boolean {
    if (isTerminated(candidateEndOffset)) {
      return true
    }
    if (constraints.applyToNextLine(pos).extendsPrev(constraints)) {
      return true
    }
    return when (kind) {
      MdxBlockKind.JSX ->
        MdxJsxScanner.scanJsxElement(source, blockStartOffset, candidateEndOffset) != null
      MdxBlockKind.ESM ->
        MdxEsmScanner.scanBlock(source, blockStartOffset, candidateEndOffset) != null
      MdxBlockKind.EXPRESSION ->
        MdxJsxScanner.scanExpression(source, blockStartOffset, candidateEndOffset) != -1
    }
  }

  private fun isTerminated(): Boolean {
    return isTerminated(currentEndOffset)
  }

  private fun hasCompleteOpeningTag(): Boolean {
    return MdxJsxScanner.scanJsxElement(source, blockStartOffset, currentEndOffset) != null
  }

  private fun isTerminated(candidateEndOffset: Int): Boolean {
    return when (kind) {
      MdxBlockKind.JSX -> MdxJsxScanner.scanJsxElement(source, blockStartOffset, candidateEndOffset)?.terminated == true
      MdxBlockKind.ESM -> {
        val block = MdxEsmScanner.scanBlock(source, blockStartOffset, candidateEndOffset)
        block != null && (block.terminated || block.recoveryBoundary || candidateEndOffset == source.length)
      }
      MdxBlockKind.EXPRESSION -> MdxJsxScanner.scanExpression(source, blockStartOffset, candidateEndOffset) == candidateEndOffset
    }
  }

  private fun finalizeProductions() {
    if (finalized) return
    finalized = true
    val nodes = when (kind) {
      MdxBlockKind.JSX -> {
        val element = MdxJsxScanner.scanJsxElement(source, blockStartOffset, currentEndOffset)
        // A marker may be finalized merely because a nested Markdown block takes over. Its current
        // range is then only a scanner snapshot and can cross the nested block's eventual JSX root.
        // Publish recovered roots only at boundaries that cannot later move: a consumed closing tag
        // or the actual end of input.
        if (element == null) {
          val openingPrefix = MdxJsxScanner.incompleteOpeningTagRange(source, blockStartOffset, currentEndOffset)
          if (openingPrefix == null) {
            emptyList()
          }
          else {
            listOf(
              SequentialParser.Node(openingPrefix, MdxMarkdownLibElementTypes.MDX_JSX_OPENING_ELEMENT),
              SequentialParser.Node(openingPrefix, MdxMarkdownLibElementTypes.MDX_JSX_FLOW_ELEMENT),
            )
          }
        }
        else if (!element.terminated && currentEndOffset < source.length) {
          emptyList()
        }
        else {
          MdxJsxScanner.createFlowElementNodes(source, element)
        }
      }
      MdxBlockKind.ESM -> {
        val block = MdxEsmScanner.scanBlock(source, blockStartOffset, currentEndOffset)
        if (block == null || (!block.terminated && !block.recoveryBoundary && currentEndOffset < source.length)) {
          listOf(SequentialParser.Node(blockStartOffset..currentEndOffset, MdxMarkdownLibTokenTypes.EMBEDDED_JS_CONTENT))
        }
        else {
          MdxJsxScanner.createEsmNodes(block)
        }
      }
      MdxBlockKind.EXPRESSION -> {
        listOf(
          SequentialParser.Node(blockStartOffset..currentEndOffset, MdxMarkdownLibTokenTypes.EMBEDDED_JS_CONTENT),
          SequentialParser.Node(blockStartOffset..currentEndOffset, MdxMarkdownLibElementTypes.MDX_EXPRESSION),
        )
      }
    }
    if (nodes.isNotEmpty()) {
      productionHolder.addProduction(nodes)
    }
  }
}
