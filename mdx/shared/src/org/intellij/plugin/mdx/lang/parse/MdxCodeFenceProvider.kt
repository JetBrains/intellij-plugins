package org.intellij.plugin.mdx.lang.parse

import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.parser.LookaheadText
import org.intellij.markdown.parser.MarkerProcessor
import org.intellij.markdown.parser.ProductionHolder
import org.intellij.markdown.parser.constraints.MarkdownConstraints
import org.intellij.markdown.parser.constraints.eatItselfFromString
import org.intellij.markdown.parser.constraints.extendsPrev
import org.intellij.markdown.parser.markerblocks.MarkerBlock
import org.intellij.markdown.parser.markerblocks.impl.CodeFenceMarkerBlock
import org.intellij.markdown.parser.sequentialparsers.SequentialParser
import org.intellij.plugins.markdown.lang.parser.blocks.CodeFenceMarkerProvider

internal class MdxCodeFenceProvider : CodeFenceMarkerProvider() {
  override fun createMarkerBlocks(
    pos: LookaheadText.Position,
    productionHolder: ProductionHolder,
    stateInfo: MarkerProcessor.StateInfo,
  ): List<MarkerBlock> {
    val openingInfo = obtainFenceOpeningInfo(pos, stateInfo.currentConstraints) ?: return emptyList()
    createOpeningNodes(pos, openingInfo.info, productionHolder)

    val jsxConstraints = stateInfo.currentConstraints as? MdxJsxMarkdownConstraints
    if (jsxConstraints == null) {
      return listOf(CodeFenceMarkerBlock(stateInfo.currentConstraints, productionHolder, openingInfo.delimiter))
    }

    val fenceConstraints = jsxConstraints.asOpaqueBlockConstraints()
    val delegate = CodeFenceMarkerBlock(fenceConstraints, productionHolder, openingInfo.delimiter)
    val hasClosingFence = hasClosingFence(pos, fenceConstraints, openingInfo.delimiter)
    return listOf(MdxRecoveringCodeFenceMarkerBlock(delegate, jsxConstraints, hasClosingFence))
  }

  private fun createOpeningNodes(pos: LookaheadText.Position, info: String, productionHolder: ProductionHolder) {
    val infoStart = pos.nextLineOrEofOffset - info.length
    productionHolder.addProduction(listOf(SequentialParser.Node(pos.offset..infoStart, MarkdownTokenTypes.CODE_FENCE_START)))
    if (info.isNotEmpty()) {
      productionHolder.addProduction(listOf(SequentialParser.Node(infoStart..pos.nextLineOrEofOffset, MarkdownTokenTypes.FENCE_LANG)))
    }
  }

  private fun hasClosingFence(
    openingPosition: LookaheadText.Position,
    constraints: MarkdownConstraints,
    delimiter: String,
  ): Boolean {
    var position = openingPosition.nextLinePosition()
    while (position != null) {
      val nextLineConstraints = constraints.applyToNextLine(position)
      if (!nextLineConstraints.extendsPrev(constraints)) return false
      if (isClosingFence(nextLineConstraints.eatItselfFromString(position.currentLine), delimiter)) return true
      position = position.nextLinePosition()
    }
    return false
  }

  private fun isClosingFence(line: CharSequence, delimiter: String): Boolean {
    var offset = 0
    while (offset < line.length && offset < 3 && line[offset] == ' ') {
      offset++
    }
    var markerLength = 0
    while (offset + markerLength < line.length && line[offset + markerLength] == delimiter[0]) {
      markerLength++
    }
    return markerLength >= delimiter.length &&
           line.subSequence(offset + markerLength, line.length).all { it == ' ' }
  }
}

private class MdxRecoveringCodeFenceMarkerBlock(
  private val delegate: MarkerBlock,
  private val jsxConstraints: MdxJsxMarkdownConstraints,
  private val hasClosingFence: Boolean,
) : MarkerBlock by delegate {
  override fun processToken(
    pos: LookaheadText.Position,
    currentConstraints: MarkdownConstraints,
  ): MarkerBlock.ProcessingResult {
    if (!hasClosingFence && pos.offsetInCurrentLine == -1) {
      when (jsxConstraints.closingBoundary(pos)) {
        MdxJsxClosingBoundary.CURRENT,
        MdxJsxClosingBoundary.ANCESTOR -> return MarkerBlock.ProcessingResult.DEFAULT
        MdxJsxClosingBoundary.NONE,
        MdxJsxClosingBoundary.MISMATCHED -> Unit
      }
    }
    return delegate.processToken(pos, currentConstraints)
  }
}
