package org.intellij.plugin.mdx.lang.parse

import org.intellij.markdown.parser.LookaheadText
import org.intellij.markdown.parser.ProductionHolder
import org.intellij.markdown.parser.constraints.MarkdownConstraints
import org.intellij.markdown.parser.constraints.extendsPrev
import org.intellij.markdown.parser.markerblocks.MarkerBlock
import org.intellij.markdown.parser.sequentialparsers.SequentialParser

internal class MdxOpaqueBlockMarkerBlock private constructor(
  myConstraints: MarkdownConstraints,
  productionHolder: ProductionHolder,
  blockStartOffset: Int,
  source: CharSequence,
  initialText: String,
  private val scanner: Scanner,
) : MdxBlockMarkerBlock(
  myConstraints,
  productionHolder,
  blockStartOffset,
  source,
  initialText,
) {
  private var scannedLimit = blockStartOffset
  private lateinit var scannedResult: ScanResult
  private lateinit var retainedResult: ScanResult

  init {
    scanTo(currentEndOffset)
    retainCurrentResult()
  }

  override fun activeProcessingResult(): MarkerBlock.ProcessingResult {
    return MarkerBlock.ProcessingResult.CANCEL
  }

  override fun shouldAppendLine(pos: LookaheadText.Position, candidateEndOffset: Int): Boolean {
    val terminated = isTerminated(candidateEndOffset)
    val boundaryEndOffset = scannedResult.boundaryEndOffset
    if (boundaryEndOffset != null && boundaryEndOffset <= pos.offset) {
      // Recovery boundaries may need the candidate as lookahead. Retain the recovered block without consuming that line.
      retainedResult = scannedResult
      return false
    }
    return terminated ||
           constraints.applyToNextLine(pos).extendsPrev(constraints) ||
           boundaryEndOffset != null
  }

  override fun appendLine(candidateEndOffset: Int) {
    super.appendLine(candidateEndOffset)
    retainCurrentResult()
  }

  override fun isTerminated(candidateEndOffset: Int): Boolean {
    scanTo(candidateEndOffset)
    return scannedResult.terminated
  }

  override fun createNodes(): List<SequentialParser.Node> {
    return retainedResult.createNodes()
  }

  private fun scanTo(limit: Int) {
    if (limit == scannedLimit && ::scannedResult.isInitialized) return
    check(limit > scannedLimit) { "MDX block scan cannot move from $scannedLimit back to $limit" }
    scannedResult = scanner.advanceTo(limit)
    scannedLimit = limit
  }

  private fun retainCurrentResult() {
    check(scannedLimit == currentEndOffset)
    retainedResult = scannedResult
  }

  private fun interface Scanner {
    fun advanceTo(limit: Int): ScanResult
  }

  private sealed interface ScanResult {
    val terminated: Boolean
    val boundaryEndOffset: Int?

    fun createNodes(): List<SequentialParser.Node>
  }

  private class EsmScanner(
    private val source: CharSequence,
    private val start: Int,
  ) : Scanner {
    private val session = MdxEsmScanner.Session(source, start)

    override fun advanceTo(limit: Int): ScanResult {
      val block = session.advanceTo(limit)
      val terminated = block != null && (block.terminated || block.recoveryBoundary || limit == source.length)
      return EsmScanResult(start..limit, block, terminated)
    }
  }

  private data class EsmScanResult(
    val range: IntRange,
    val block: MdxEsmScanner.Block?,
    override val terminated: Boolean,
  ) : ScanResult {
    override val boundaryEndOffset: Int?
      get() = block?.range?.last

    override fun createNodes(): List<SequentialParser.Node> {
      return if (block == null || !terminated) {
        listOf(SequentialParser.Node(range, MdxMarkdownLibTokenTypes.EMBEDDED_JS_CONTENT))
      }
      else {
        MdxBlockNodeFactory.createEsmNodes(block)
      }
    }
  }

  private class ExpressionScanner(
    private val start: Int,
    source: CharSequence,
  ) : Scanner {
    private val session = MdxExpressionBoundaryScanner.Session(source, start)

    override fun advanceTo(limit: Int): ScanResult {
      val expressionEnd = session.advanceTo(limit)
      return ExpressionScanResult(start..limit, expressionEnd)
    }
  }

  private data class ExpressionScanResult(
    val range: IntRange,
    private val expressionEnd: Int,
  ) : ScanResult {
    override val terminated: Boolean
      get() = boundaryEndOffset == range.last
    override val boundaryEndOffset: Int?
      get() = expressionEnd.takeIf { it != -1 }

    override fun createNodes(): List<SequentialParser.Node> {
      val nodeRange = boundaryEndOffset?.let { range.first..it } ?: range
      return listOf(
        SequentialParser.Node(nodeRange, MdxMarkdownLibTokenTypes.EMBEDDED_JS_CONTENT),
        SequentialParser.Node(nodeRange, MdxMarkdownLibElementTypes.MDX_EXPRESSION),
      )
    }
  }

  companion object {
    fun esm(
      myConstraints: MarkdownConstraints,
      productionHolder: ProductionHolder,
      blockStartOffset: Int,
      source: CharSequence,
      initialText: String,
    ): MdxOpaqueBlockMarkerBlock {
      return MdxOpaqueBlockMarkerBlock(
        myConstraints,
        productionHolder,
        blockStartOffset,
        source,
        initialText,
        EsmScanner(source, blockStartOffset),
      )
    }

    fun expression(
      myConstraints: MarkdownConstraints,
      productionHolder: ProductionHolder,
      blockStartOffset: Int,
      source: CharSequence,
      initialText: String,
    ): MdxOpaqueBlockMarkerBlock {
      return MdxOpaqueBlockMarkerBlock(
        myConstraints,
        productionHolder,
        blockStartOffset,
        source,
        initialText,
        ExpressionScanner(blockStartOffset, source),
      )
    }
  }
}
