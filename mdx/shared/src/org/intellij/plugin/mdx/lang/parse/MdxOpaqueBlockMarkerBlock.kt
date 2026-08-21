package org.intellij.plugin.mdx.lang.parse

import org.intellij.markdown.parser.LookaheadText
import org.intellij.markdown.parser.ProductionHolder
import org.intellij.markdown.parser.constraints.MarkdownConstraints
import org.intellij.markdown.parser.constraints.extendsPrev
import org.intellij.markdown.parser.markerblocks.MarkerBlock
import org.intellij.markdown.parser.sequentialparsers.SequentialParser

internal class MdxOpaqueBlockMarkerBlock(myConstraints: MarkdownConstraints,
                                         productionHolder: ProductionHolder,
                                         private val kind: MdxOpaqueBlockKind,
                                         blockStartOffset: Int,
                                         source: CharSequence,
                                         initialText: String) : MdxBlockMarkerBlock(
  myConstraints,
  productionHolder,
  blockStartOffset,
  source,
  initialText,
) {
  override fun activeProcessingResult(): MarkerBlock.ProcessingResult {
    return MarkerBlock.ProcessingResult.CANCEL
  }

  override fun shouldAppendLine(pos: LookaheadText.Position, candidateEndOffset: Int): Boolean {
    return isTerminated(candidateEndOffset) ||
           constraints.applyToNextLine(pos).extendsPrev(constraints) ||
           when (kind) {
             MdxOpaqueBlockKind.ESM -> MdxEsmScanner.scanBlock(source, blockStartOffset, candidateEndOffset) != null
             MdxOpaqueBlockKind.EXPRESSION ->
               MdxExpressionBoundaryScanner.findExpressionEnd(source, blockStartOffset, candidateEndOffset) != -1
           }
  }

  override fun isTerminated(candidateEndOffset: Int): Boolean {
    return when (kind) {
      MdxOpaqueBlockKind.ESM -> {
        val block = MdxEsmScanner.scanBlock(source, blockStartOffset, candidateEndOffset)
        block != null && (block.terminated || block.recoveryBoundary || candidateEndOffset == source.length)
      }
      MdxOpaqueBlockKind.EXPRESSION ->
        MdxExpressionBoundaryScanner.findExpressionEnd(source, blockStartOffset, candidateEndOffset) == candidateEndOffset
    }
  }

  override fun createNodes(): List<SequentialParser.Node> {
    return when (kind) {
      MdxOpaqueBlockKind.ESM -> {
        val block = MdxEsmScanner.scanBlock(source, blockStartOffset, currentEndOffset)
        if (block == null || (!block.terminated && !block.recoveryBoundary && currentEndOffset < source.length)) {
          listOf(SequentialParser.Node(blockStartOffset..currentEndOffset, MdxMarkdownLibTokenTypes.EMBEDDED_JS_CONTENT))
        }
        else {
          MdxBlockNodeFactory.createEsmNodes(block)
        }
      }
      MdxOpaqueBlockKind.EXPRESSION -> {
        listOf(
          SequentialParser.Node(blockStartOffset..currentEndOffset, MdxMarkdownLibTokenTypes.EMBEDDED_JS_CONTENT),
          SequentialParser.Node(blockStartOffset..currentEndOffset, MdxMarkdownLibElementTypes.MDX_EXPRESSION),
        )
      }
    }
  }
}

internal enum class MdxOpaqueBlockKind {
  ESM,
  EXPRESSION
}
