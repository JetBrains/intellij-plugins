package org.intellij.plugin.mdx.lang.parse

import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.parser.sequentialparsers.SequentialParser

internal object MdxJsxScanner {
  private const val MAX_SCAN_LENGTH = 250_000

  enum class TagKind {
    OPENING,
    CLOSING,
    SELF_CLOSING
  }

  data class Tag(
    val range: IntRange,
    val kind: TagKind,
    val name: String?,
    val attributes: List<IntRange>,
    val expressions: List<IntRange>,
  )

  data class Element(
    val range: IntRange,
    val tags: List<Tag>,
    val expressions: List<IntRange>,
    val balanced: Boolean,
  )

  data class EsmBlock(
    val range: IntRange,
    val balanced: Boolean,
  )

  fun isLineStartExpression(text: CharSequence, start: Int): Boolean {
    val lineStart = lineStart(text, start)
    val indent = smallIndent(text, lineStart, start)
    return indent != -1 && lineStart + indent == start && text.getOrNull(start) == '{'
  }

  fun isLineStartJsx(text: CharSequence, start: Int): Boolean {
    val lineStart = lineStart(text, start)
    val indent = smallIndent(text, lineStart, start)
    return indent != -1 &&
           lineStart + indent == start &&
           (parseTag(text, start, text.length) != null || isIncompleteOpeningTagStart(text, start, text.length))
  }

  fun isLineStartEsm(text: CharSequence, start: Int): Boolean {
    val lineStart = lineStart(text, start)
    val indent = smallIndent(text, lineStart, start)
    return indent != -1 && lineStart + indent == start && isEsmKeywordAt(text, start)
  }

  fun scanJsxElement(text: CharSequence, start: Int, limit: Int = text.length): Element? {
    if (limit - start > MAX_SCAN_LENGTH) return null
    val opening = parseTag(text, start, limit) ?: return null
    if (opening.kind == TagKind.CLOSING) return null
    if (opening.kind == TagKind.SELF_CLOSING) {
      return Element(start..opening.range.last, listOf(opening), opening.expressions, true)
    }

    val tags = mutableListOf(opening)
    val expressions = opening.expressions.toMutableList()
    val stack = mutableListOf(opening.name)
    var offset = opening.range.last
    while (offset < limit) {
      when (text[offset]) {
        '{' -> {
          val expressionEnd = scanExpression(text, offset, limit)
          if (expressionEnd == -1) {
            return Element(start..limit, tags, expressions, false)
          }
          expressions.add(offset..expressionEnd)
          offset = expressionEnd
        }
        '<' -> {
          val tag = parseTag(text, offset, limit)
          if (tag == null) {
            offset++
            continue
          }
          tags.add(tag)
          expressions.addAll(tag.expressions)
          when (tag.kind) {
            TagKind.OPENING -> stack.add(tag.name)
            TagKind.SELF_CLOSING -> Unit
            TagKind.CLOSING -> {
              val closeIndex = stack.indexOfLast { it == tag.name || it == null || tag.name == null }
              if (closeIndex != -1) {
                while (stack.size > closeIndex) {
                  stack.removeAt(stack.lastIndex)
                }
              }
            }
          }
          offset = tag.range.last
          if (stack.isEmpty()) {
            return Element(start..offset, tags, expressions, true)
          }
        }
        else -> offset++
      }
    }
    return Element(start..limit, tags, expressions, false)
  }

  fun scanExpression(text: CharSequence, start: Int, limit: Int = text.length): Int {
    if (text.getOrNull(start) != '{') return -1
    var offset = start + 1
    var depth = 1
    while (offset < limit) {
      when (text[offset]) {
        '\'' -> offset = scanQuoted(text, offset, limit, '\'')
        '"' -> offset = scanQuoted(text, offset, limit, '"')
        '`' -> offset = scanTemplate(text, offset, limit)
        '/' -> offset = scanSlash(text, offset, limit)
        '{' -> {
          depth++
          offset++
        }
        '}' -> {
          depth--
          offset++
          if (depth == 0) return offset
        }
        else -> offset++
      }
      if (offset == -1) return -1
    }
    return -1
  }

  fun scanEsmBlock(text: CharSequence, start: Int, limit: Int = text.length): EsmBlock? {
    if (limit - start > MAX_SCAN_LENGTH || !isEsmKeywordAt(text, start)) return null
    var offset = start
    var parenDepth = 0
    var braceDepth = 0
    var bracketDepth = 0
    var lastSignificantOffset = -1
    while (offset < limit) {
      when (text[offset]) {
        '\'' -> {
          offset = scanQuoted(text, offset, limit, '\'')
          if (offset != -1) lastSignificantOffset = offset - 1
        }
        '"' -> {
          offset = scanQuoted(text, offset, limit, '"')
          if (offset != -1) lastSignificantOffset = offset - 1
        }
        '`' -> {
          offset = scanTemplate(text, offset, limit)
          if (offset != -1) lastSignificantOffset = offset - 1
        }
        '/' -> {
          val end = scanSlash(text, offset, limit)
          if (end == offset + 1) {
            lastSignificantOffset = offset
          }
          offset = end
        }
        '<' -> {
          val element = scanJsxElement(text, offset, limit)
          if (element != null && element.balanced) {
            offset = element.range.last
            lastSignificantOffset = offset - 1
          }
          else {
            lastSignificantOffset = offset
            offset++
          }
        }
        '(' -> {
          parenDepth++
          lastSignificantOffset = offset
          offset++
        }
        ')' -> {
          if (parenDepth > 0) parenDepth--
          lastSignificantOffset = offset
          offset++
        }
        '{' -> {
          braceDepth++
          lastSignificantOffset = offset
          offset++
        }
        '}' -> {
          if (braceDepth > 0) braceDepth--
          lastSignificantOffset = offset
          offset++
        }
        '[' -> {
          bracketDepth++
          lastSignificantOffset = offset
          offset++
        }
        ']' -> {
          if (bracketDepth > 0) bracketDepth--
          lastSignificantOffset = offset
          offset++
        }
        ';' -> {
          lastSignificantOffset = offset
          offset++
          if (parenDepth == 0 && braceDepth == 0 && bracketDepth == 0) {
            return EsmBlock(start..offset, true)
          }
        }
        '\n' -> {
          if (parenDepth == 0 &&
              braceDepth == 0 &&
              bracketDepth == 0 &&
              isCompleteBeforeLineBreak(text, lastSignificantOffset) &&
              !nextLineContinuesEsm(text, offset + 1, limit, lastSignificantOffset)) {
            return EsmBlock(start..lastSignificantOffset + 1, true)
          }
          offset++
        }
        else -> {
          if (!text[offset].isWhitespace()) {
            lastSignificantOffset = offset
          }
          offset++
        }
      }
      if (offset == -1) return EsmBlock(start..limit, false)
    }
    return EsmBlock(start..limit, parenDepth == 0 && braceDepth == 0 && bracketDepth == 0)
  }

  fun createElementNodes(element: Element,
                         type: IElementType,
                         shift: Int = 0,
                         includeRoot: Boolean = true): List<SequentialParser.Node> {
    val nodes = mutableListOf<SequentialParser.Node>()
    for (tag in element.tags) {
      nodes.addTagContentNodes(tag, shift)
      val tagType = when (tag.kind) {
        TagKind.OPENING -> MdxElementTypes.MDX_JSX_OPENING_ELEMENT
        TagKind.CLOSING -> MdxElementTypes.MDX_JSX_CLOSING_ELEMENT
        TagKind.SELF_CLOSING -> MdxElementTypes.MDX_JSX_SELF_CLOSING_ELEMENT
      }
      nodes.add(SequentialParser.Node(tag.range.shiftRight(shift), tagType))
    }
    for (expression in element.expressions) {
      nodes.add(SequentialParser.Node(expression.shiftRight(shift), MdxTokenTypes.JSX_BLOCK_CONTENT))
      nodes.add(SequentialParser.Node(expression.shiftRight(shift), MdxElementTypes.MDX_JSX_EXPRESSION))
    }
    if (includeRoot) {
      nodes.add(SequentialParser.Node(element.range.shiftRight(shift), type))
    }
    return nodes
  }

  fun createFlowElementNodes(text: CharSequence,
                             element: Element,
                             shift: Int = 0,
                             includeRoot: Boolean = true): List<SequentialParser.Node> {
    val nodes = createElementNodes(element, MdxElementTypes.MDX_JSX_FLOW_ELEMENT, shift, includeRoot = false).toMutableList()
    flowChildParagraphRange(text, element)?.let {
      nodes.add(SequentialParser.Node(it.shiftRight(shift), MarkdownElementTypes.PARAGRAPH))
    }
    if (includeRoot) {
      nodes.add(SequentialParser.Node(element.range.shiftRight(shift), MdxElementTypes.MDX_JSX_FLOW_ELEMENT))
    }
    return nodes
  }

  fun createEsmNodes(block: EsmBlock, shift: Int = 0, includeRoot: Boolean = true): List<SequentialParser.Node> {
    return buildList {
      add(SequentialParser.Node(block.range.shiftRight(shift), MdxTokenTypes.JSX_BLOCK_CONTENT))
      if (includeRoot) {
        add(SequentialParser.Node(block.range.shiftRight(shift), MdxElementTypes.MDX_ESM_BLOCK))
      }
    }
  }

  private fun MutableList<SequentialParser.Node>.addTagContentNodes(tag: Tag, shift: Int) {
    var offset = tag.range.first
    for (attribute in tag.attributes) {
      addContentNode(offset..attribute.first, shift)
      addContentNode(attribute, shift)
      add(SequentialParser.Node(attribute.shiftRight(shift), MdxElementTypes.MDX_JSX_ATTRIBUTE))
      offset = attribute.last
    }
    addContentNode(offset..tag.range.last, shift)
  }

  private fun MutableList<SequentialParser.Node>.addContentNode(range: IntRange, shift: Int) {
    if (range.first < range.last) {
      add(SequentialParser.Node(range.shiftRight(shift), MdxTokenTypes.JSX_BLOCK_CONTENT))
    }
  }

  private fun flowChildParagraphRange(text: CharSequence, element: Element): IntRange? {
    val opening = element.tags.firstOrNull() ?: return null
    val closing = element.tags.lastOrNull() ?: return null
    if (opening.kind == TagKind.SELF_CLOSING || closing.kind != TagKind.CLOSING) {
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

    val paragraphEnd = trimTrailingWhitespace(text, contentStart, firstBlankLineStart(text, firstLineEnd, contentEnd) ?: contentEnd)
    return if (contentStart < paragraphEnd) contentStart..paragraphEnd else null
  }

  private fun firstBlankLineStart(text: CharSequence, firstLineEnd: Int, limit: Int): Int? {
    var lineStart = nextLineStart(text, firstLineEnd, limit)
    while (lineStart < limit) {
      val lineEnd = lineEnd(text, lineStart, limit)
      if (text.subSequence(lineStart, lineEnd).isBlank()) {
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

  private fun parseTag(text: CharSequence, start: Int, limit: Int): Tag? {
    if (text.getOrNull(start) != '<' || start + 1 >= limit) return null
    var offset = start + 1
    val closing = text.getOrNull(offset) == '/'
    if (closing) offset++

    val name: String?
    if (!closing && text.getOrNull(offset) == '>') {
      name = null
      return Tag(start..offset + 1, TagKind.OPENING, name, emptyList(), emptyList())
    }
    else if (closing && text.getOrNull(offset) == '>') {
      name = null
      return Tag(start..offset + 1, TagKind.CLOSING, name, emptyList(), emptyList())
    }
    else {
      val nameStart = offset
      if (!isNameStart(text.getOrNull(offset))) return null
      offset++
      while (offset < limit && isNamePart(text[offset])) {
        offset++
      }
      name = text.subSequence(nameStart, offset).toString()
    }

    if (closing) {
      offset = skipSpaces(text, offset, limit)
      return if (text.getOrNull(offset) == '>') {
        Tag(start..offset + 1, TagKind.CLOSING, name, emptyList(), emptyList())
      }
      else null
    }

    val attributes = mutableListOf<IntRange>()
    val expressions = mutableListOf<IntRange>()
    var selfClosing = false
    while (offset < limit) {
      offset = skipSpaces(text, offset, limit)
      when (text.getOrNull(offset)) {
        null -> return null
        '>' -> return Tag(start..offset + 1, if (selfClosing) TagKind.SELF_CLOSING else TagKind.OPENING, name, attributes, expressions)
        '/' -> {
          val next = skipSpaces(text, offset + 1, limit)
          if (text.getOrNull(next) == '>') {
            return Tag(start..next + 1, TagKind.SELF_CLOSING, name, attributes, expressions)
          }
          selfClosing = true
          offset++
        }
        '{' -> {
          val expressionEnd = scanExpression(text, offset, limit)
          if (expressionEnd == -1) return null
          expressions.add(offset..expressionEnd)
          offset = expressionEnd
        }
        else -> {
          val attributeStart = offset
          if (!isAttributeNameStart(text.getOrNull(offset))) return null
          offset++
          while (offset < limit && isAttributeNamePart(text[offset])) {
            offset++
          }
          offset = skipSpaces(text, offset, limit)
          if (text.getOrNull(offset) == '=') {
            offset = skipSpaces(text, offset + 1, limit)
            when (text.getOrNull(offset)) {
              '\'', '"' -> offset = scanQuoted(text, offset, limit, text[offset])
              '{' -> {
                val expressionEnd = scanExpression(text, offset, limit)
                if (expressionEnd == -1) return null
                expressions.add(offset..expressionEnd)
                offset = expressionEnd
              }
              null -> return null
              else -> {
                while (offset < limit && !text[offset].isWhitespace() && text[offset] != '>' && text[offset] != '/') {
                  offset++
                }
              }
            }
            if (offset == -1) return null
          }
          attributes.add(attributeStart..offset)
        }
      }
    }
    return null
  }

  private fun isIncompleteOpeningTagStart(text: CharSequence, start: Int, limit: Int): Boolean {
    if (text.getOrNull(start) != '<' || start + 1 >= limit || text.getOrNull(start + 1) == '/') return false
    var offset = start + 1
    if (text.getOrNull(offset) == '>') return false
    if (!isNameStart(text.getOrNull(offset))) return false
    offset++
    while (offset < limit && isNamePart(text[offset])) {
      offset++
    }
    return offset == limit || text.subSequence(offset, limit).none { it == '>' || it == '<' }
  }

  private fun scanQuoted(text: CharSequence, start: Int, limit: Int, quote: Char): Int {
    var offset = start + 1
    while (offset < limit) {
      when (text[offset]) {
        '\\' -> offset += 2
        quote -> return offset + 1
        else -> offset++
      }
    }
    return -1
  }

  private fun scanTemplate(text: CharSequence, start: Int, limit: Int): Int {
    var offset = start + 1
    while (offset < limit) {
      when (text[offset]) {
        '\\' -> offset += 2
        '`' -> return offset + 1
        '$' -> {
          if (text.getOrNull(offset + 1) == '{') {
            val expressionEnd = scanExpression(text, offset + 1, limit)
            if (expressionEnd == -1) return -1
            offset = expressionEnd
          }
          else {
            offset++
          }
        }
        else -> offset++
      }
    }
    return -1
  }

  private fun scanSlash(text: CharSequence, start: Int, limit: Int): Int {
    if (text.getOrNull(start + 1) == '/') {
      var offset = start + 2
      while (offset < limit && text[offset] != '\n') {
        offset++
      }
      return offset
    }
    if (text.getOrNull(start + 1) == '*') {
      var offset = start + 2
      while (offset + 1 < limit) {
        if (text[offset] == '*' && text[offset + 1] == '/') return offset + 2
        offset++
      }
      return -1
    }
    return start + 1
  }

  private fun isCompleteBeforeLineBreak(text: CharSequence, lastSignificantOffset: Int): Boolean {
    if (lastSignificantOffset == -1) return false
    val char = text[lastSignificantOffset]
    if (char in LINE_END_CONTINUATION_CHARS) return false
    return !(char == '>' && text.getOrNull(lastSignificantOffset - 1) == '=')
  }

  private fun nextLineContinuesEsm(text: CharSequence, start: Int, limit: Int, previousSignificantOffset: Int): Boolean {
    val next = firstNonWhitespaceOffset(text, start, limit)
    if (next == -1) return false
    if (hasLineBreakBefore(text, start, next)) return false
    val lineStart = lineStart(text, next)
    if (smallIndent(text, lineStart, next) == -1) return true
    if (keywordAt(text, next, "from")) return true
    if (keywordEndsAt(text, previousSignificantOffset, "from")) {
      return text[next] == '\'' || text[next] == '"' || text[next] == '`'
    }
    if (keywordEndsAt(text, previousSignificantOffset, "import")) {
      return isImportContinuationStart(text[next])
    }
    if (keywordEndsAt(text, previousSignificantOffset, "export")) {
      return isExportContinuationStart(text[next])
    }
    return text[next] in NEXT_LINE_CONTINUATION_CHARS
  }

  private fun keywordEndsAt(text: CharSequence, endOffset: Int, keyword: String): Boolean {
    val start = endOffset - keyword.length + 1
    return start >= 0 && keywordAt(text, start, keyword)
  }

  private fun isImportContinuationStart(char: Char): Boolean {
    return char == '{' || char == '*' || char == '\'' || char == '"' || char == '`' || isNameStart(char)
  }

  private fun isExportContinuationStart(char: Char): Boolean {
    return char == '{' || char == '*' || isNameStart(char)
  }

  private fun hasLineBreakBefore(text: CharSequence, start: Int, end: Int): Boolean {
    var offset = start
    while (offset < end) {
      if (text[offset] == '\n') return true
      offset++
    }
    return false
  }

  private fun firstNonWhitespaceOffset(text: CharSequence, start: Int, limit: Int): Int {
    var offset = start
    while (offset < limit) {
      if (!text[offset].isWhitespace()) {
        return offset
      }
      offset++
    }
    return -1
  }

  private fun skipSpaces(text: CharSequence, start: Int, limit: Int): Int {
    var offset = start
    while (offset < limit && text[offset].isWhitespace()) {
      offset++
    }
    return offset
  }

  private fun isNameStart(char: Char?): Boolean {
    return char != null && (char.isLetter() || char == '_')
  }

  private fun isNamePart(char: Char): Boolean {
    return char.isLetterOrDigit() || char == '_' || char == '-' || char == '.' || char == ':'
  }

  private fun isAttributeNameStart(char: Char?): Boolean {
    return char != null && (char.isLetter() || char == '_' || char == ':' || char == '$')
  }

  private fun isAttributeNamePart(char: Char): Boolean {
    return char.isLetterOrDigit() || char == '_' || char == '-' || char == '.' || char == ':' || char == '$'
  }

  private fun isEsmKeywordAt(text: CharSequence, start: Int): Boolean {
    return keywordAt(text, start, "import") || keywordAt(text, start, "export")
  }

  private fun keywordAt(text: CharSequence, start: Int, keyword: String): Boolean {
    if (start + keyword.length > text.length) return false
    for (index in keyword.indices) {
      if (text[start + index] != keyword[index]) return false
    }
    val before = text.getOrNull(start - 1)
    val after = text.getOrNull(start + keyword.length)
    return before?.let { !isNamePart(it) } != false && after?.let { !isNamePart(it) } != false
  }

  private fun lineStart(text: CharSequence, offset: Int): Int {
    var lineStart = offset.coerceAtMost(text.length)
    while (lineStart > 0 && text[lineStart - 1] != '\n') {
      lineStart--
    }
    return lineStart
  }

  private fun smallIndent(text: CharSequence, lineStart: Int, offset: Int): Int {
    var indent = 0
    while (lineStart + indent < offset && text[lineStart + indent] == ' ') {
      indent++
    }
    return if (indent <= 3) indent else -1
  }

  private fun CharSequence.getOrNull(index: Int): Char? {
    return if (index in indices) this[index] else null
  }

  private fun IntRange.shiftRight(delta: Int): IntRange {
    return first + delta..last + delta
  }

  private val LINE_END_CONTINUATION_CHARS = setOf('=', '+', '-', '*', '/', '%', '&', '|', '^', '!', '~', '?', '.', ',', ':', '(', '[', '{', '<')
  private val NEXT_LINE_CONTINUATION_CHARS = setOf('.', '?', ':', ',', '+', '-', '*', '/', '%', '&', '|', ')', ']', '}', '(')
}
