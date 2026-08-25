package org.intellij.plugin.mdx.lang.parse

import org.intellij.markdown.parser.LookaheadText
import org.intellij.markdown.parser.MarkerProcessor
import org.intellij.markdown.parser.ProductionHolder
import org.intellij.markdown.parser.constraints.MarkdownConstraints
import org.intellij.markdown.parser.markerblocks.MarkerBlock
import org.intellij.markdown.parser.markerblocks.MarkerBlockProvider
import org.intellij.markdown.parser.sequentialparsers.SequentialParser

class MdxBlockProvider : MarkerBlockProvider<MarkerProcessor.StateInfo> {
  override fun createMarkerBlocks(pos: LookaheadText.Position,
                                  productionHolder: ProductionHolder,
                                  stateInfo: MarkerProcessor.StateInfo): List<MarkerBlock> {
    val start = findStart(pos, stateInfo.currentConstraints) ?: return emptyList()
    val lineText = pos.currentLineFromPosition
    val localText = lineText.subSequence(start.offsetInLine, lineText.length)
    val absoluteStart = pos.offset + start.offsetInLine
    val immediateBlock = when (start.kind) {
      MdxBlockKind.ESM -> {
        val block = MdxEsmScanner.scanBlock(localText, 0)
        if (block != null && block.terminated && localText.subSequence(block.range.last, localText.length).isBlank()) {
          ImmediateBlock(
            MdxMarkdownLibElementTypes.MDX_ESM_BLOCK,
            MdxBlockNodeFactory.createEsmNodes(block, absoluteStart, includeRoot = false),
          )
        }
        else null
      }
      MdxBlockKind.JSX -> {
        val element = MdxJsxScanner.scanJsxElement(localText, 0)
        if (element != null &&
            element.termination != MdxJsxScanner.Termination.UNTERMINATED &&
            localText.subSequence(element.range.last, localText.length).isBlank()) {
          ImmediateBlock(
            MdxMarkdownLibElementTypes.MDX_JSX_FLOW_ELEMENT,
            MdxBlockNodeFactory.createFlowElementNodes(localText, element, absoluteStart, includeRoot = false),
          )
        }
        else null
      }
      MdxBlockKind.EXPRESSION -> {
        val expressionEnd = MdxExpressionBoundaryScanner.findExpressionEnd(localText, 0, localText.length)
        if (expressionEnd != -1 && localText.subSequence(expressionEnd, localText.length).isBlank()) {
          ImmediateBlock(
            MdxMarkdownLibElementTypes.MDX_EXPRESSION,
            listOf(SequentialParser.Node(absoluteStart..absoluteStart + expressionEnd, MdxMarkdownLibTokenTypes.EMBEDDED_JS_CONTENT)),
          )
        }
        else null
      }
    }
    if (immediateBlock != null) {
      return listOf(
        ImmediateMdxBlockMarkerBlock(
          stateInfo.currentConstraints,
          productionHolder,
          immediateBlock.type,
          immediateBlock.children,
        ),
      )
    }
    val marker = when (start.kind) {
      MdxBlockKind.JSX -> MdxJsxBlockMarkerBlock(
        stateInfo.currentConstraints,
        productionHolder,
        absoluteStart,
        pos.offsetInCurrentLine + start.offsetInLine,
        MdxJsxScanner.openingElementIdentity(pos.originalText, absoluteStart),
        pos.originalText,
        localText.toString(),
      )
      MdxBlockKind.ESM -> MdxOpaqueBlockMarkerBlock(
        stateInfo.currentConstraints,
        productionHolder,
        MdxOpaqueBlockKind.ESM,
        absoluteStart,
        pos.originalText,
        localText.toString(),
      )
      MdxBlockKind.EXPRESSION -> MdxOpaqueBlockMarkerBlock(
        stateInfo.currentConstraints,
        productionHolder,
        MdxOpaqueBlockKind.EXPRESSION,
        absoluteStart,
        pos.originalText,
        localText.toString(),
      )
    }
    return listOf(marker)
  }

  override fun interruptsParagraph(pos: LookaheadText.Position, constraints: MarkdownConstraints): Boolean {
    return findStart(pos, constraints) != null
  }

  private fun findStart(pos: LookaheadText.Position, constraints: MarkdownConstraints): StartInfo? {
    if (!MarkerBlockProvider.isStartOfLineWithConstraints(pos, constraints)) {
      return null
    }
    val text = pos.currentLineFromPosition
    val offset = MarkerBlockProvider.passSmallIndent(text)
    if (offset >= text.length) {
      return null
    }
    return when {
      constraints.types.isEmpty() && MdxEsmScanner.isLineStart(text, offset) ->
        StartInfo(offset, MdxBlockKind.ESM)
      text[offset] == '<' && isLineStartJsxBlock(text, offset) ->
        StartInfo(offset, MdxBlockKind.JSX)
      text[offset] == '{' ->
        StartInfo(offset, MdxBlockKind.EXPRESSION)
      else -> null
    }
  }

  private fun isLineStartJsxBlock(text: CharSequence, offset: Int): Boolean {
    if (!MdxJsxScanner.isLineStartJsx(text, offset)) {
      return false
    }

    val localText = text.subSequence(offset, text.length)
    val element = MdxJsxScanner.scanJsxElement(localText, 0)
    return element == null ||
           element.termination == MdxJsxScanner.Termination.UNTERMINATED ||
           localText.subSequence(element.range.last, localText.length).isBlank()
  }

  private data class StartInfo(val offsetInLine: Int, val kind: MdxBlockKind)

  private data class ImmediateBlock(val type: org.intellij.markdown.IElementType, val children: List<SequentialParser.Node>)
}

private enum class MdxBlockKind {
  JSX,
  ESM,
  EXPRESSION
}
