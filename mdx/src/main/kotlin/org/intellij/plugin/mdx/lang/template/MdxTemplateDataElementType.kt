package org.intellij.plugin.mdx.lang.template

import com.intellij.lang.javascript.types.JSEmbeddedBlockElementType
import com.intellij.lexer.Lexer
import com.intellij.openapi.util.TextRange
import com.intellij.psi.templateLanguages.TemplateDataElementType
import com.intellij.psi.templateLanguages.TemplateDataModifications
import com.intellij.psi.tree.TokenSet
import org.intellij.plugin.mdx.lang.MdxLanguage
import org.intellij.plugin.mdx.lang.parse.MdxJsxScanner
import org.intellij.plugin.mdx.lang.parse.MdxTokenTypes
import org.intellij.plugin.mdx.lang.template.MdxTemplateElementTypes
import org.intellij.plugins.markdown.lang.MarkdownTokenTypes

object MdxTemplateDataElementType : MdxTemplateDataElementTypeBase(),
                                    JSEmbeddedBlockElementType {

  override fun isModule(): Boolean = true
}


open class MdxTemplateDataElementTypeBase : TemplateDataElementType("MDX_TEMPLATE_JSX",
                                                                    MdxLanguage,
                                                                    MdxTokenTypes.EMBEDDED_JS_CONTENT,
                                                                    MdxTemplateElementTypes.OUTER_MARKDOWN_CONTENT) {
  override fun collectTemplateModifications(sourceCode: CharSequence, baseLexer: Lexer): TemplateDataModifications {
    val modifications = TemplateDataModifications()
    val templateRanges = collectTemplateRanges(sourceCode, baseLexer)
    var offset = 0
    var pendingSemicolon = false
    for (range in templateRanges) {
      if (offset < range.first) {
        if (pendingSemicolon) {
          modifications.addRangeToRemove(offset, ";")
          pendingSemicolon = false
        }
        modifications.addOuterRange(TextRange.create(offset, range.first), true)
      }
      if (pendingSemicolon) {
        modifications.addRangeToRemove(range.first, ";")
      }
      // Hide one dash of a single-line `<!--` opener from the JS view: JS otherwise reads it as an
      // XML-style comment, but `<!-` is a malformed JSX tag, so the JSX parser reports it as an error.
      if (isInvalidSingleLineComment(sourceCode, range)) {
        modifications.addOuterRange(TextRange.create(range.first + 3, range.first + 4), true)
      }
      pendingSemicolon = needsStatementSemicolon(sourceCode, range)
      offset = range.last
    }
    if (offset < sourceCode.length) {
      if (pendingSemicolon) {
        modifications.addRangeToRemove(offset, ";")
        pendingSemicolon = false
      }
      modifications.addOuterRange(TextRange.create(offset, sourceCode.length), true)
    }
    if (pendingSemicolon) {
      modifications.addRangeToRemove(sourceCode.length, ";")
    }
    return modifications
  }

  private fun collectTemplateRanges(sourceCode: CharSequence, baseLexer: Lexer): List<IntRange> {
    val blockedRanges = collectBlockedRanges(sourceCode, baseLexer)
    val ranges = mutableListOf<TemplateRange>()
    var offset = 0
    while (offset < sourceCode.length) {
      val blockedRange = blockedRanges.firstOrNull {
        it.kind == BlockedRangeKind.HARD && offset >= it.range.first && offset < it.range.last
      }
      if (blockedRange != null) {
        offset = blockedRange.range.last
        continue
      }

      // A single-line `<!--...-->` is invalid MDX; route it to JSX explicitly since the scanner itself
      // rejects `<!`.
      val invalidComment = blockedRanges.firstOrNull {
        it.kind == BlockedRangeKind.INVALID_COMMENT && offset == it.range.first
      }
      if (invalidComment != null) {
        ranges.add(TemplateRange(invalidComment.range, TemplateRangeKind.JSX))
        offset = invalidComment.range.last
        continue
      }

      when (sourceCode[offset]) {
        '<' -> {
          val element = MdxJsxScanner.scanJsxElement(sourceCode, offset)
          if (element != null && element.balanced && !element.range.intersectsAny(blockedRanges, sourceCode)) {
            ranges.add(TemplateRange(element.range, TemplateRangeKind.JSX))
            offset = element.range.last
            continue
          }
          // A balanced element whose only opaque content is code fences (e.g. `<div>` wrapping a fence) is
          // still projected as a real tag: carve the fences out so they stay OUTER and project the rest.
          if (element != null && element.balanced) {
            val fences = MdxJsxScanner.codeFenceRanges(sourceCode, element.range.first, element.range.last)
            if (fences.isNotEmpty()) {
              val carved = carveAround(element.range, fences)
              if (carved.none { it.intersectsAny(blockedRanges, sourceCode) }) {
                carved.forEach { ranges.add(TemplateRange(it, TemplateRangeKind.JSX)) }
                offset = element.range.last
                continue
              }
            }
          }
          // A still-unbalanced `<Name…` prefix (mid-typing) would otherwise stay outside the MdxJS layer,
          // so JSX tag-name completion would find nothing; project just the incomplete opening tag.
          val incompleteRange = MdxJsxScanner.incompleteOpeningTagRange(sourceCode, offset)
          if (incompleteRange != null && !incompleteRange.intersectsAny(blockedRanges, sourceCode)) {
            ranges.add(TemplateRange(incompleteRange, TemplateRangeKind.JSX))
            offset = incompleteRange.last
            continue
          }
        }
        '{' -> {
          val expressionEnd = MdxJsxScanner.scanExpression(sourceCode, offset)
          if (expressionEnd != -1 && !(offset..expressionEnd).intersectsAny(blockedRanges, sourceCode)) {
            ranges.add(TemplateRange(offset..expressionEnd, TemplateRangeKind.EXPRESSION))
            offset = expressionEnd
            continue
          }
        }
        else -> {
          if (MdxJsxScanner.isLineStartEsm(sourceCode, offset)) {
            val block = MdxJsxScanner.scanEsmBlock(sourceCode, offset)
            if (block != null && !block.range.intersectsAny(blockedRanges, sourceCode)) {
              ranges.add(TemplateRange(block.range, TemplateRangeKind.ESM))
              offset = block.range.last
              continue
            }
          }
        }
      }
      offset++
    }
    return ranges.mergeTemplateRanges(sourceCode).map { it.range }
  }

  private fun collectBlockedRanges(sourceCode: CharSequence, baseLexer: Lexer): List<BlockedRange> {
    val ranges = mutableListOf<BlockedRange>()
    var htmlCommentStart = -1
    baseLexer.start(sourceCode)
    while (baseLexer.tokenType != null) {
      val tokenStart = baseLexer.tokenStart
      val tokenEnd = baseLexer.tokenEnd
      val tokenText = sourceCode.subSequence(tokenStart, tokenEnd)
      if (baseLexer.tokenType == MarkdownTokenTypes.CODE_LINE) {
        ranges.add(BlockedRange(tokenStart..tokenEnd, BlockedRangeKind.INDENTED_CODE))
      }
      else if (baseLexer.tokenType in HARD_CODE_TOKENS) {
        ranges.add(BlockedRange(tokenStart..tokenEnd, BlockedRangeKind.HARD))
      }

      var tokenOffset = 0
      while (tokenOffset < tokenText.length) {
        if (htmlCommentStart == -1) {
          val startInToken = tokenText.indexOf("<!--", tokenOffset)
          if (startInToken == -1) break
          htmlCommentStart = tokenStart + startInToken
          tokenOffset = startInToken + 4
        }
        val endInToken = tokenText.indexOf("-->", tokenOffset)
        if (endInToken == -1) break
        val commentEnd = tokenStart + endInToken + 3
        ranges.add(BlockedRange(htmlCommentStart..commentEnd, commentBlockedKind(sourceCode, htmlCommentStart, commentEnd)))
        htmlCommentStart = -1
        tokenOffset = endInToken + 3
      }
      baseLexer.advance()
    }
    if (htmlCommentStart != -1) {
      ranges.add(BlockedRange(htmlCommentStart..sourceCode.length, BlockedRangeKind.HARD))
    }
    return ranges.mergeTouchingBlockedRanges()
  }

  // Multi-line `<!-- ... -->` stays opaque (HARD, never an error); only single-line is invalid MDX.
  private fun commentBlockedKind(sourceCode: CharSequence, start: Int, end: Int): BlockedRangeKind {
    val spansSingleLine = (start until end).none { sourceCode[it] == '\n' }
    return if (spansSingleLine) BlockedRangeKind.INVALID_COMMENT else BlockedRangeKind.HARD
  }

  private fun isInvalidSingleLineComment(sourceCode: CharSequence, range: IntRange): Boolean {
    val text = sourceCode.subSequence(range.first, range.last)
    return text.startsWith("<!--") && text.endsWith("-->") && text.none { it == '\n' }
  }

  private fun needsStatementSemicolon(sourceCode: CharSequence, range: IntRange): Boolean {
    val text = sourceCode.subSequence(range.first, range.last).trim()
    return (text.startsWith("import") || text.startsWith("export") || text.startsWith("<")) && !text.endsWith(';')
  }

  private fun List<TemplateRange>.mergeTemplateRanges(sourceCode: CharSequence): List<TemplateRange> {
    if (isEmpty()) return emptyList()
    val sorted = sortedBy { it.range.first }
    val result = mutableListOf<TemplateRange>()
    var current = sorted.first()
    for (range in sorted.drop(1)) {
      if (range.range.first <= current.range.last || canMergeThroughSingleLineBreak(current, range, sourceCode)) {
        current = TemplateRange(current.range.first..maxOf(current.range.last, range.range.last), current.kind)
      }
      else {
        result.add(current)
        current = range
      }
    }
    result.add(current)
    return result
  }

  private fun canMergeThroughSingleLineBreak(left: TemplateRange, right: TemplateRange, sourceCode: CharSequence): Boolean {
    if (left.kind != TemplateRangeKind.ESM || right.kind != TemplateRangeKind.ESM) return false
    val gap = sourceCode.subSequence(left.range.last, right.range.first)
    return gap.isNotEmpty() && gap.all { it.isWhitespace() } && gap.count { it == '\n' } == 1
  }

  private fun List<BlockedRange>.mergeTouchingBlockedRanges(): List<BlockedRange> {
    if (isEmpty()) return emptyList()
    val sorted = sortedWith(compareBy<BlockedRange> { it.range.first }.thenBy { it.range.last })
    val result = mutableListOf<BlockedRange>()
    var current = sorted.first()
    for (range in sorted.drop(1)) {
      if (range.kind == current.kind && range.range.first <= current.range.last) {
        current = BlockedRange(current.range.first..maxOf(current.range.last, range.range.last), current.kind)
      }
      else {
        result.add(current)
        current = range
      }
    }
    result.add(current)
    return result
  }

  /** [range] split into the sub-ranges left after removing [gaps] (which stay OUTER); empty pieces dropped. */
  private fun carveAround(range: IntRange, gaps: List<IntRange>): List<IntRange> {
    val result = mutableListOf<IntRange>()
    var cursor = range.first
    for (gap in gaps.sortedBy { it.first }) {
      val gapStart = gap.first.coerceIn(range.first, range.last)
      val gapEnd = gap.last.coerceIn(range.first, range.last)
      if (cursor < gapStart) result.add(cursor..gapStart)
      cursor = maxOf(cursor, gapEnd)
    }
    if (cursor < range.last) result.add(cursor..range.last)
    return result
  }

  private fun IntRange.intersectsAny(ranges: List<BlockedRange>, sourceCode: CharSequence): Boolean {
    return ranges.any { blockedRange ->
      first < blockedRange.range.last &&
      blockedRange.range.first < last &&
      !blockedRange.allowsCandidate(this, sourceCode)
    }
  }

  private fun BlockedRange.allowsCandidate(candidate: IntRange, sourceCode: CharSequence): Boolean {
    if (kind != BlockedRangeKind.INDENTED_CODE) return false
    return firstNonWhitespaceOffset(sourceCode, range.first, range.last) == candidate.first
  }

  private fun firstNonWhitespaceOffset(text: CharSequence, start: Int, end: Int): Int {
    var offset = start
    while (offset < end) {
      if (!text[offset].isWhitespace()) return offset
      offset++
    }
    return -1
  }

  private data class BlockedRange(val range: IntRange, val kind: BlockedRangeKind)

  private data class TemplateRange(val range: IntRange, val kind: TemplateRangeKind)

  private enum class BlockedRangeKind {
    HARD,
    INDENTED_CODE,
    INVALID_COMMENT
  }

  private enum class TemplateRangeKind {
    ESM,
    JSX,
    EXPRESSION
  }

  private companion object {
    private val HARD_CODE_TOKENS = TokenSet.create(
      MarkdownTokenTypes.CODE_FENCE_START,
      MarkdownTokenTypes.CODE_FENCE_CONTENT,
      MarkdownTokenTypes.CODE_FENCE_END,
      MarkdownTokenTypes.BACKTICK,
      MarkdownTokenTypes.ESCAPED_BACKTICKS,
    )
  }
}
