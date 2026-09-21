package org.intellij.plugin.mdx.lang.parse

import com.intellij.openapi.util.TextRange
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.parser.sequentialparsers.SequentialParser

internal object MdxBlockNodeFactory {
  fun createFlowElementNodes(
    element: MdxJsxScanner.Element,
    shift: Int = 0,
    includeRoot: Boolean = true,
    paragraphRange: TextRange? = null,
  ): List<SequentialParser.Node> {
    val nodes = createElementNodes(element, shift).toMutableList()
    paragraphRange?.let {
      nodes.add(SequentialParser.Node(it.toMarkdownRange(), MarkdownElementTypes.PARAGRAPH))
    }
    if (includeRoot) {
      nodes.add(SequentialParser.Node(element.range.shiftRight(shift).toMarkdownRange(), MdxMarkdownLibElementTypes.MDX_JSX_FLOW_ELEMENT))
    }
    return nodes
  }

  fun createEsmNodes(
    block: MdxEsmScanner.Block,
    shift: Int = 0,
    includeRoot: Boolean = true,
  ): List<SequentialParser.Node> {
    return buildList {
      add(SequentialParser.Node(block.range.shiftRight(shift).toMarkdownRange(), MdxMarkdownLibTokenTypes.EMBEDDED_JS_CONTENT))
      if (includeRoot) {
        add(SequentialParser.Node(block.range.shiftRight(shift).toMarkdownRange(), MdxMarkdownLibElementTypes.MDX_ESM_BLOCK))
      }
    }
  }

  private fun createElementNodes(
    element: MdxJsxScanner.Element,
    shift: Int,
  ): List<SequentialParser.Node> {
    val nodes = mutableListOf<SequentialParser.Node>()
    for (tag in element.tags) {
      nodes.addTagContentNodes(tag, shift)
      val tagType = when (tag.kind) {
        MdxJsxScanner.TagKind.OPENING -> MdxMarkdownLibElementTypes.MDX_JSX_OPENING_ELEMENT
        MdxJsxScanner.TagKind.CLOSING -> MdxMarkdownLibElementTypes.MDX_JSX_CLOSING_ELEMENT
        MdxJsxScanner.TagKind.SELF_CLOSING -> MdxMarkdownLibElementTypes.MDX_JSX_SELF_CLOSING_ELEMENT
      }
      nodes.add(SequentialParser.Node(tag.range.shiftRight(shift).toMarkdownRange(), tagType))
    }
    for (expression in element.expressions) {
      nodes.add(SequentialParser.Node(expression.shiftRight(shift).toMarkdownRange(), MdxMarkdownLibTokenTypes.EMBEDDED_JS_CONTENT))
      nodes.add(SequentialParser.Node(expression.shiftRight(shift).toMarkdownRange(), MdxMarkdownLibElementTypes.MDX_EXPRESSION))
    }
    return nodes
  }

  private fun MutableList<SequentialParser.Node>.addTagContentNodes(tag: MdxJsxScanner.Tag, shift: Int) {
    var offset = tag.range.startOffset
    for (attribute in tag.attributes) {
      addContentNode(TextRange(offset, attribute.startOffset), shift)
      addContentNode(attribute, shift)
      add(SequentialParser.Node(attribute.shiftRight(shift).toMarkdownRange(), MdxMarkdownLibElementTypes.MDX_JSX_ATTRIBUTE))
      offset = attribute.endOffset
    }
    addContentNode(TextRange(offset, tag.range.endOffset), shift)
  }

  private fun MutableList<SequentialParser.Node>.addContentNode(range: TextRange, shift: Int) {
    if (!range.isEmpty) {
      add(SequentialParser.Node(range.shiftRight(shift).toMarkdownRange(), MdxMarkdownLibTokenTypes.EMBEDDED_JS_CONTENT))
    }
  }

}
