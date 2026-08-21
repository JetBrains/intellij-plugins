package org.intellij.plugin.mdx.lang.parse

import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.parser.sequentialparsers.SequentialParser

internal object MdxBlockNodeFactory {
  fun createFlowElementNodes(
    text: CharSequence,
    element: MdxJsxScanner.Element,
    shift: Int = 0,
    includeRoot: Boolean = true,
  ): List<SequentialParser.Node> {
    val nodes = createElementNodes(element, shift).toMutableList()
    flowChildParagraphRange(text, element)?.let {
      nodes.add(SequentialParser.Node(it.shiftRight(shift), MarkdownElementTypes.PARAGRAPH))
    }
    if (includeRoot) {
      nodes.add(SequentialParser.Node(element.range.shiftRight(shift), MdxMarkdownLibElementTypes.MDX_JSX_FLOW_ELEMENT))
    }
    return nodes
  }

  fun createEsmNodes(
    block: MdxEsmScanner.Block,
    shift: Int = 0,
    includeRoot: Boolean = true,
  ): List<SequentialParser.Node> {
    return buildList {
      add(SequentialParser.Node(block.range.shiftRight(shift), MdxMarkdownLibTokenTypes.EMBEDDED_JS_CONTENT))
      if (includeRoot) {
        add(SequentialParser.Node(block.range.shiftRight(shift), MdxMarkdownLibElementTypes.MDX_ESM_BLOCK))
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
      nodes.add(SequentialParser.Node(tag.range.shiftRight(shift), tagType))
    }
    for (expression in element.expressions) {
      nodes.add(SequentialParser.Node(expression.shiftRight(shift), MdxMarkdownLibTokenTypes.EMBEDDED_JS_CONTENT))
      nodes.add(SequentialParser.Node(expression.shiftRight(shift), MdxMarkdownLibElementTypes.MDX_EXPRESSION))
    }
    return nodes
  }

  private fun MutableList<SequentialParser.Node>.addTagContentNodes(tag: MdxJsxScanner.Tag, shift: Int) {
    var offset = tag.range.first
    for (attribute in tag.attributes) {
      addContentNode(offset..attribute.first, shift)
      addContentNode(attribute, shift)
      add(SequentialParser.Node(attribute.shiftRight(shift), MdxMarkdownLibElementTypes.MDX_JSX_ATTRIBUTE))
      offset = attribute.last
    }
    addContentNode(offset..tag.range.last, shift)
  }

  private fun MutableList<SequentialParser.Node>.addContentNode(range: IntRange, shift: Int) {
    if (range.first < range.last) {
      add(SequentialParser.Node(range.shiftRight(shift), MdxMarkdownLibTokenTypes.EMBEDDED_JS_CONTENT))
    }
  }

  private fun flowChildParagraphRange(text: CharSequence, element: MdxJsxScanner.Element): IntRange? {
    val opening = element.tags.firstOrNull() ?: return null
    val closing = element.tags.lastOrNull() ?: return null
    if (opening.kind == MdxJsxScanner.TagKind.SELF_CLOSING || closing.kind != MdxJsxScanner.TagKind.CLOSING) {
      return null
    }

    val contentStart = opening.range.last
    val contentEnd = closing.range.first
    if (contentStart >= contentEnd) {
      return null
    }

    val firstLineEnd = lineEnd(text, contentStart, contentEnd)
    if (text.subSequence(contentStart, firstLineEnd).isBlank()) {
      return null
    }

    // Stop where the block parser starts a sibling block, or the paragraph ranges would overlap.
    val regionEnd = firstParagraphBoundary(text, firstLineEnd, contentEnd) ?: contentEnd
    // A continued paragraph keeps trailing spaces, while a single-line paragraph does not.
    val paragraphEnd = if (regionEnd > firstLineEnd) trimTrailingLineBreaks(text, contentStart, regionEnd)
                       else trimTrailingWhitespace(text, contentStart, regionEnd)
    return if (contentStart < paragraphEnd) contentStart..paragraphEnd else null
  }

  private fun firstParagraphBoundary(text: CharSequence, firstLineEnd: Int, limit: Int): Int? {
    var lineStart = nextLineStart(text, firstLineEnd, limit)
    while (lineStart < limit) {
      val lineEnd = lineEnd(text, lineStart, limit)
      if (text.subSequence(lineStart, lineEnd).isBlank()) {
        return lineStart
      }
      if (MdxMarkdownFenceScanner.findEnd(text, lineStart, limit) != -1) {
        return lineStart
      }
      lineStart = nextLineStart(text, lineEnd, limit)
    }
    return null
  }

  private fun lineEnd(text: CharSequence, start: Int, limit: Int): Int {
    var offset = start
    while (offset < limit && text[offset] != '\n') {
      offset++
    }
    return offset
  }

  private fun nextLineStart(text: CharSequence, lineEnd: Int, limit: Int): Int {
    return if (lineEnd < limit && text[lineEnd] == '\n') lineEnd + 1 else limit
  }

  private fun trimTrailingWhitespace(text: CharSequence, start: Int, end: Int): Int {
    var offset = end
    while (offset > start && text[offset - 1].isWhitespace()) {
      offset--
    }
    return offset
  }

  private fun trimTrailingLineBreaks(text: CharSequence, start: Int, end: Int): Int {
    var offset = end
    while (offset > start && (text[offset - 1] == '\n' || text[offset - 1] == '\r')) {
      offset--
    }
    return offset
  }

  private fun IntRange.shiftRight(delta: Int): IntRange {
    return first + delta..last + delta
  }
}
