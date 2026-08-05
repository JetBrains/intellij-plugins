package org.intellij.plugin.mdx.lang.parse

import com.intellij.xml.util.HtmlUtil
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

  /**
   * True when the caret at [offset] sits in a JSX-tag-start context: the character just before the
   * caret is a `<`, or the caret follows a freshly-typed `<Name` prefix (walk back over JSX name
   * characters to a `<`). Used by the auto-popup enabler to surface JSX tag-name completion for an
   * unbalanced `<`/`<My`, which the parser does not yet project into the MdxJS layer. WEB-78468.
   */
  fun isJsxTagStartContext(text: CharSequence, offset: Int): Boolean {
    if (offset <= 0 || offset > text.length) return false
    var index = offset - 1
    if (text[index] == '<') return true
    // Walk back over a partial JSX tag name (e.g. `My`, `Foo.Bar`) to the `<` that opened it.
    while (index >= 0 && isNamePart(text[index])) {
      index--
    }
    return index >= 0 && text[index] == '<'
  }

  /**
   * If a `<Name…` opening tag that has not yet been closed with `>` begins at [start] (see
   * [isIncompleteOpeningTagStart]), returns its range up to the logical end of a "clean" prefix — a
   * `<Name` with optional, fully-formed attributes, ending at a line break or end of input. A dangling
   * `{…` expression or unterminated quote (e.g. the static `<Broken attr={"unterminated}`) yields
   * `null` so it keeps its existing outer-language parse rather than being reinterpreted as JSX.
   * Used to project a freshly-typed `<My` into the MdxJS layer so the platform JSX tag-name completion
   * runs while the tag is still unbalanced. Ranges use an exclusive `.last` (a text offset), matching
   * the other MdxJsxScanner ranges. WEB-78468.
   */
  fun incompleteOpeningTagRange(text: CharSequence, start: Int, limit: Int = text.length): IntRange? {
    if (!isIncompleteOpeningTagStart(text, start, limit)) return null
    var offset = start + 1
    while (offset < limit && isNamePart(text[offset])) {
      offset++
    }
    while (offset < limit) {
      when (text[offset]) {
        '\n' -> return start..offset
        '{' -> {
          val expressionEnd = scanExpression(text, offset, limit)
          if (expressionEnd == -1) return null
          offset = expressionEnd
        }
        '\'', '"' -> {
          val quoteEnd = scanQuoted(text, offset, limit, text[offset])
          if (quoteEnd == -1) return null
          offset = quoteEnd
        }
        else -> offset++
      }
    }
    return start..limit
  }

  /**
   * When a `>` is about to be typed at [caretOffset], returns the closing tag to auto-insert (e.g.
   * `</div>`, `</Foo>`, or `</>` for a fragment), or `null` when no closing tag should be inserted.
   *
   * Operates on the document [text] directly (the source of truth at typing time): walks back from the
   * caret to the `<` that opens the current tag and, if that tag is an unclosed opening tag, derives
   * its name. Returns `null` for closing tags (`</…`), already-`>`-terminated tags, and self-closing
   * (`/`) tags, and void HTML elements (`<br>`, `<input>`, …), which have no closing tag. WEB-78468.
   */
  fun closingTagToInsert(text: CharSequence, caretOffset: Int): String? {
    if (caretOffset <= 0 || caretOffset > text.length) return null
    // Find the `<` that opens the tag containing the caret. A completed `{…}` attribute expression or
    // quoted value ending just before the caret is part of the opening tag (e.g. a multi-line
    // `onClick={() => { … }}`), so skip each as a unit — otherwise its internal `>`/`<`/newlines/quotes
    // would abort the search for the tag's `<`. A top-level `>` (tag already closed), `<` (tag start),
    // or newline still stops the walk. WEB-78468.
    var index = caretOffset - 1
    walk@ while (index >= 0) {
      when (text[index]) {
        '}' -> {
          val expressionStart = matchingExpressionStart(text, index) ?: return null
          index = expressionStart - 1
        }
        '\'', '"', '`' -> {
          val quoteStart = matchingQuoteStart(text, index) ?: return null
          index = quoteStart - 1
        }
        '>' -> return null
        '<' -> break@walk
        '\n' -> return null
        else -> index--
      }
    }
    if (index < 0 || text[index] != '<') return null
    val tagStart = index
    // A closing tag `</…` never auto-closes.
    if (text.getOrNull(tagStart + 1) == '/') return null
    // Fragment: a bare `<` directly before the caret.
    if (tagStart == caretOffset - 1) return "</>"
    // Otherwise it must be a `<name` opening tag with a valid JSX/HTML name.
    var nameEnd = tagStart + 1
    if (!isNameStart(text.getOrNull(nameEnd))) return null
    nameEnd++
    while (nameEnd < text.length && nameEnd < caretOffset && isNamePart(text[nameEnd])) {
      nameEnd++
    }
    val name = text.subSequence(tagStart + 1, nameEnd).toString()
    if (name.isEmpty()) return null
    // The `>` only terminates the opening tag when the caret sits at the tag's top level. If it is
    // inside a JSX `{…}` attribute expression (e.g. completing an `=>` arrow or an `a > b` comparison)
    // or inside a quoted attribute value, the typed `>` is ordinary expression/string text, not the
    // tag terminator, so no closing tag should be inserted. WEB-78468.
    if (caretInsideTagExpressionOrQuote(text, nameEnd, caretOffset)) return null
    // Reject a self-closing `<name … /` whose `/` precedes the caret.
    if (text.getOrNull(caretOffset - 1) == '/') return null
    // Void HTML elements (input, br, img, …) have no closing tag; JSX requires them self-closed, so a
    // typed `>` must not insert `</input>`. Case-sensitive: a capitalized component (never a void
    // element) such as `<Input>` still auto-closes.
    if (HtmlUtil.isSingleHtmlTag(name, true)) return null
    return "</$name>"
  }

  /**
   * Scans the opening-tag body from [from] (just past the tag name) and reports whether [caretOffset]
   * falls inside a JSX `{…}` expression or a quoted attribute value. Such a span swallows a typed `>`
   * as expression/string text rather than the tag terminator. An unterminated `{…`/quote that reaches
   * the caret also counts (the caret is inside it). The backward walk in [closingTagToInsert] already
   * guarantees no `<`/`>` appears between the tag start and the caret, so only `{`/quotes need tracking.
   */
  private fun caretInsideTagExpressionOrQuote(text: CharSequence, from: Int, caretOffset: Int): Boolean {
    var offset = from
    while (offset < caretOffset) {
      when (text[offset]) {
        '{' -> {
          val expressionEnd = scanExpression(text, offset, text.length)
          if (expressionEnd == -1 || caretOffset < expressionEnd) return true
          offset = expressionEnd
        }
        '\'', '"' -> {
          val quoteEnd = scanQuoted(text, offset, text.length, text[offset])
          if (quoteEnd == -1 || caretOffset < quoteEnd) return true
          offset = quoteEnd
        }
        else -> offset++
      }
    }
    return false
  }

  /**
   * Given the `}` at [closeBrace], returns the offset of the `{` that opens the matching JSX expression,
   * or `null` if there is none. The candidate `{` (found by a brace-depth walk) is confirmed with the
   * string/template/comment-aware forward [scanExpression] — so an unmatched or string-embedded brace
   * yields `null` (no auto-close) rather than a wrong match. WEB-78468.
   */
  private fun matchingExpressionStart(text: CharSequence, closeBrace: Int): Int? {
    var depth = 0
    var i = closeBrace
    while (i >= 0) {
      when (text[i]) {
        '}' -> depth++
        '{' -> {
          depth--
          if (depth == 0) {
            return if (scanExpression(text, i, text.length) == closeBrace + 1) i else null
          }
        }
      }
      i--
    }
    return null
  }

  /**
   * Given the quote at [closeQuote], returns the offset of the opening quote of the matching quoted
   * value, or `null` if there is none on the same line. Candidates are confirmed with the escape-aware
   * forward [scanQuoted]; a quoted attribute value does not span lines, so a newline stops the search.
   * WEB-78468.
   */
  private fun matchingQuoteStart(text: CharSequence, closeQuote: Int): Int? {
    val quote = text[closeQuote]
    var i = closeQuote - 1
    while (i >= 0) {
      if (text[i] == '\n') return null
      if (text[i] == quote && scanQuoted(text, i, text.length, quote) == closeQuote + 1) return i
      i--
    }
    return null
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
      // Skip fenced code blocks whole: their {/}/< are code, not MDX expressions or tags.
      if (isAtLineStart(text, offset)) {
        val fenceEnd = skipCodeFence(text, offset, limit)
        if (fenceEnd != -1) {
          offset = fenceEnd
          continue
        }
      }
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
              val top = stack.lastOrNull()
              if (top == tag.name || top == null || tag.name == null) {
                stack.removeAt(stack.lastIndex)
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

    // Stop at the same boundary (blank line or code fence) the block parser would start a new block at,
    // or the paragraph overlaps the sibling block and crashes with "Intersecting parsed nodes".
    val regionEnd = firstParagraphBoundary(text, firstLineEnd, contentEnd) ?: contentEnd
    // Keep trailing spaces when continuing past the first line: the sub-block parser's paragraph for that
    // continuation line keeps them too, so trimming here would only partially overlap it.
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
      if (skipCodeFence(text, lineStart, limit) != -1) {
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

  private fun isAtLineStart(text: CharSequence, offset: Int): Boolean {
    return offset == 0 || text.getOrNull(offset - 1) == '\n'
  }

  /**
   * True if [offset] falls inside a fenced code block that opens at or after [from]. Scans line by line from
   * [from] (a line start), jumping over whole fences via [skipCodeFence]. Used to keep fence interiors opaque:
   * a `</tag>` line inside a fence must not be treated as a flow-element closing tag by the JSX constraints. WEB-78468.
   */
  fun isInsideCodeFence(text: CharSequence, from: Int, offset: Int): Boolean {
    var lineStart = from.coerceAtLeast(0)
    while (lineStart < offset && lineStart < text.length) {
      val fenceEnd = skipCodeFence(text, lineStart, text.length)
      if (fenceEnd != -1) {
        if (offset < fenceEnd) return true
        lineStart = nextLineStart(text, fenceEnd, text.length)
      }
      else {
        lineStart = nextLineStart(text, lineEnd(text, lineStart, text.length), text.length)
      }
    }
    return false
  }

  /**
   * The contiguous ranges of fenced code blocks opening on their own line within `[start, limit)` — each from
   * the opener line start to just past the closing fence. Unlike the lexer's HARD-blocked token ranges (which
   * are fragmented by the fence-info and newline tokens), these are whole-fence spans, so the MdxJS projection
   * can carve a fence out of an enclosing JSX flow element (the fence stays OUTER while the element around it
   * is still projected as a real tag). WEB-78468.
   */
  fun codeFenceRanges(text: CharSequence, start: Int, limit: Int): List<IntRange> {
    val ranges = mutableListOf<IntRange>()
    var lineStart = start
    while (lineStart < limit) {
      val fenceEnd = if (isAtLineStart(text, lineStart)) skipCodeFence(text, lineStart, limit) else -1
      if (fenceEnd != -1) {
        ranges.add(lineStart..fenceEnd)
        lineStart = nextLineStart(text, fenceEnd, limit)
      }
      else {
        lineStart = nextLineStart(text, lineEnd(text, lineStart, limit), limit)
      }
    }
    return ranges
  }

  /**
   * If the line at [start] opens a fenced code block (any amount of leading spaces, 3+ backticks or
   * tildes), returns the offset just past the closing fence (or [limit] if the fence is unterminated);
   * otherwise returns -1. The fence body is treated as opaque so that `{`/`}`/`<` inside it are not
   * scanned as MDX expressions or JSX tags.
   *
   * Unlike CommonMark's top-level ≤3-space rule ([smallIndent]), any indentation is accepted here: this
   * runs while scanning inside a JSX flow body, where a fence is indented to align with its container
   * (e.g. four spaces under `<div>`). The block parser already treats such a fence as a fence, so with the
   * ≤3 cap an incremental re-lex would fail to skip an indented fence and scan its `{`/`}` as JSX, emitting
   * a node that intersects the CODE_FENCE_CONTENT. Accepting the wider indent keeps the two consistent. WEB-78468.
   */
  private fun skipCodeFence(text: CharSequence, start: Int, limit: Int): Int {
    val indent = fenceIndent(text, start, limit)
    val fenceStart = start + indent
    val fenceChar = text.getOrNull(fenceStart) ?: return -1
    if (fenceChar != '`' && fenceChar != '~') return -1
    var marker = 0
    while (fenceStart + marker < limit && text[fenceStart + marker] == fenceChar) {
      marker++
    }
    if (marker < 3) return -1
    // A ``` fence-info string must not itself contain a backtick (CommonMark rule); ~~~ has no such
    // restriction. This keeps an inline ``code`` span from being mistaken for a fence opener.
    var infoEnd = fenceStart + marker
    while (infoEnd < limit && text[infoEnd] != '\n') {
      if (fenceChar == '`' && text[infoEnd] == '`') return -1
      infoEnd++
    }
    var lineStart = nextLineStart(text, infoEnd, limit)
    while (lineStart < limit) {
      val contentStart = lineStart + fenceIndent(text, lineStart, limit)
      var closeMarker = 0
      while (contentStart + closeMarker < limit && text[contentStart + closeMarker] == fenceChar) {
        closeMarker++
      }
      if (closeMarker >= marker && isBlankUntilLineEnd(text, contentStart + closeMarker, limit)) {
        return lineEnd(text, contentStart + closeMarker, limit)
      }
      lineStart = nextLineStart(text, lineEnd(text, lineStart, limit), limit)
    }
    return limit
  }

  /** Counts leading spaces of the line at [lineStart] with no ≤3 cap, for opaque fence-skipping inside JSX bodies. */
  private fun fenceIndent(text: CharSequence, lineStart: Int, limit: Int): Int {
    var indent = 0
    while (lineStart + indent < limit && text[lineStart + indent] == ' ') {
      indent++
    }
    return indent
  }

  private fun isBlankUntilLineEnd(text: CharSequence, start: Int, limit: Int): Boolean {
    var offset = start
    while (offset < limit && text[offset] != '\n') {
      if (!text[offset].isWhitespace()) return false
      offset++
    }
    return true
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
    // A `>`/`<` outside an expression breaks the tag; one inside an unterminated `{...}` attribute
    // (e.g. an `=>` arrow) does not, so expressions are skipped as a unit rather than scanned char-by-char.
    while (offset < limit) {
      when (text[offset]) {
        '>', '<' -> return false
        '\n' -> return true
        '{' -> {
          val expressionEnd = scanExpression(text, offset, limit)
          if (expressionEnd == -1) return true
          offset = expressionEnd
        }
        '\'', '"' -> {
          val quoteEnd = scanQuoted(text, offset, limit, text[offset])
          if (quoteEnd == -1) return true
          offset = quoteEnd
        }
        else -> offset++
      }
    }
    return true
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
