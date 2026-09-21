package org.intellij.plugin.mdx.lang.template

import com.intellij.lang.javascript.types.JSEmbeddedBlockElementType
import com.intellij.lexer.Lexer
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.templateLanguages.TemplateDataElementType
import com.intellij.psi.templateLanguages.TemplateDataModifications
import com.intellij.psi.tree.TokenSet
import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.accept
import org.intellij.markdown.ast.visitors.RecursiveVisitor
import org.intellij.markdown.parser.CancellationToken
import org.intellij.markdown.parser.MarkdownParser
import org.intellij.plugin.mdx.lang.MdxLanguage
import org.intellij.plugin.mdx.lang.parse.MdxExpressionBoundaryScanner
import org.intellij.plugin.mdx.lang.parse.MdxFlavourDescriptor
import org.intellij.plugin.mdx.lang.parse.MdxMarkdownLibElementTypes
import org.intellij.plugin.mdx.lang.parse.MdxTextRangeSet
import org.intellij.plugin.mdx.lang.parse.MdxTokenTypes
import org.intellij.plugin.mdx.lang.parse.mdxCancellableText
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
    val source = mdxCancellableText(sourceCode)
    val structure = collectTemplateStructure(source)
    val comments = collectHtmlComments(source, baseLexer, structure.opaqueRanges)
    val invalidComments = comments.invalid
    val embeddedRanges = MdxTextRangeSet.of(
      MdxTextRangeSet.of(structure.pieces.map { it.range }).subtract(comments.opaque) + invalidComments,
    )

    val operations = mutableListOf<Modification>()
    var offset = 0
    for (range in embeddedRanges) {
      if (offset < range.startOffset) {
        operations.add(Modification.Outer(TextRange(offset, range.startOffset)))
      }
      offset = maxOf(offset, range.endOffset)
    }
    if (offset < source.length) {
      operations.add(Modification.Outer(TextRange(offset, source.length)))
    }

    // JavaScript accepts `<!--` as a legacy line-comment opener. MDX explicitly rejects HTML
    // comments, so hide one dash and let the existing JSX parser report malformed markup.
    for (range in invalidComments) {
      operations.add(Modification.Outer(TextRange(range.startOffset + 3, range.startOffset + 4)))
    }

    for (index in structure.pieces.indices) {
      val piece = structure.pieces[index]
      if (!piece.rootKind.requiresStatementSeparator) continue
      val nextRange = structure.pieces.getOrNull(index + 1)?.range
      val continuesAtBoundary = nextRange?.startOffset == piece.range.endOffset && nextRange.endOffset > piece.range.endOffset
      if (!continuesAtBoundary && !endsWithSemicolon(source, piece.range)) {
        operations.add(Modification.Remove(piece.range.endOffset, ";"))
      }
    }
    for (comment in invalidComments) {
      if (!endsWithSemicolon(source, comment)) {
        operations.add(Modification.Remove(comment.endOffset, ";"))
      }
    }

    val modifications = TemplateDataModifications()
    for (operation in operations.sortedWith(compareBy<Modification> { it.offset }.thenBy { it.priority })) {
      when (operation) {
        is Modification.Outer -> {
          modifications.addOuterRange(operation.range, true)
        }
        is Modification.Remove -> modifications.addRangeToRemove(operation.offset, operation.text)
      }
    }
    return modifications
  }

  /**
   * Derives template ranges from the same Markdown structure that drives the base PSI and editor
   * highlighter. This follows RMarkdown's template-data pattern and deliberately does not rescan JSX.
   */
  private fun collectTemplateStructure(sourceCode: CharSequence): TemplateStructure {
    val root = MarkdownParser(MdxFlavourDescriptor, cancellationToken = CancellationToken { ProgressManager.checkCanceled() })
      .parse(MarkdownElementType("MDX_TEMPLATE_ROOT"), sourceCode)
    val roots = mutableListOf<TemplateRoot>()
    val opaqueRanges = mutableListOf<TextRange>()
    val linePrefixes = LinePrefixIndex(sourceCode)
    var templateRootDepth = 0
    var opaqueDepth = 0
    root.accept(object : RecursiveVisitor() {
      override fun visitNode(node: ASTNode) {
        ProgressManager.checkCanceled()
        val rootKind = node.type.rootKind()
        if (rootKind != null && templateRootDepth == 0) {
          roots.add(TemplateRoot(TextRange(node.startOffset, node.endOffset), rootKind))
        }
        if (rootKind != null) templateRootDepth++
        val opaque = node.type in OPAQUE_MARKDOWN_ELEMENTS
        if (opaque && opaqueDepth == 0) {
          opaqueRanges.add(TextRange(linePrefixes.opaqueStart(node.startOffset), node.endOffset))
        }
        if (opaque) opaqueDepth++
        super.visitNode(node)
        if (opaque) opaqueDepth--
        if (rootKind != null) templateRootDepth--
      }
    })
    val normalizedRoots = roots
      .map { it.withExactExpressionRange(sourceCode) }
      .mergeAdjacentEsm(sourceCode)
    val normalizedOpaqueRanges = MdxTextRangeSet.of(opaqueRanges)
    return TemplateStructure(projectRoots(normalizedRoots, normalizedOpaqueRanges), normalizedOpaqueRanges)
  }

  private fun TemplateRoot.withExactExpressionRange(sourceCode: CharSequence): TemplateRoot {
    if (kind != RootKind.EXPRESSION) return this
    val expressionEnd = MdxExpressionBoundaryScanner.findExpressionEnd(sourceCode, range.startOffset, range.endOffset)
    return if (expressionEnd == -1) this else copy(range = TextRange(range.startOffset, expressionEnd))
  }

  private fun List<TemplateRoot>.mergeAdjacentEsm(sourceCode: CharSequence): List<TemplateRoot> {
    if (isEmpty()) return emptyList()
    var current = first()
    return buildList {
      for (next in this@mergeAdjacentEsm.drop(1)) {
        if (current.kind == RootKind.ESM && next.kind == RootKind.ESM &&
            isSingleWhitespaceLine(sourceCode, current.range.endOffset, next.range.startOffset)) {
          current = current.copy(range = TextRange(current.range.startOffset, next.range.endOffset))
        }
        else {
          add(current)
          current = next
        }
      }
      add(current)
    }
  }

  private fun isSingleWhitespaceLine(sourceCode: CharSequence, startOffset: Int, endOffset: Int): Boolean {
    if (startOffset >= endOffset) return false
    var lineBreaks = 0
    var offset = startOffset
    while (offset < endOffset) {
      val char = sourceCode[offset]
      offset++
      if (!char.isWhitespace()) return false
      if (char == '\n' && ++lineBreaks > 1) return false
    }
    return lineBreaks == 1
  }

  private fun projectRoots(roots: List<TemplateRoot>, exclusions: MdxTextRangeSet): List<EmbeddedPiece> {
    val projectedJsxRanges = MdxTextRangeSet.of(roots.filter { it.kind == RootKind.JSX }.map { it.range })
      .subtract(exclusions)
    var projectedJsxIndex = 0
    return buildList {
      for ((rootRange, rootKind) in roots) {
        if (rootKind != RootKind.JSX) {
          add(EmbeddedPiece(rootRange, rootKind))
          continue
        }
        while (projectedJsxIndex < projectedJsxRanges.size &&
               projectedJsxRanges[projectedJsxIndex].startOffset < rootRange.endOffset) {
          val range = projectedJsxRanges[projectedJsxIndex]
          if (range.endOffset > rootRange.startOffset) {
            add(EmbeddedPiece(
              TextRange(maxOf(range.startOffset, rootRange.startOffset), minOf(range.endOffset, rootRange.endOffset)),
              RootKind.JSX,
            ))
          }
          if (range.endOffset > rootRange.endOffset) {
            break
          }
          projectedJsxIndex++
        }
      }
    }
  }

  private fun collectHtmlComments(
    sourceCode: CharSequence,
    baseLexer: Lexer,
    opaqueRanges: MdxTextRangeSet,
  ): HtmlComments {
    val hardRanges = mutableListOf<TextRange>()
    baseLexer.start(sourceCode)
    while (baseLexer.tokenType != null) {
      ProgressManager.checkCanceled()
      if (baseLexer.tokenType in HARD_CODE_TOKENS) {
        hardRanges.add(TextRange(baseLexer.tokenStart, baseLexer.tokenEnd))
      }
      baseLexer.advance()
    }
    val codeRanges = MdxTextRangeSet.of(hardRanges + opaqueRanges)

    val invalid = mutableListOf<TextRange>()
    val opaque = mutableListOf<TextRange>()
    var codeRangeIndex = 0
    var start = findMarker(sourceCode, "<!--", 0)
    while (start >= 0) {
      val endMarker = findMarker(sourceCode, "-->", start + 4)
      if (endMarker < 0) break
      val end = endMarker + 3
      val singleLine = hasNoLineBreak(sourceCode, start, end)
      while (codeRangeIndex < codeRanges.size && codeRanges[codeRangeIndex].endOffset <= start) {
        codeRangeIndex++
      }
      val codeRange = codeRanges.getOrNull(codeRangeIndex)
      val inCode = codeRange != null && codeRange.startOffset <= start && end <= codeRange.endOffset
      if (!inCode) {
        (if (singleLine) invalid else opaque).add(TextRange(start, end))
      }
      start = findMarker(sourceCode, "<!--", end)
    }
    return HtmlComments(invalid, MdxTextRangeSet.of(opaque))
  }

  private fun findMarker(sourceCode: CharSequence, marker: String, startOffset: Int): Int {
    var offset = startOffset.coerceAtLeast(0)
    val lastStart = sourceCode.length - marker.length
    while (offset <= lastStart) {
      var markerOffset = 0
      while (markerOffset < marker.length && sourceCode[offset + markerOffset] == marker[markerOffset]) {
        markerOffset++
      }
      if (markerOffset == marker.length) return offset
      offset++
    }
    return -1
  }

  private fun hasNoLineBreak(sourceCode: CharSequence, startOffset: Int, endOffset: Int): Boolean {
    var offset = startOffset
    while (offset < endOffset) {
      val hasLineBreak = sourceCode[offset] == '\n'
      offset++
      if (hasLineBreak) return false
    }
    return true
  }

  private fun IElementType.rootKind(): RootKind? = when (this) {
    MdxMarkdownLibElementTypes.MDX_ESM_BLOCK -> RootKind.ESM
    MdxMarkdownLibElementTypes.MDX_JSX_FLOW_ELEMENT,
    MdxMarkdownLibElementTypes.MDX_JSX_TEXT_ELEMENT -> RootKind.JSX
    MdxMarkdownLibElementTypes.MDX_EXPRESSION -> RootKind.EXPRESSION
    else -> null
  }

  private fun endsWithSemicolon(sourceCode: CharSequence, range: TextRange): Boolean {
    var offset = range.endOffset - 1
    while (offset >= range.startOffset && sourceCode[offset].isWhitespace()) {
      offset--
    }
    return offset >= range.startOffset && sourceCode[offset] == ';'
  }

  private data class TemplateStructure(
    val pieces: List<EmbeddedPiece>,
    val opaqueRanges: MdxTextRangeSet,
  )

  private data class TemplateRoot(val range: TextRange, val kind: RootKind)

  private data class EmbeddedPiece(val range: TextRange, val rootKind: RootKind)

  private data class HtmlComments(val invalid: List<TextRange>, val opaque: MdxTextRangeSet)

  private sealed class Modification(val offset: Int, val priority: Int) {
    class Remove(offset: Int, val text: String) : Modification(offset, 0)
    class Outer(val range: TextRange) : Modification(range.startOffset, 1)
  }

  private enum class RootKind(val requiresStatementSeparator: Boolean) {
    ESM(true),
    JSX(true),
    EXPRESSION(false),
  }

  private class LinePrefixIndex(sourceCode: CharSequence) {
    private var entries = IntArray(32)
    private var entryCount = 0

    init {
      var lineStart = 0
      while (lineStart <= sourceCode.length) {
        var offset = lineStart
        var firstContent = sourceCode.length
        while (offset < sourceCode.length) {
          val char = sourceCode[offset]
          if (char == '\n') break
          if (firstContent == sourceCode.length && !char.isWhitespace()) {
            firstContent = offset
          }
          offset++
        }
        add(lineStart, firstContent)
        if (offset >= sourceCode.length) break
        lineStart = offset + 1
      }
    }

    fun opaqueStart(offset: Int): Int {
      var low = 0
      var high = entryCount
      while (low + 1 < high) {
        val middle = (low + high) ushr 1
        if (lineStart(middle) <= offset) low = middle
        else high = middle
      }
      return if (firstContentOffset(low) >= offset) lineStart(low) else offset
    }

    private fun add(lineStart: Int, firstContentOffset: Int) {
      if (entryCount * 2 == entries.size) {
        entries = entries.copyOf(entries.size * 2)
      }
      entries[entryCount * 2] = lineStart
      entries[entryCount * 2 + 1] = firstContentOffset
      entryCount++
    }

    private fun lineStart(index: Int): Int = entries[index * 2]

    private fun firstContentOffset(index: Int): Int = entries[index * 2 + 1]
  }

  private companion object {
    private val OPAQUE_MARKDOWN_ELEMENTS = setOf(
      MarkdownElementTypes.CODE_BLOCK,
      MarkdownElementTypes.CODE_FENCE,
      MarkdownElementTypes.CODE_SPAN,
    )

    private val HARD_CODE_TOKENS = TokenSet.create(
      MarkdownTokenTypes.CODE_FENCE_START,
      MarkdownTokenTypes.CODE_FENCE_CONTENT,
      MarkdownTokenTypes.CODE_FENCE_END,
      MarkdownTokenTypes.BACKTICK,
      MarkdownTokenTypes.ESCAPED_BACKTICKS,
      MarkdownTokenTypes.CODE_LINE,
    )
  }
}
