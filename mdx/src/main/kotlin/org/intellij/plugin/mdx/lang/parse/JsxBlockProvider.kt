package org.intellij.plugin.mdx.lang.parse

import org.intellij.markdown.parser.LookaheadText
import org.intellij.markdown.parser.MarkerProcessor
import org.intellij.markdown.parser.ProductionHolder
import org.intellij.markdown.parser.constraints.MarkdownConstraints
import org.intellij.markdown.parser.markerblocks.MarkerBlock
import org.intellij.markdown.parser.markerblocks.MarkerBlockProvider
import org.intellij.markdown.parser.sequentialparsers.SequentialParser

class JsxBlockProvider : MarkerBlockProvider<MarkerProcessor.StateInfo> {
  override fun createMarkerBlocks(pos: LookaheadText.Position,
                                  productionHolder: ProductionHolder,
                                  stateInfo: MarkerProcessor.StateInfo): List<MarkerBlock> {
    val start = findStart(pos, stateInfo.currentConstraints) ?: return emptyList()
    val lineText = pos.currentLineFromPosition
    val localText = lineText.subSequence(start.offsetInLine, lineText.length)
    val absoluteStart = pos.offset + start.offsetInLine
    val immediateBlock = when (start.kind) {
      MdxBlockKind.ESM -> {
        val block = MdxJsxScanner.scanEsmBlock(localText, 0)
        if (block != null && block.balanced && localText.subSequence(block.range.last, localText.length).isBlank()) {
          ImmediateBlock(MdxElementTypes.MDX_ESM_BLOCK, MdxJsxScanner.createEsmNodes(block, absoluteStart, includeRoot = false))
        }
        else null
      }
      MdxBlockKind.JSX -> {
        val element = MdxJsxScanner.scanJsxElement(localText, 0)
        if (element != null && element.balanced && localText.subSequence(element.range.last, localText.length).isBlank()) {
          ImmediateBlock(
            MdxElementTypes.MDX_JSX_FLOW_ELEMENT,
            MdxJsxScanner.createElementNodes(element, MdxElementTypes.MDX_JSX_FLOW_ELEMENT, absoluteStart, includeRoot = false),
          )
        }
        else null
      }
      MdxBlockKind.EXPRESSION -> {
        val expressionEnd = MdxJsxScanner.scanExpression(localText, 0)
        if (expressionEnd != -1 && localText.subSequence(expressionEnd, localText.length).isBlank()) {
          ImmediateBlock(
            MdxElementTypes.MDX_JSX_EXPRESSION,
            listOf(SequentialParser.Node(absoluteStart..absoluteStart + expressionEnd, MdxTokenTypes.JSX_BLOCK_CONTENT)),
          )
        }
        else null
      }
    }
    if (immediateBlock != null) {
      return listOf(
        ImmediateJsxBlockMarkerBlock(
          stateInfo.currentConstraints,
          productionHolder,
          immediateBlock.type,
          immediateBlock.children,
        ),
      )
    }
    return listOf(
      JsxBlockMarkerBlock(
        stateInfo.currentConstraints,
        productionHolder,
        start.kind,
        absoluteStart,
        start.offsetInLine,
        pos.originalText,
        localText.toString(),
      ),
    )
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
      MdxJsxScanner.isLineStartEsm(text, offset) ->
        StartInfo(offset, MdxBlockKind.ESM)
      text[offset] == '<' && isLineStartJsxBlock(text, offset) ->
        StartInfo(offset, MdxBlockKind.JSX)
      text[offset] == '{' && MdxJsxScanner.isLineStartExpression(text, offset) ->
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
    return element == null || !element.balanced || localText.subSequence(element.range.last, localText.length).isBlank()
  }

  private data class StartInfo(val offsetInLine: Int, val kind: MdxBlockKind)

  private data class ImmediateBlock(val type: org.intellij.markdown.IElementType, val children: List<SequentialParser.Node>)
}

internal enum class MdxBlockKind {
  JSX,
  ESM,
  EXPRESSION
}
